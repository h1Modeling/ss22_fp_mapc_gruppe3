package de.feu.massim22.group3.agents;

import java.awt.Point;
import java.util.*;

import de.feu.massim22.group3.agents.Desires.BDesires.*;

import de.feu.massim22.group3.agents.Reachable.ReachableDispenser;
import de.feu.massim22.group3.agents.Reachable.ReachableGoalZone;
import de.feu.massim22.group3.agents.Reachable.ReachableRoleZone;
import de.feu.massim22.group3.utils.logging.AgentLogger;
import eis.iilang.Identifier;
import eis.iilang.Action;
import massim.protocol.data.TaskInfo;
import massim.protocol.data.Thing;
import massim.protocol.messages.scenario.Actions;
import massim.protocol.messages.scenario.ActionResults;

public class DesireUtilities {
	
	public TaskInfo task;
    public String directionCircle = "cw";
    public int directionCounter = 0;
    public int circleSize = 40;
 
	public List<Thing> attachedThings = new ArrayList<Thing>();
    public List<Thing> goodBlocks = new ArrayList<Thing>();
    public List<Thing> badBlocks = new ArrayList<Thing>();
    public List<Thing> goodPositionBlocks = new ArrayList<Thing>();
    public List<Thing> badPositionBlocks = new ArrayList<Thing>();
    public List<Thing> missingBlocks = new ArrayList<Thing>();
	public boolean typeOk = false;
	public boolean analysisDone = false;
	public boolean dontArrange = false;
	public String nextTry = "ccw";
	public int nextTryDir = 1;
	public int failedPath = 0;
	
    /**
     * The method runs the different agent decisions.
     *
     * @param agent - the agent who wants to make the decisions
     * 
     * @return boolean - the agent decisions are done
     */
    public synchronized boolean runAgentDecisions(int step, BdiAgentV2 agent) {
        boolean result = false;
        AgentLogger.info(Thread.currentThread().getName() + " runAgentDecisions() Start - Step: " + step
                + " , Agent: " + agent.getName());

        doDecision(agent, new DigFreeDesire(agent.belief));
        doDecision(agent, new LocalExploreDesire(agent.belief, agent.supervisor.getName(), agent));
        
        agent.decisionsDone = true;
        return result;
    }
 
    boolean doDecision(BdiAgentV2 agent, IDesire inDesire) {
        boolean result = false;
      
        if (!inDesire.isFulfilled().value() 
                && !inDesire.isUnfulfillable().value() 
                && inDesire.isExecutable().value()) { // desire ist möglich , hinzufügen
            inDesire.setOutputAction(inDesire.getNextActionInfo().value());
            inDesire.setPriority(getPriority(inDesire));
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
        AgentLogger.info(Thread.currentThread().getName() + " runSupervisorDecisions() Start - Step: " + step
                + " , Supervisor: " + supervisor.getName() + " , Agents: " + supervisor.getAgents());

        BdiAgentV2 supervisorAgent = StepUtilities.getAgent(supervisor.getName());
        //Set<TaskInfo> set = supervisorAgent.belief.getTaskInfo();
        //List<Point> attachedPoints;
        //Role role = null;

        List<String> allGroupAgents = new ArrayList<>(supervisor.getAgents());
        List<String> freeGroupAgents = new ArrayList<>(allGroupAgents);
        List<String> busyGroupAgents = new ArrayList<>();

        // Schleife über alle Tasks
        //for (TaskInfo loopTask : supervisorAgent.belief.getNewTasks()) {
        for (TaskInfo loopTask : supervisorAgent.belief.getTaskInfo()) {
            task = loopTask;
            AgentLogger.info(Thread.currentThread().getName() + " runSupervisorDecisions() Task: " + task.name
                    + " Agents: " + allGroupAgents + " freie Agents: " + freeGroupAgents);

            // TODO Mehrblock-Tasks
           if ( task.requirements.size() > 1) {
//               continue;
           }
           
            // über alle Agenten einer Gruppe
            for (String agentStr : allGroupAgents) {
                AgentLogger.info(Thread.currentThread().getName() + " runSupervisorDecisions() Agent: " + agentStr);
                BdiAgentV2 agent = StepUtilities.getAgent(agentStr);               
                agent.desireProcessing.attachedThings = new ArrayList<Thing>();
                agent.desireProcessing.task = task;
                agent.desireProcessing.attachedThings = agent.belief.getAttachedThings();

                AgentLogger.info(Thread.currentThread().getName() + " runSupervisorDecisions - Agent: " + agentStr + " attachedThings: "
                        + agent.desireProcessing.attachedThings);
                AgentLogger.info(
                        Thread.currentThread().getName() + " runSupervisorDecisions() - Agent: " + agent.getName()
                                + " , lA: " + agent.belief.getLastAction() + " , lAR: " + agent.belief.getLastActionResult());
                
                //doDecision(agent, new ProcessEasyTaskDesire(agent.belief, task, agent.getName())); 
                
                if (agent.desireProcessing.attachedThings.size() == 0
                    && doDecision(agent, new AttachSingleBlockFromDispenserDesire(agent.belief, task.requirements.get(0), supervisor.getName()))) {
                    //&& doDecision(agent, new GoDispenserDesire(agent.belief, task.requirements.get(0), supervisor.getName(), agent))) {
                } else
                    AgentLogger.info(Thread.currentThread().getName() + " Desire not added - Agent: " + agent.getName()
                            + " , AttachSingleBlockFromDispenserDesire");

                if (agent.desireProcessing.attachedThings.size() > 0 && !agent.belief.getGoalZones().contains(new Point(0, 0)))
                    if (doDecision(agent, new GoToGoalZoneDesire(agent.belief))) {
                    } else
                    AgentLogger.info(Thread.currentThread().getName() + " Desire not added - Agent: " + agent.getName()
                            + " , GoToGoalZoneDesire");

                if (agent.desireProcessing.attachedThings.size() > 0 && agent.belief.getGoalZones().contains(new Point(0, 0)))
                    if (doDecision(agent, new GetBlocksInOrderDesire(agent.belief, task))) {
                    //if (doDecision(agent, new ArrangeBlocksDesire(agent.belief, task))) {
                    } else
                    AgentLogger.info(Thread.currentThread().getName() + " Desire not added - Agent: " + agent.getName()
                            + " , GetBlocksInOrderDesire");

                if (agent.desireProcessing.attachedThings.size() > 0 && agent.belief.getGoalZones().contains(new Point(0, 0)))
                    if (doDecision(agent, new SubmitDesire(agent.belief, task))) {
                    } else
                    AgentLogger.info(Thread.currentThread().getName() + " Desire not added - Agent: " + agent.getName()
                            + " , SubmitDesire");
              
                for (int i = agent.desires.size() - 1; i >= 0; i--) {
                    AgentLogger.info(Thread.currentThread().getName() + " determineIntention() - Agent: " + agent.getName()
                    + " , " + agent.desires.get(i).getName() + " , Action: " + agent.desires.get(i).getOutputAction() + " , Prio: " + agent.desires.get(i).getPriority());
                }
                
                // was hat der Agent für eine Rolle
/*                role = agent.getAgentBelief().getRole();
                // Agent hat default Rolle
                if (role.equals(" default") && doDecision(agent, new GoAdoptRole(agent, "worker"))) {
                    busyGroupAgents.add(agent.getName());
                } else {

 */
            } // Loop agents
        } // Loop tasks

        freeGroupAgents.removeAll(busyGroupAgents);
        busyGroupAgents.clear();

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
    public int getPriority(IDesire desire) {
        int result = 0;

        switch (desire.getName()) {
        
        case "DigFreeDesire":
            result = 1000;
            break;
        case "LocalExploreDesire":
            result = 10;
            break;
        case "ExploreDesire":
            result = 20;
            break;
        case "AttachSingleBlockFromDispenserDesire":
            result = 200;
            break;
        case "GoDispenserDesire":
            result = 200 - desire.getPriority();
            break;
        case "GoToGoalZoneDesire":
            result = 300;
            break;
        case "GetBlocksInOrderDesire":
            result = 400;
            break;
        case "ArrangeBlocksDesire":
            result = 400;
            break;
        case "SubmitDesire":
            result = 500;
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
    public synchronized IDesire determineIntention(BdiAgentV2 agent) {
        IDesire result = null;
        int priority = 0;
        
        /*for (int i = agent.desires.size() - 1; i >= 0; i--) {
            IDesire d = agent.desires.get(i);
            d.update(agent.supervisor.getName());

            if (!d.isFulfilled().value() && d.isExecutable().value()) {
                result = agent.desires.get(i);
                break;
            }
        }*/
        
        for (IDesire desire : agent.desires) {
            /*AgentLogger.info(Thread.currentThread().getName() + " determineIntention() - Agent: " + agent.getName()
                    + " , Desire: " + desire.getName() + " , Action: " + desire.getOutputAction() + " , Prio: " + getPriority(desire));*/
            if (getPriority(desire) > priority) {
                result = desire;
                priority = getPriority(desire);
            }
        }

        /*AgentLogger.info(Thread.currentThread().getName() + " determineIntention() End - Agent: " + agent.getName()
                + " , Intention: " + result.getName() + " , Action: " + result.getOutputAction());*/
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
    
    public Identifier walkCircles(BdiAgentV2 agent, int stepWidth) {
        String startDirection = "";
        float random = new Random().nextFloat();
        if (random < 0.25) {
            startDirection = "n";
        } else if (random < 0.5) {
            startDirection = "e";
        } else if (random < 0.75) {
            startDirection = "w";
        } else {
            startDirection = "s";
        }
        Identifier resultDirection = new Identifier(startDirection);

        if (agent.belief.getLastAction() != null && agent.belief.getLastAction().equals(Actions.MOVE)) {
            directionCounter++;
            resultDirection = new Identifier(agent.belief.getLastActionParams().get(0));
            
           /* if (agent.belief.getLastActionResult().equals(ActionResults.FAILED_PATH)) {
                if (directionCircle.equals("cw")) {
                    directionCircle = "ccw";
                } else {
                    directionCircle = "cw";
                }
            }*/

            if (directionCircle.equals("cw")) {
                if (agent.belief.getLastAction().equals("move") && directionCounter >= circleSize) {
                    if (agent.belief.getLastActionParams().get(0).equals("n")) resultDirection = new Identifier("e");
                    if (agent.belief.getLastActionParams().get(0).equals("e")) resultDirection = new Identifier("s");
                    if (agent.belief.getLastActionParams().get(0).equals("s")) resultDirection = new Identifier("w");
                    if (agent.belief.getLastActionParams().get(0).equals("w")) {
                        resultDirection = new Identifier("n");
                        circleSize = circleSize + stepWidth;
                    }
                    directionCounter = 0;
                    directionCircle = "ccw";
                }
            } else {
                if (agent.belief.getLastAction().equals("move") && directionCounter >= circleSize) {
                    if (agent.belief.getLastActionParams().get(0).equals("n")) resultDirection = new Identifier("w");
                    if (agent.belief.getLastActionParams().get(0).equals("w")) resultDirection = new Identifier("s");
                    if (agent.belief.getLastActionParams().get(0).equals("s")) resultDirection = new Identifier("e");
                    if (agent.belief.getLastActionParams().get(0).equals("e")) {
                        resultDirection = new Identifier("n");
                        circleSize = circleSize + stepWidth;
                    }
                    directionCounter = 0;
                    directionCircle = "cw";
                }
            }
        }
        return resultDirection;
    }
    
    public void analyseAttachedThings() {  
        AgentLogger.info(Thread.currentThread().getName() + " analyseAttachedThings() Task: " + task.name);
        for (Thing attachedBlock : attachedThings) {
	        
			if (blockInTask(task.requirements, attachedBlock)) {
		        AgentLogger.info(Thread.currentThread().getName()+ " analyseAttachedThings() - Block OK");    
				// Blocktype stimmt
				// Block ist an der richtigen Stelle
				goodBlocks.add(attachedBlock);
				goodPositionBlocks.add(attachedBlock);
			} else {
	             AgentLogger.info(Thread.currentThread().getName()+ " analyseAttachedThings() - Block nicht OK");  
				typeOk = false;
				
				for (Thing taskBlock : task.requirements) {
					if (attachedBlock.details.equals(taskBlock.type)) {
		                AgentLogger.info(Thread.currentThread().getName()+ " analyseAttachedThings() - Block OK an falscher Stelle"); 
						// Blocktype stimmt
						// Block ist an der falschen Stelle
						goodBlocks.add(attachedBlock);
						badPositionBlocks.add(attachedBlock);
						typeOk = true;
						break;
					}
				}
				
				if (!typeOk) {
                    AgentLogger.info(Thread.currentThread().getName()+ " analyseAttachedThings() - Block Type falsch"); 
					// Blocktype stimmt nicht
					badBlocks.add(attachedBlock);
				}
			}
		}
		
        for (int i = 0; i < 5; i++) {
            int diff = countBlockType(task.requirements, "b" + i) - countBlockType(attachedThings, "b" + i);
            
            if (diff > 0) {
                for (int j = 0; j < diff; j++) {
                    missingBlocks.add(new Thing(0, 0, Thing.TYPE_BLOCK, "b" + i));
                }
            }
        }
        AgentLogger.info(Thread.currentThread().getName() + " analyseAttachedThings() missingBlocks: " + missingBlocks);              
    }
    
    public Thing toTaskBlock(Thing toThingBlock) {           
        return new Thing(toThingBlock.x, toThingBlock.y, toThingBlock.details, "");
    }
    
    public Thing toThingBlock(Thing toTaskBlock) {           
        return new Thing(toTaskBlock.x, toTaskBlock.y, Thing.TYPE_BLOCK, toTaskBlock.type);
    }
    
    public boolean blockInTask(List<Thing> inTaskReqs, Thing inBlock) { 
        for (Thing req : inTaskReqs) {
            if (req.x == inBlock.x && req.y == inBlock.y && req.type.equals(inBlock.details)) {              
                return true;
            }
        }
        
        return false;
    }
    
    public boolean taskReqInList(List<Thing> inList, Thing inTaskReq) {     
        for (Thing block : inList) {
            if (block.x == inTaskReq.x && block.y == inTaskReq.y && block.details.equals(inTaskReq.type)) {
                return true;
            }
        }
        
        return false;
    }
    
    public int countBlockType(List<Thing> inList, String inType) {  
        int count = 0;
        
        for (Thing block : inList) {
            if (block.details.equals(inType) || block.type.equals(inType)) {
                count++;
            }
        }

        return count;
    }
    
    public Thing getContentInDirection(BdiAgentV2 agent, String direction) {
        Point cell = DirectionUtil.getCellInDirection(direction);

        return getContent(agent, cell);
    }
    
    public Thing getContentInDirection(BdiAgentV2 agent, Point from, String direction) {
        Point cell = DirectionUtil.getCellInDirection(from, direction);

        return getContent(agent, cell);
    }
    
    public Thing getContent(BdiAgentV2 agent, Point cell) {      
        //AgentLogger.info(Thread.currentThread().getName() + " getContent() - Position: " + cell);
        
        for (Thing thing : agent.belief.getThings()) {           
            if (thing.type.equals(Thing.TYPE_OBSTACLE) || thing.type.equals(Thing.TYPE_ENTITY) || thing.type.equals(Thing.TYPE_BLOCK)) {
                // an diesem Punkt ist ein Hindernis
                
                if (cell.equals(new Point(thing.x, thing.y))) {
                    //AgentLogger.info(Thread.currentThread().getName() + " getContentInDirection() - Vision: " + thing);
                    // Agent steht vor Hinderniss in Richtung direction
                    return thing;
                } 
            }
        }

        return null;
    }
    
    
    public Action getPossibleActionForMove(BdiAgentV2 agent, String direction) {
        Action nextAction = null;
        //Thing neighbourOfBlock = null;
        Thing neighbour = agent.desireProcessing.getContentInDirection(agent, direction);
        // AgentLogger.info(Thread.currentThread().getName() + "
        // getPossibleActionForMove() - Neighbour: " + neighbour);

         if (neighbour == null 
                 || (neighbour.type.equals(Thing.TYPE_BLOCK )
                 && agent.desireProcessing.attachedThings.contains(neighbour))) {
             //if (neighbour == null || (neighbour.type.equals(Thing.TYPE_BLOCK)
                //     && agent.desireProcessing.attachedThings.contains(neighbour) && agent.desireProcessing.getContentInDirection(agent, new Point(neighbour.x, neighbour.y), direction) == null)) {
             // Weg ist frei (alte Variante)
            if (agent.belief.getLastActionResult().equals(ActionResults.FAILED_PATH) 
                    || (neighbour != null && neighbour.type.equals(Thing.TYPE_BLOCK )
                            && !agent.desireProcessing.attachedThings.contains(neighbour))) {
                if (agent.desireProcessing.attachedThings.size() == 1) {
                    agent.desireProcessing.dontArrange = true;
                    
                    if (agent.belief.getLastActionResult().equals(ActionResults.FAILED_PATH)) {
                        agent.desireProcessing.failedPath += 1;
                        
                        if ( agent.desireProcessing.failedPath > 2) {
                            if(agent.desireProcessing.nextTry == "ccw") {
                                agent.desireProcessing.nextTry = "cw";
                            } else {
                                agent.desireProcessing.nextTry = "ccw";
                            }
                        }
                    }
                    nextAction = new Action("rotate", new Identifier(agent.desireProcessing.nextTry));
                } else { 
                    /**if(agent.desireProcessing.nextTryDir == 1) {
                        agent.desireProcessing.nextTryDir = -1;
                    } else {
                        agent.desireProcessing.nextTryDir = 1;
                    }
                    direction = DirectionUtil.intToString(DirectionUtil.stringToInt(direction) + agent.desireProcessing.nextTryDir);*/
                    agent.desireProcessing.failedPath = 0;
                    nextAction = new Action("move", new Identifier(direction));
                }
            } else {
                agent.desireProcessing.failedPath = 0;
                nextAction = new Action("move", new Identifier(direction));
            }

        } else if (neighbour.type.equals(Thing.TYPE_OBSTACLE) 
                || (neighbour.type.equals(Thing.TYPE_BLOCK )
                        && !agent.desireProcessing.attachedThings.contains(neighbour))) {
            Point pointCell = DirectionUtil.getCellInDirection(direction);
            // ein Hindernis wegräumen
            agent.desireProcessing.failedPath = 0;
            nextAction = new Action("clear", new Identifier(String.valueOf(pointCell.x)),
                    new Identifier(String.valueOf(pointCell.y)));

        } else if (neighbour.type.equals(Thing.TYPE_ENTITY)) {
            direction = DirectionUtil.intToString(DirectionUtil.stringToInt(direction) + 1);
            // einem Agenten oder einem Block ausweichen
            nextAction = agent.desireProcessing.getPossibleActionForMove(agent, direction);

        } else
            AgentLogger.info(Thread.currentThread().getName() + "  90 XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX ");

        AgentLogger.info(Thread.currentThread().getName() + "  getPossibleActionForMove() - Action: "
                + nextAction.getName() + " , " + direction);

        return nextAction;
    }
    
    public synchronized void addDesire(BdiAgentV2 agent, IDesire inDesire) {
        AgentLogger.info(Thread.currentThread().getName() + " addDesire() Agent: " + agent.getName() + " , Desire: " + inDesire.getName());
        agent.desires.add(inDesire);
    }
}

