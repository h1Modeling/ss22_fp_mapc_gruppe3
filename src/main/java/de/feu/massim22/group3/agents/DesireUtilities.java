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
import massim.protocol.messages.scenario.ActionResults;

public class DesireUtilities {
	
	public TaskInfo task;
    public String directionCircle = "cw";
    public int directionCounter = 0;
    public int circleSize = 10;
 
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
        //doDecision(agent, new RemoveObstacle(agent));
        doDecision(agent, new LocalExplore(agent));
        
        agent.decisionsDone = true;
        return result;
    }
    
    boolean doDecision(BdiAgent agent, DesireIntegration inDesire) {
        boolean result = false;
      
        if (inDesire.isExecutable()) { // desire ist möglich , hinzufügen
            inDesire.setOutputAction(inDesire.getNextAction());
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

            // TODO Mehrblock-Tasks
           if ( task.requirements.size() > 1) {
               continue;
           }
           
            // über alle Agenten einer Gruppe
            for (String agentStr : allGroupAgents) {
                AgentLogger.info(Thread.currentThread().getName() + " runSupervisorDecisions() Agent: " + agentStr);
                BdiAgent agent = StepUtilities.getAgent(agentStr);
                agent.desireProcessing.attachedThings = new ArrayList<Thing>();
                agent.desireProcessing.task = task;

                // alle Blöcke die ein Agent hat
                attachedPoints = agent.belief.getAttachedThings();

                Set<Thing> things = agent.belief.getThings();
                for (Point p : attachedPoints) {
                    for (Thing t : things) {
                        if (t.type.equals(Thing.TYPE_BLOCK) && t.x == p.x && t.y == p.y) {
                            agent.desireProcessing.attachedThings.add(t);
                            break;
                        }
                    }
                }

                AgentLogger.info(Thread.currentThread().getName() + " runSupervisorDecisions - Agent: " + agentStr + " attachedThings: "
                        + agent.desireProcessing.attachedThings);
                AgentLogger.info(
                        Thread.currentThread().getName() + " runSupervisorDecisions() - Agent: " + agent.getName()
                                + " , lA: " + agent.belief.getLastAction() + " , lAR: " + agent.belief.getLastActionResult());

                // was hat der Agent für eine Rolle
                role = agent.getAgentBelief().getRole();
                // Agent hat default Rolle
                if (role.equals(" default") && doDecision(agent, new GoAdoptRole(agent, "worker"))) {
                    busyGroupAgents.add(agent.getName());
                } else {

                    agent.desireProcessing.goodBlocks = new ArrayList<Thing>();
                    agent.desireProcessing.badBlocks = new ArrayList<Thing>();
                    agent.desireProcessing.goodPositionBlocks = new ArrayList<Thing>();
                    agent.desireProcessing.badPositionBlocks = new ArrayList<Thing>();
                    agent.desireProcessing.missingBlocks = new ArrayList<Thing>();
                    agent.desireProcessing.typeOk = false;
                    agent.desireProcessing.analysisDone = false;
                    
                    // wenn ein Agent alle Blöcke einer Task besitzt
                    if ((new ArrangeBlocks(agent).isExecutable() || new GoSubmit(agent).isExecutable()) 
                            && agent.belief.getReachableGoalZones().size() == 0) {
                        // leider keine GoalZone in Sichtweite
                        AgentLogger.info(Thread.currentThread().getName() + " runSupervisorDecisions() vor Explore1");
                        if (doDecision(agent, new Explore(agent, 45))) {
                            busyGroupAgents.add(agent.getName());
                        }
                        // wenn ein Agent alle Blöcke einer Task an der richtigen Stelle besitzt
                    } else {
                        AgentLogger.info(Thread.currentThread().getName() + " runSupervisorDecisions() vor GoSubmit");
                        if (doDecision(agent, new GoSubmit(agent))) {
                            busyGroupAgents.add(agent.getName());

                            // wenn ein Agent alle Blöcke einer Task besitzt (aber an der falschen Stelle)
                        } else {
                                AgentLogger.info(Thread.currentThread().getName()
                                        + " runSupervisorDecisions() vor ArrangeBlocks");
                                if (doDecision(agent, new ArrangeBlocks(agent))) {
                                    busyGroupAgents.add(agent.getName());

                                    // wenn ein Agent nicht alle Blöcke einer Task besitzt
                                } else {
                                    String type = "";
                                    if (agent.desireProcessing.missingBlocks.size() > 0) {
                                        type = agent.desireProcessing.missingBlocks.get(0).details;
                                    } else {
                                        type = task.requirements.get(0).type;
                                    }
                                    AgentLogger.info(Thread.currentThread().getName()
                                            + " runSupervisorDecisions() vor GoDispenser");
                                    if (doDecision(agent, new GoDispenser(agent, type))) {
                                        busyGroupAgents.add(agent.getName());

                                        // wenn der gesuchte Dispenser nicht ereichbar ist
                                    } else {
                                        AgentLogger.info(Thread.currentThread().getName()
                                                + " runSupervisorDecisions() vor Explore");
                                        if (doDecision(agent, new Explore(agent))) {
                                            busyGroupAgents.add(agent.getName());
                                        }
                                    }
                                }
                        }
                    } // go submit
                } // adopt role
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
    public int getPriority(DesireIntegration desire) {
        int result = 0;

        switch (desire.getName()) {
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
            if (desire.getGroupOrder()) {
                result = 60 ;               
            } else {
                result = 60;            
            }
            break;
        case "ArrangeBlocks":
            if (desire.getNextAction().getName().equals("rotate")) {
                result = 40;
            } else {
                result = 100;
            }
            break;
        case "ReactToNorm":
            result = 100;
            break;
        case "GoGoalZone":
            result = 80;
            break;
        case "GoRoleZone":
            result = 90;
            break;
        case "GoDispenser":
        	if (desire.getGroupOrder()) {
                result = 50;        		
        	} else {
                result = 60;       		
        	}
            break;
        case "Explore":
            if (desire.getPriority() == 1000) {
                result = 55;
            } else {
                result = desire.getPriority();
            }
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
    public synchronized DesireIntegration determineIntention(BdiAgentV2 agent) {
        DesireIntegration result = null;
        int priority = 1000;
        
        for (DesireIntegration desire : agent.desires) {
            /*AgentLogger.info(Thread.currentThread().getName() + " determineIntention() - Agent: " + agent.getName()
                    + " , Desire: " + desire.getName() + " , Action: " + desire.getOutputAction() + " , Prio: " + desire.getPriority());*/
            if (desire.getPriority() < priority) {
                result = desire;
                priority = desire.getPriority();
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
    
    public Identifier walkCircles(BdiAgent agent, int stepWidth) {
        Identifier resultDirection = new Identifier("n");

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
    
    public Thing getContentInDirection(BdiAgent agent, String direction) {
        Point cell = DirectionUtil.getCellInDirection(direction);

        return getContent(agent, cell);
    }
    
    public Thing getContentInDirection(BdiAgent agent, Point from, String direction) {
        Point cell = DirectionUtil.getCellInDirection(from, direction);

        return getContent(agent, cell);
    }
    
    public Thing getContent(BdiAgent agent, Point cell) {      
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
    
    /*public Action getPossibleActionForMove(BdiAgent agent, String direction) {
        Action nextAction = null;       
        Thing neighbour = agent.desireProcessing.getContentInDirection(agent, direction);
        
        //AgentLogger.info(Thread.currentThread().getName() + " getPossibleActionForMove() - Neighbour: " + neighbour);

       
        if (neighbour == null || (neighbour.type.equals(Thing.TYPE_BLOCK)
                && agent.desireProcessing.attachedThings.contains(neighbour))) {
            // Weg ist frei
            if (agent.belief.getLastActionResult().equals(ActionResults.FAILED_PATH)) {
                direction = DirectionUtil.intToString(DirectionUtil.stringToInt(direction) - 1);
            }         
            nextAction = new Action("move", new Identifier(direction));
            
        } else if (neighbour.type.equals(Thing.TYPE_OBSTACLE)) {
            Point pointCell = DirectionUtil.getCellInDirection(direction);
            // ein Hindernis wegräumen
            nextAction = new Action("clear", new Identifier(String.valueOf(pointCell.x)),
                    new Identifier(String.valueOf(pointCell.y)));

        } else if (neighbour.type.equals(Thing.TYPE_ENTITY)) {
            direction = DirectionUtil.intToString(DirectionUtil.stringToInt(direction) + 1);          
            // einem Agenten ausweichen  
            nextAction = agent.desireProcessing.getPossibleActionForMove(agent, direction); 

        } else if (neighbour.type.equals(Thing.TYPE_BLOCK)) {
            direction = DirectionUtil.intToString(DirectionUtil.stringToInt(direction) + 1);
            // einem Block ausweichen 
            nextAction = agent.desireProcessing.getPossibleActionForMove(agent, direction); 
        }
        //AgentLogger.info(Thread.currentThread().getName() + " getPossibleActionForMove() - Action: " + nextAction.getName() + " , " + direction);

        return nextAction;
    }*/
    
    public int getWidth(BdiAgent agent, String side) {
        int maxWidth = 0;
        
        for (Point cell : agent.belief.getAttachedThings()) {
            if (side.equals("right")) {
                if (cell.x > maxWidth) {
                    maxWidth = maxWidth + cell.x;
                }         
            } else {
                if (cell.x < maxWidth) {
                    maxWidth = maxWidth + cell.x;
                }                
            }
            
            if (side.equals("left")) {
                maxWidth = Math.abs(maxWidth);
            }
        }
        
        return maxWidth;
    }
    
    public Action getPossibleActionForMove(BdiAgent agent, String direction) {
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
}

