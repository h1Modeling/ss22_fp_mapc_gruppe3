package de.feu.massim22.group3.agents;

//import java.awt.Point;
import java.util.*;

import javax.management.relation.RoleStatus;
import de.feu.massim22.group3.agents.Point;
import de.feu.massim22.group3.agents.AgentMeetings.Meeting;
import de.feu.massim22.group3.agents.Desires.BDesires.*;

import de.feu.massim22.group3.agents.Reachable.ReachableDispenser;
import de.feu.massim22.group3.agents.Reachable.ReachableGoalZone;
import de.feu.massim22.group3.agents.Reachable.ReachableRoleZone;
import de.feu.massim22.group3.utils.logging.AgentLogger;
import eis.iilang.Identifier;
import eis.iilang.Action;
import massim.protocol.data.TaskInfo;
import massim.protocol.data.Thing;
import massim.protocol.data.Role;
import massim.protocol.messages.scenario.Actions;
import massim.protocol.messages.scenario.ActionResults;

public class DesireUtilities {
	public StepUtilities stepUtilities;
	public TaskInfo task;
    public int maxTaskBlocks = 1;
    public String directionCircle = "cw";
    public int directionCounter = 0;
    public int circleSize = 40;
    private String dir2;
    private boolean dir2Used = false;
    private int moveIteration = 0;
 
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
	public String lastWishDirection = null;
	
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

        if (doDecision(agent, new DigFreeDesire(agent.belief))) {
        } else
            AgentLogger.info(Thread.currentThread().getName() + " Desire not added - Agent: " + agent.getName()
            + " , DigFreeDesire");
        
        if (doDecision(agent, new FreedomDesire(agent.belief))) {
        } else
            AgentLogger.info(Thread.currentThread().getName() + " Desire not added - Agent: " + agent.getName()
            + " , FreedomDesire");
        
        if (doDecision(agent, new LocalExploreDesire(agent.belief, agent.supervisor.getName(), agent))) {
        } else
            AgentLogger.info(Thread.currentThread().getName() + " Desire not added - Agent: " + agent.getName()
            + " , LocalExploreDesire");
        
        agent.decisionsDone = true;
        return result;
    }
 
    boolean doDecision(BdiAgentV2 agent, IDesire inDesire) {
        boolean result = false;
      
        if (!inDesire.isFulfilled().value() 
                && !inDesire.isUnfulfillable().value() 
                && inDesire.isExecutable().value()) { // desire ist m??glich , hinzuf??gen
            inDesire.setOutputAction(inDesire.getNextActionInfo().value());
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
    public synchronized boolean runSupervisorDecisions(int step, Supervisor supervisor, StepUtilities stepUtilities) {
        this.stepUtilities = stepUtilities;
        boolean result = false;
        AgentLogger.info(Thread.currentThread().getName() + " runSupervisorDecisions() Start - Step: " + step
                + " , Supervisor: " + supervisor.getName() + " , Agents: " + supervisor.getAgents());

        BdiAgentV2 supervisorAgent = StepUtilities.getAgent(supervisor.getName());
        
        AgentLogger.info(Thread.currentThread().getName() + " runSupervisorDecisions() Dispenser: " + supervisorAgent.belief.getReachableDispensers());

        List<String> allGroupAgents = new ArrayList<>(supervisor.getAgents());
        List<String> freeGroupAgents = new ArrayList<>(allGroupAgents);
        List<String> busyGroupAgents = new ArrayList<>();

        // Schleife ??ber alle Tasks
        for (TaskInfo loopTask : supervisorAgent.belief.getTaskInfo()) {
        	//Task Deadline erreicht
        	if(taskReachedDeadline ( supervisorAgent, loopTask)) {
        		 continue;
        	}
            task = loopTask;
            AgentLogger.info(Thread.currentThread().getName() + " runSupervisorDecisions() Task: " + task.name
                    + " Agents: " + allGroupAgents + " freie Agents: " + freeGroupAgents);

            // TODO Mehrblock-Tasks
           if ( task.requirements.size() > maxTaskBlocks) {
               continue;
           }
           
            // ??ber alle Agenten einer Gruppe
            for (String agentStr : allGroupAgents) {
                BdiAgentV2 agent = StepUtilities.getAgent(agentStr); 
                
                AgentLogger.info(Thread.currentThread().getName() + " runSupervisorDecisions() Agent: " + agentStr + " , Pos: " + agent.belief.getPosition());
                AgentLogger.info(
                        Thread.currentThread().getName() + ".getNextAction() - Agent: " + agent.getName()
                                + " , lA: " + agent.belief.getLastAction() + " , lAR: " + agent.belief.getLastActionResult());
              
                agent.desireProcessing.attachedThings = new ArrayList<Thing>();
                agent.desireProcessing.task = task;
                agent.desireProcessing.attachedThings = agent.getAttachedThings();

                AgentLogger.info(Thread.currentThread().getName() + " runSupervisorDecisions - Agent: " + agentStr + " attachedThings: "
                        + agent.desireProcessing.attachedThings);
                AgentLogger.info(Thread.currentThread().getName() + " runSupervisorDecisions() - Agent: "
                        + agent.getName() + " , Things: " + agent.belief.getThings());
                AgentLogger.info(Thread.currentThread().getName() + " runSupervisorDecisions() - Agent: "
                        + agent.getName() + " , GoalZones: " + agent.belief.getGoalZones());
                AgentLogger.info(Thread.currentThread().getName() + " runSupervisorDecisions() - Agent: "
                        + agent.getName() + " , ReachableGoalZones: " + agent.belief.getReachableGoalZones());
                AgentLogger.info(Thread.currentThread().getName() + " runSupervisorDecisions() - Agent: "
                        + agent.getName() + " , nicht in Zone: " + !agent.belief.getGoalZones().contains(Point.zero()) 
                        + " , in Zone: "+ agent.belief.getGoalZones().contains(Point.zero()) + " , att. Size: "
                        + agent.desireProcessing.attachedThings.size());
                
                if (agent.blockAttached && agent.desireProcessing.attachedThings.size() > maxTaskBlocks 
                    && doDecision(agent, new LooseWeightDesire(agent.belief))) {
                } else
                    AgentLogger.info(Thread.currentThread().getName() + " Desire not added - Agent: " + agent.getName()
                            + " , LooseWeightDesire");
                
                if (agent.belief.getRole().name().equals("default") 
                    && doDecision(agent, new GoAdoptRoleDesire(agent.belief, agent, "worker"))) {
                } else 
                    AgentLogger.info(Thread.currentThread().getName() + " Desire not added - Agent: " + agent.getName()
                            + " , GoAdoptRoleDesire - worker");
                
                if (agent.desireProcessing.attachedThings.size() == 0
                && doDecision(agent, new GoAbandonedBlockDesire(agent, getTaskBlock(agent, task).type))) {
                } else
                    AgentLogger.info(Thread.currentThread().getName() + " Desire not added - Agent: " + agent.getName()
                            + " , AttachAbandonedBlockDesire");
                
                if (agent.desireProcessing.attachedThings.size() == 0
                    && doDecision(agent, new GoDispenserDesire(agent.belief, getTaskBlock(agent, task), supervisor.getName(), agent, stepUtilities))) {
                } else
                    AgentLogger.info(Thread.currentThread().getName() + " Desire not added - Agent: " + agent.getName()
                            + " , GoDispenserDesire");
                
                if (maxTaskBlocks > 1 && agent.blockAttached && agent.desireProcessing.attachedThings.size() == 1
                        && doDecision(agent, new HelpMultiBlocksDesire(agent.belief, task,agent))) {
                        } else
                        AgentLogger.info(Thread.currentThread().getName() + " Desire not added - Agent: " + agent.getName()
                                + " , HelpMultiBlocksDesire");

                if (agent.blockAttached && agent.desireProcessing.attachedThings.size() > 0 && !agent.belief.getGoalZones().contains(Point.zero()) 
                    && doDecision(agent, new GoGoalZoneDesire(agent.belief, agent))) {
                    } else
                    AgentLogger.info(Thread.currentThread().getName() + " Desire not added - Agent: " + agent.getName()
                            + " , GoGoalZoneDesire");

                if (agent.blockAttached && agent.desireProcessing.attachedThings.size() == 1 && agent.belief.getGoalZones().contains(Point.zero())
                    && doDecision(agent, new ArrangeBlocksDesire(agent.belief, task, agent))) {
                    } else
                    AgentLogger.info(Thread.currentThread().getName() + " Desire not added - Agent: " + agent.getName()
                            + " , ArrangeBlocksDesire");
                                
                if (maxTaskBlocks > 1 && agent.blockAttached && agent.desireProcessing.attachedThings.size() > 0 && agent.belief.getGoalZones().contains(Point.zero())
                        && doDecision(agent, new ArrangeMultiBlocksDesire(agent.belief, task))) {
                        } else
                        AgentLogger.info(Thread.currentThread().getName() + " Desire not added - Agent: " + agent.getName()
                                + " , ArrangeMultiBlocksDesire");

                if (agent.blockAttached && agent.desireProcessing.attachedThings.size() > 0 && agent.belief.getGoalZones().contains(Point.zero())
                    && doDecision(agent, new SubmitDesire(agent.belief, task))) {
                    } else {}
                    AgentLogger.info(Thread.currentThread().getName() + " Desire not added - Agent: " + agent.getName()
                            + " , SubmitDesire");
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
	// TODO sinnvolle Priorit??ten vergeben
    public int getPriority(IDesire desire) {
        int result = 0;

        switch (desire.getName()) {
        
        case "GoAdoptRoleDesire":
            if (desire.getOutputAction().getName().equals(Actions.SKIP))
                result = 10;
            else
                result = 1000;
            break;
        case "DigFreeDesire":
            result = 1500;
            break;
        case "FreedomDesire":
            result = 2000;
            break;
        case "LocalExploreDesire":
            result = 100;
            break;

            //Heinz Desires (nicht genutzt)
        case "ExploreDesire":
            result = 20;
            break;
        case "AttachSingleBlockFromDispenserDesire":
            result = 200;
            break;
        case "GoToGoalZoneDesire":
            result = 300;
            break;
        case "GetBlocksInOrderDesire":
            result = 400;
            break;
            //Heinz Desires (nicht genutzt) Ende 
            
        case "AttachAbandonedBlockDesire":
            if (desire.getOutputAction().getName().equals(Actions.ATTACH))
                result = 290;
            else
                result = 250;        
            break;
        case "GoDispenserDesire":
            if (desire.getOutputAction().getName().equals(Actions.ATTACH))
                result = 300;
                else if (desire.getOutputAction().getName().equals(Actions.REQUEST))
                    result = 280;
            else
                result = 250 - desire.getPriority();           
            break;
        case "GoGoalZoneDesire":
            if (desire.getOutputAction().getName().equals(Actions.SKIP))
                result = 10;
            else
                result = 400;        
            break;
        case "ArrangeBlocksDesire":
            if (desire.getOutputAction().getName().equals(Actions.SKIP))
                result = 10;
                else if (desire.getOutputAction().getName().equals(Actions.DETACH))
                    result = 450;
            else
                result = 500;        
            break;
        case "SubmitDesire":
            result = 600;
            break;
        case "LooseWeightDesire":
            result = 700;
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
        String startDirection = DirectionUtil.intToString(agent.exploreDirection);
        /*float random = new Random().nextFloat();
        if (random < 0.25) {
            startDirection = "n";
        } else if (random < 0.5) {
            startDirection = "e";
        } else if (random < 0.75) {
            startDirection = "w";
        } else {
            startDirection = "s";
        }*/
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
        Point cell = Point.castToPoint(DirectionUtil.getCellInDirection(direction));

        return getContent(agent, cell);
    }
    
    public Thing getContentInDirection(BdiAgentV2 agent, Point from, String direction) {
        Point cell = Point.castToPoint(DirectionUtil.getCellInDirection(from, direction));

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
          
    public boolean taskReachedDeadline (BdiAgentV2 agent,TaskInfo task) {
    	boolean result = false;
    	if (agent.belief.getStep() > task.deadline) {
    		//Task ist abgelaufen 
    		result = true;
    	}
    	return result;
    }
       
	public Thing getTaskBlock(BdiAgentV2 agent, TaskInfo task) {
		Thing result = task.requirements.get(0);
		// ein Block Task
		if (task.requirements.size() > 1) {
			for (Meeting meeting : AgentMeetings.find(agent)) {
				if (!meeting.agent2().getAttachedThings().isEmpty()) {
					for (Thing attachedThing : meeting.agent2().getAttachedThings()) {
						// Kenn ich einen Agenten mit get(0)?
						if (attachedThing.details.equals(task.requirements.get(0).type)) {
							result = task.requirements.get(1);
							break;
						}
					}
				}
			}
		}
		return result;
	}
	
    private ActionInfo getIteratedActionForMove(BdiAgentV2 agent, String dir, String desire) {
        moveIteration++;
        if (moveIteration < 4) {
            return getActionForMove(agent, dir, desire);
        }
        // TODO AGENT is STuck
        return ActionInfo.SKIP("Agent is Stuck");
    }
	
	public ActionInfo getActionForMoveWithAlternate(BdiAgentV2 agent, String dir, String dirAlt, String desire) {
        ActionInfo firstTry = getActionForMove(agent, dir, desire);
        String lastRotation = agent.belief.getLastActionParams().size() > 0 ? agent.belief.getLastActionParams().get(0) : "";
        
        if ((firstTry.value().getName().equals(Actions.MOVE) 
                && !firstTry.value().getParameters().get(0).toString().equals(dir)  
                && !firstTry.value().getParameters().get(0).toString().equals(dirAlt))
                || ((firstTry.value().getName().equals(Actions.ROTATE) 
                && (firstTry.value().getParameters().get(0).toString().equals("cw")
                && lastRotation.equals("ccw")) 
                || (firstTry.value().getParameters().get(0).toString().equals("ccw")
                && lastRotation.equals("cw"))))) {
            return getActionForMove(agent, dirAlt, desire);
        }
        
        return firstTry;
    }

    public ActionInfo getActionForMove(BdiAgentV2 agent, String dir, String dir2, String desire) {
        this.dir2 = dir2;
        dir2Used = true;
        ActionInfo out = getActionForMove(agent, dir, desire);
        dir2Used = false;
        return out;
    }
    
    public ActionInfo getActionForMove(BdiAgentV2 agent, String dir, String desire) {
        Point dirPoint = Point.castToPoint(DirectionUtil.getCellInDirection(dir));
        //Melinda 
        List<Point> attached = agent.getAttachedPoints();       
        //List<Point> attached = belief.getAttachedPoints();
        //Melinda Ende
        // Rotate attached
        for (Point p : attached) {
            Point testPoint = new Point(p.x + dirPoint.x, p.y + dirPoint.y);
            Thing t = agent.belief.getThingAt(testPoint);
            AgentLogger.info(Thread.currentThread().getName() + " getActionForMove - Direction: " + dir + " , Block attached: " + p + " , in Richtung: " + testPoint);
            if (!isFree(t) && !testPoint.equals(new Point(0, 0))) {
                // Can be rotated
                Thing cw = agent.belief.getThingCRotatedAt(p);
                Thing ccw = agent.belief.getThingCCRotatedAt(p);
                Point cwP = getCRotatedPoint(p);
                Point ccwP = getCCRotatedPoint(p);
                AgentLogger.info(Thread.currentThread().getName() + " getActionForMove - cw: " + cwP + " , ccw: " + ccwP);
                AgentLogger.info(Thread.currentThread().getName() + " getActionForMove - oppositeD: " + DirectionUtil.oppositeDirection(dir) + " , Cell: " + DirectionUtil.getCellInDirection(DirectionUtil.oppositeDirection(dir)));
                AgentLogger.info(Thread.currentThread().getName() + " getActionForMove - cw: " + cw + " , Free?: " + isFree(cw));
                String lastRotation = agent.belief.getLastActionParams().size() > 0 ? agent.belief.getLastActionParams().get(0) : "";
                
                if (DirectionUtil.getCellInDirection(DirectionUtil.oppositeDirection(dir)).equals(cwP)) {
                    AgentLogger.info(Thread.currentThread().getName() + " getActionForMove - if1");
                    if (isFree(cw) && !lastRotation.equals("ccw")) {
                        AgentLogger.info(Thread.currentThread().getName() + " getActionForMove - rcw");
                        return ActionInfo.ROTATE_CW(desire);
                    } else {
                        if (isFree(ccw) && !lastRotation.equals("cw")) {
                            AgentLogger.info(Thread.currentThread().getName() + " getActionForMove - rccw");
                            return ActionInfo.ROTATE_CCW(desire);
                        }
                    }
                } else {
                    AgentLogger.info(Thread.currentThread().getName() + " getActionForMove - else1");
                    if (isFree(ccw) && !lastRotation.equals("cw")) {
                        AgentLogger.info(Thread.currentThread().getName() + " getActionForMove - rccw");
                        return ActionInfo.ROTATE_CCW(desire);
                    } else {
                        if (isFree(cw) && !lastRotation.equals("ccw")) {
                            AgentLogger.info(Thread.currentThread().getName() + " getActionForMove - rcw");
                            return ActionInfo.ROTATE_CW(desire);
                        }
                    }                    
                }
                
                if (cw != null && cw.type.equals(Thing.TYPE_OBSTACLE) && !cwP.equals(dirPoint)) {
                    Point target = Point.castToPoint(DirectionUtil.rotateCW(p));
                    return ActionInfo.CLEAR(target, desire);
                }
                if (ccw != null && ccw.type.equals(Thing.TYPE_OBSTACLE) && !ccwP.equals(dirPoint)) {
                    Point target = Point.castToPoint(DirectionUtil.rotateCW(p));
                    return ActionInfo.CLEAR(target, desire);
                }
            }
        }
        // Test Agent
        Thing t = agent.belief.getThingAt(dirPoint);
        AgentLogger.info(Thread.currentThread().getName() + " getActionForMove - t: " + t);
        if (t != null && t.type.equals(Thing.TYPE_OBSTACLE)) {
            AgentLogger.info(Thread.currentThread().getName() + " getActionForMove - if2");
            return ActionInfo.CLEAR(dirPoint, desire);
        } else if (isFree(t) || attached.contains(dirPoint)) {
            AgentLogger.info(Thread.currentThread().getName() + " getActionForMove - if3");

            if (dir2Used)
                return ActionInfo.MOVE(dir, dir2, desire);
            else
                return ActionInfo.MOVE(dir, desire);
        } else if (t != null && (t.type.equals(Thing.TYPE_ENTITY)                
                || (t.type.equals(Thing.TYPE_BLOCK) && !attached.contains(dirPoint)))) {

            AgentLogger.info(Thread.currentThread().getName() + " getActionForMove - if4");
            // Try to move around agent
            boolean inDirection = true; // dir.equals("n") || dir.equals("e");
            String dir1 = inDirection ? getCRotatedDirection(dir) : getCCRotatedDirection(dir);
            String dir2 = inDirection ? getCCRotatedDirection(dir) : getCRotatedDirection(dir);
            Thing tDir1 = agent.belief.getThingAt(dir1);
            Thing tDir2 = agent.belief.getThingAt(dir2);

            if (isFree(tDir1) || isClearable(tDir1)) {
                return getIteratedActionForMove(agent, dir1, desire);
            }

            if (isFree(tDir2) || isClearable(tDir2)) {
                return getIteratedActionForMove(agent, dir2, desire);
            }
            return ActionInfo.SKIP("Agent is stuck");

        } else {
            return ActionInfo.SKIP("Agent is stuck");
        }
    }
    
    protected boolean isFree(Thing t) {
        return t == null || t.type.equals(Thing.TYPE_DISPENSER);
    }

    protected boolean isClearable(Thing t) {
        return t != null && (t.type.equals(Thing.TYPE_BLOCK) || t.type.equals(Thing.TYPE_OBSTACLE));
    }

    protected Point getCRotatedPoint(Point p) {
        return new Point(-p.y, p.x);
    }

    protected String getCRotatedDirection(String dir) {
        switch (dir) {
            case "n": return "e";
            case "e": return "s";
            case "s": return "w";
            default: return "n";
        }
    }

    protected String getCCRotatedDirection(String dir) {
        switch (dir) {
            case "n": return "w";
            case "e": return "n";
            case "s": return "e";
            default: return "s";
        }
    }

    protected String getDirectionFromPoint(Point p) {
        if (p.x == 0) {
            return p.y < 0 ? "n" : "s";
        }
        return p.x < 0 ? "w" : "e";
    }

    protected Point getPointFromDirection(String dir) {
        switch (dir) {
            case "n": return new Point(0, -1);
            case "e": return new Point(1, 0);
            case "s": return new Point(0, 1);
            default: return new Point(-1, 0);
        }
    }

    public Point getCCRotatedPoint(Point p) {
        return new Point(p.y, -p.x);
    }
}
