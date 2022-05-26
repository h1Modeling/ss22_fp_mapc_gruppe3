package de.feu.massim22.group3.agents;

import java.awt.Point;
import java.util.*;

import de.feu.massim22.group3.agents.Desires.ADesires.ADesire;
import de.feu.massim22.group3.agents.Desires.ADesires.GoAdoptRole;
import de.feu.massim22.group3.agents.Desires.ADesires.DigFree;
import de.feu.massim22.group3.agents.Desires.ADesires.DodgeClear;
import de.feu.massim22.group3.agents.Desires.ADesires.GetBlock;
import de.feu.massim22.group3.agents.Desires.ADesires.GoDispenser;
import de.feu.massim22.group3.agents.Desires.ADesires.GoGoalZone;
import de.feu.massim22.group3.agents.Desires.ADesires.GoRoleZone;
import de.feu.massim22.group3.agents.Desires.ADesires.LocalExplore;
import de.feu.massim22.group3.agents.Desires.ADesires.GoSubmit;
import de.feu.massim22.group3.agents.Desires.ADesires.HinderEnemy;
import de.feu.massim22.group3.agents.Desires.ADesires.RemoveObstacle;
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
	
	public String task;
    public int directionCounter = 0;
    public int circleSize = 5;
	
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
                + " , Supervisor: " + agent.getName());

        doDecision(agent, new DodgeClear(agent));
        doDecision(agent, new DigFree(agent));
        doDecision(agent, new HinderEnemy(agent));
        doDecision(agent, new GoGoalZone(agent));
        doDecision(agent, new GoRoleZone(agent));
        doDecision(agent, new GoDispenser(agent));
        doDecision(agent, new GetBlock(agent));
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
            getPriority(inDesire);
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
				+ " , Supervisor: " + supervisor.getName());

		BdiAgentV2 supervisorAgent = StepUtilities.getAgent(supervisor.getName());
		Set<TaskInfo> set = supervisorAgent.belief.getTaskInfo();
		List<Point> attachedPoints;
		List<Thing> attachedThings = new ArrayList<Thing>();
		String role = "";

		// Schleife über alle Tasks
		/*
		 * for(TaskInfo t : set) { if(minBlockCount > t.requirements.size()) { //Task
		 * mit wenigsten Blöcken minBlockCount = t.requirements.size(); minTask = t; } }
		 */

		List<BdiAgent> allGroupAgents = supervisor.getAllGroupAgents();
		List<BdiAgent> freeGroupAgents = allGroupAgents;
		List<BdiAgent> busyGroupAgents = new ArrayList<>();

		// Schleife über alle Tasks
		for (TaskInfo task : set) {
			// über alle Agenten einer Gruppe
			for (BdiAgent agent : freeGroupAgents) {
				// alle Blöcke die ein Agent hat
				attachedPoints = agent.belief.getAttachedThings();

				if (attachedPoints.size() > 0) {
					Set<Thing> things = agent.belief.getThings();
					for (Point p : attachedPoints) {
						for (Thing t : things) {
							if (t.x == p.x && t.y == p.y) {
								attachedThings.add(t);
								break;
							}
						}
					}

					// was hat der Agent für eine Rolle
					// role = agent.getAgentBelief().getRole();

					List<Thing> goodBlocks = new ArrayList<Thing>();
					List<Thing> badBlocks = new ArrayList<Thing>();
					List<Thing> goodPositionBlocks = new ArrayList<Thing>();
					List<Thing> badPositionBlocks = new ArrayList<Thing>();
					List<Thing> missingBlocks = new ArrayList<Thing>();
					boolean typeOk = false;

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

					if (goodPositionBlocks.size() > 0 && badPositionBlocks.size() == 0 && badBlocks.size() == 0
							&& missingBlocks.size() == 0) {
						// wenn ein Agent alle Blöcke einer Task an der richtigen Stelle besitzt
						// GoSubmit
						this.task = task.name;
						doDecision(agent, new GoSubmit(agent));
						busyGroupAgents.add(agent);
						break; // nächster Agent
					} else if (goodPositionBlocks.size() > 0 && badPositionBlocks.size() > 0 && badBlocks.size() == 0
							&& missingBlocks.size() == 0) {
						// wenn ein Agent alle Blöcke einer Task besitzt (eventuell an der falschen
						// Stelle)
						// ArrangeBlocks
						// doDecision(((BdiAgentV2) agent), new ArrangeBlocks(((BdiAgentV2) agent),
						// this));
						busyGroupAgents.add(agent);
						break; // nächster Agent
					} else if (goodPositionBlocks.size() > 0 && badPositionBlocks.size() > 0 && badBlocks.size() == 0
							&& missingBlocks.size() == 0) {
						// wenn ein Agent Blöcke einer Task besitzt (nicht alle)
						// GetBlock
						doDecision(agent, new GetBlock(agent));
						busyGroupAgents.add(agent);
						break; // nächster Agent
					}
				} // If blocks attached
			} // Loop agents
		} // Loop tasks

		freeGroupAgents.removeAll(busyGroupAgents);
		busyGroupAgents.clear();
		
		// über alle Agenten einer Gruppe
		for (BdiAgent agent : freeGroupAgents) {
			
		}
		
		supervisor.setDecisionsDone(true);
		return result;
	}
    

    /**
     * The method has a certain priority for every desire.
     *
     * @param desire - the desire that needs a priority
     * 
     * @return int - the priority
     */
    public int getPriority(ADesire desire) {
        int result = 0;

        switch (desire.name) {
        case "DigFree":
            result = 10;
        case "GoSubmit":
        	if(desire.getNextAction().getName().equals("submit")) {
        		result = 15; 
        	} else {
        		result = 25;
        	}         
        case "DodgeClear":
            result = 20;
        case "GoAdoptRole":
            result = 30;
        case "ArrangeBlocks":
            result = 40;
        case "GetBlock":
            result = 50;
        case "ReactToNorm":
            result = 40;
        case "GoGoalZone":
            result = 80;
        case "GoRoleZone":
            result = 30;
        case "GoDispenser":
            result = 60;
        case "LocalGetBlocks":
            result = 70;
        case "Explore":
            result = 85;
        case "LocalHinderEnemy":
            result = 95;
        case "LocalExplore":
            result = 100;
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
    public ADesire determineIntention(BdiAgentV2 agent) {
        ADesire result = null;
        int priority = 1000;
        for (ADesire desire : agent.desires) {
            AgentLogger.info(Thread.currentThread().getName() + " determineIntention() - Agent: " + agent.getName()
                    + " , Desire: " + desire.name + " , Action: " + desire.outputAction);
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
}

