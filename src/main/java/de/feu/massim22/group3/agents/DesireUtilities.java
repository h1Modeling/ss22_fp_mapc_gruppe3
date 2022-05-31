package de.feu.massim22.group3.agents;

import java.awt.Point;
import java.util.*;

import de.feu.massim22.group3.agents.Desires.ADesires.*;

import de.feu.massim22.group3.agents.Reachable.ReachableDispenser;
import de.feu.massim22.group3.agents.Reachable.ReachableGoalZone;
import de.feu.massim22.group3.agents.Reachable.ReachableRoleZone;
import de.feu.massim22.group3.utils.logging.AgentLogger;
import eis.iilang.Identifier;
import eis.iilang.Action;
import massim.protocol.data.TaskInfo;
import massim.protocol.data.Thing;
import massim.protocol.messages.scenario.Actions;

public class DesireUtilities {
	
	public TaskInfo task;
    public int directionCounter = 0;
    public int circleSize = 5;
 
	public List<Thing> attachedThings = new ArrayList<Thing>();
    public List<Thing> goodBlocks = new ArrayList<Thing>();
    public List<Thing> badBlocks = new ArrayList<Thing>();
    public List<Thing> goodPositionBlocks = new ArrayList<Thing>();
    public List<Thing> badPositionBlocks = new ArrayList<Thing>();
    public List<Thing> missingBlocks = new ArrayList<Thing>();
	public boolean typeOk = false;
	public boolean analysisDone = false;
	
    /**
     * The method runs the different agent decisions.
     *
     * @param agent - the agent who wants to make the decisions
     * 
     * @return boolean - the agent decisions are done
     */
    public synchronized boolean runAgentDecisions(int step, BdiAgent agent) {
        boolean result = false;
        AgentLogger.info(Thread.currentThread().getName() + " runAgentDecisions() Start - Step: " + step
                + " , Agent: " + agent.getName());

        doDecision(agent, new DodgeClear(agent));
        doDecision(agent, new DigFree(agent));
        doDecision(agent, new HinderEnemy(agent));
        doDecision(agent, new GoGoalZone(agent));
        doDecision(agent, new GoDispenser(agent));
        doDecision(agent, new GoAdoptRole(agent));
        doDecision(agent, new RemoveObstacle(agent));
        doDecision(agent, new LocalExplore(agent));
        
        agent.decisionsDone = true;
        return result;
    }
    
    boolean doDecision(BdiAgent agent, ADesire inDesire) {
        boolean result = false;
      
        if (inDesire.isExecutable()) { // desire ist möglich , hinzufügen
            inDesire.outputAction = inDesire.getNextAction();
            inDesire.priority = getPriority(inDesire);
            agent.desires.add(inDesire);
            result = true;
        }
        
        return result;
    }
    

    /**
     * The method runs the different supervisor decisions.
     *
     * @param supervisor - the supervisor who wants to make the decisions
     * 
     * @return boolean - the supervisor decisions are done
     */
	public synchronized boolean runSupervisorDecisions(int step, Supervisor supervisor) {
		boolean result = false;
		int minBlockCount = 1000;
		TaskInfo minTask;

		AgentLogger.info(Thread.currentThread().getName() + " runSupervisorDecisions() Start - Step: " + step
				+ " , Supervisor: " + supervisor.getName() + " , Agents: " + supervisor.getAgents());

		BdiAgentV2 supervisorAgent = StepUtilities.getAgent(supervisor.getName());
		Set<TaskInfo> set = supervisorAgent.belief.getTaskInfo();
		List<Point> attachedPoints;
		String role = "";

		List<String> allGroupAgents = new ArrayList<>(supervisor.getAgents());
		List<String> freeGroupAgents = new ArrayList<>(allGroupAgents);
		List<String> busyGroupAgents = new ArrayList<>();

        // Schleife über alle Tasks
        for (TaskInfo loopTask : set) {
            task = loopTask;
            AgentLogger.info(Thread.currentThread().getName() + " runSupervisorDecisions() Task: " + task.name
                    + " Agents: " + allGroupAgents + " freie Agents: " + freeGroupAgents);
            // über alle Agenten einer Gruppe
            for (String agentStr : allGroupAgents) {
                AgentLogger.info(Thread.currentThread().getName() + " runSupervisorDecisions() Agent: " + agentStr);
                attachedThings = new ArrayList<Thing>();
                BdiAgent agent = StepUtilities.getAgent(agentStr);
                agent.desireProcessing.task = task;

                // alle Blöcke die ein Agent hat
                attachedPoints = agent.belief.getAttachedThings();
                AgentLogger.info(Thread.currentThread().getName() + " Agent: " + agentStr + " attachedPoints: "
                        + attachedPoints);

                Set<Thing> things = agent.belief.getThings();
                for (Point p : attachedPoints) {
                    for (Thing t : things) {
                        if (t.x == p.x && t.y == p.y) {
                            attachedThings.add(t);
                            break;
                        }
                    }
                }

                AgentLogger.info(Thread.currentThread().getName() + " Agent: " + agentStr + " attachedThings: "
                        + attachedThings);

                // was hat der Agent für eine Rolle
                role = agent.getAgentBelief().getRole();
                // Agent hat default Rolle
                if (role.equals(" default") && doDecision(agent, new GoAdoptRole(agent, "worker"))) {
                    busyGroupAgents.add(agent.getName());
                } else {

                    goodBlocks = new ArrayList<Thing>();
                    badBlocks = new ArrayList<Thing>();
                    goodPositionBlocks = new ArrayList<Thing>();
                    badPositionBlocks = new ArrayList<Thing>();
                    missingBlocks = new ArrayList<Thing>();
                    typeOk = false;

                    // wenn ein Agent alle Blöcke einer Task an der richtigen Stelle besitzt
                    AgentLogger.info(Thread.currentThread().getName() + " runSupervisorDecisions() vor GoSubmit");
                    if (doDecision(agent, new GoSubmit(agent))) {
                        busyGroupAgents.add(agent.getName());

                        // wenn ein Agent alle Blöcke einer Task besitzt (eventuell an der falschen
                        // Stelle)
                    } else {
                        AgentLogger
                                .info(Thread.currentThread().getName() + " runSupervisorDecisions() vor ArrangeBlocks");
                        if (doDecision(agent, new ArrangeBlocks(agent))) {
                            busyGroupAgents.add(agent.getName());

                            // wenn ein Agent Blöcke einer Task besitzt (nicht alle)
                        } else {
                            String type = "";
                            if (missingBlocks.size() > 0) {
                                type = missingBlocks.get(0).type;
                            } else {
                                type = task.requirements.get(0).type;
                            }
                            AgentLogger.info(
                                    Thread.currentThread().getName() + " runSupervisorDecisions() vor GoDispenser");
                            if (doDecision(agent, new GoDispenser(agent, type))) {
                                busyGroupAgents.add(agent.getName());

                                // wenn der gesuchte Dispenser nicht ereichbar ist
                            } else {
                                AgentLogger.info(
                                        Thread.currentThread().getName() + " runSupervisorDecisions() vor Explore");
                                if (doDecision(agent, new Explore(agent))) {
                                    busyGroupAgents.add(agent.getName());
                                }
                            }
                        }
                    }//go submit
                }//adopt role
            } // Loop agents
        } // Loop tasks

        freeGroupAgents.removeAll(busyGroupAgents);
        busyGroupAgents.clear();

        // über alle Agenten einer Gruppe z.B, TODO fremde Gegenden erkunden etc.
		for (String agentStr : freeGroupAgents) {
			
		}
		
		supervisor.setDecisionsDone(true);
		AgentLogger.info(Thread.currentThread().getName() + " runSupervisorDecisions() End - Step: " + step
				+ " , Supervisor: " + supervisor.getName() + " , Agents: " + supervisor.getAgents());
		return result;
	}
    

    /**
     * The method has a certain priority for every desire.
     *
     * @param desire - the desire that needs a priority
     * 
     * @return int - the priority
     */
	// TODO sinnvolle Prioritäten vergeben
    public int getPriority(ADesire desire) {
        int result = 0;

        switch (desire.name) {
        case "DigFree":
            result = 10;
            break;
        case "GoSubmit":
			if (desire.getNextAction().getName().equals("submit")) {
				result = 15;
			} else {
				result = 25;
			}
            break;
        case "DodgeClear":
            result = 20;
            break;
        case "GoAdoptRole":
            if (desire.groupOrder) {
                result = 17 ;               
            } else {
                result = 60;            
            }
            break;
        case "ArrangeBlocks":
            result = 40;
            break;
        case "ReactToNorm":
            result = 40;
            break;
        case "GoGoalZone":
            result = 80;
            break;
        case "GoRoleZone":
            result = 90;
            break;
        case "GoDispenser":
        	if (desire.groupOrder) {
                result = 50;        		
        	} else {
                result = 60;       		
        	}
            break;
        case "Explore":
            result = 55;
            break;
        case "LocalHinderEnemy":
            result = 95;
            break;
        case "LocalExplore":
            result = 100;
            break;
        }

        return result;
    }

    /**
     * The method determines the Intention for a certain agent .
     *
     * @param agent - the agent that needs a intention
     * 
     * @return Desire - the intention
     */
    public synchronized ADesire determineIntention(BdiAgentV2 agent) {
        ADesire result = null;
        int priority = 1000;
        for (ADesire desire : agent.desires) {
            AgentLogger.info(Thread.currentThread().getName() + " determineIntention() - Agent: " + agent.getName()
                    + " , Desire: " + desire.name + " , Action: " + desire.outputAction + " , Prio: " + desire.priority);
            if (desire.priority < priority) {
                result = desire;
                priority = desire.priority;
            }
        }

        AgentLogger.info(Thread.currentThread().getName() + " determineIntention() End - Agent: " + agent.getName()
                + " , Intention: " + result.name + " , Action: " + result.outputAction);
        return result;
    }

    public ReachableGoalZone getNearestGoalZone(List<ReachableGoalZone> inZoneList) {
        int distance = 1000;
        ReachableGoalZone result = null;

        for (ReachableGoalZone zone : (List<ReachableGoalZone>) inZoneList) {
            if (zone.distance() < distance) {
                distance = zone.distance();
                result = zone;
            }
        }
        return result;
    }
    
    public ReachableRoleZone getNearestRoleZone(List<ReachableRoleZone> inZoneList) {
        int distance = 1000;
        ReachableRoleZone result = null;
        
        for (ReachableRoleZone zone : (List<ReachableRoleZone>) inZoneList) {
            if (zone.distance() < distance) {
                distance = zone.distance();
                result = zone;
            }
        }
        return result;
    }

    public ReachableDispenser getNearestDispenser(List<ReachableDispenser> inZoneList) {
        int distance = 1000;
        ReachableDispenser result = null;

        for (ReachableDispenser zone : (List<ReachableDispenser>) inZoneList) {
            if (zone.distance() < distance) {
                distance = zone.distance();
                result = zone;
            }
        }
        return result;
    }
    
    public Identifier walkCircles(BdiAgent agent, int stepWidth) {
        Identifier resultDirection = new Identifier("n");

        if (agent.belief.getLastAction() != null && agent.belief.getLastAction().equals(Actions.MOVE)) {
            directionCounter++;
            resultDirection = new Identifier(agent.belief.getLastActionParams().get(0));

            if (agent.belief.getLastAction().equals("move") && directionCounter >= circleSize) {
                if (agent.belief.getLastActionParams().get(0).equals("n")) resultDirection = new Identifier("e");
                if (agent.belief.getLastActionParams().get(0).equals("e")) resultDirection = new Identifier("s");
                if (agent.belief.getLastActionParams().get(0).equals("s")) resultDirection = new Identifier("w");
                if (agent.belief.getLastActionParams().get(0).equals("w")) {
                    resultDirection = new Identifier("n");
                    circleSize = circleSize + stepWidth;
                }
                directionCounter = 0;
            }
        }
        return resultDirection;
    }
    
    public void analyseAttachedThings() {
		
		for (Thing attachedBlock : attachedThings) {
			if (task.requirements.contains(attachedBlock)) {
				// Blocktype stimmt
				// Block ist an der richtigen Stelle
				goodBlocks.add(attachedBlock);
				goodPositionBlocks.add(attachedBlock);
			} else {
				typeOk = false;
				for (Thing taskBlock : task.requirements) {
					if (attachedBlock.details.equals(taskBlock.details)) {
						// Blocktype stimmt
						// Block ist an der falschen Stelle
						goodBlocks.add(attachedBlock);
						badPositionBlocks.add(attachedBlock);
						typeOk = true;
						break;
					}
				}
				if (!typeOk) {
					// Blocktype stimmt nicht
					badBlocks.add(attachedBlock);
				}
			}
		}
		AgentLogger.info(Thread.currentThread().getName() + " analyseAttachedThings() Task: " + task.name);
		for (Thing taskBlock : task.requirements) {
			if (!attachedThings.contains(taskBlock)) {
				typeOk = false;
				for (Thing badPositionBlock : badPositionBlocks) {
					if (badPositionBlock.details.equals(taskBlock.details)) {
						// Blocktype vorhanden
						typeOk = true;
						break;
					}
				}
				if (!typeOk) {
					// Blocktype fehlt
					missingBlocks.add(taskBlock);
				}
			}
		}
    }
}

