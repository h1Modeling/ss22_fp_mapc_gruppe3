package de.feu.massim22.group3.agents.desires.desiresV2;

//import java.awt.Point;
import java.util.*;

import de.feu.massim22.group3.agents.*;
import de.feu.massim22.group3.agents.desires.*;
import de.feu.massim22.group3.agents.utilsV2.*;
import de.feu.massim22.group3.agents.utilsV2.AgentCooperations.Cooperation;
import de.feu.massim22.group3.agents.utilsV2.AgentMeetings.Meeting;
import de.feu.massim22.group3.utils.DirectionUtil;
import de.feu.massim22.group3.utils.logging.AgentLogger;
import massim.protocol.data.TaskInfo;
import massim.protocol.data.Thing;
import massim.protocol.messages.scenario.Actions;

/**
 * The class <code>Helper2MultiBlocksDesire</code> models the desire of a agent who is the second helper ( has the third block) of a multi-block-task.
 * 
 * @author Melinda Betz
 */
public class Helper2MultiBlocksDesire extends BeliefDesire {

    private TaskInfo info;    
    private BdiAgentV2 agent;
    private Cooperation coop;
    
    private int distanceAgent;
    private Meeting nearestMeeting;
    private boolean onTarget;
    private int distanceNearestTarget;
    private Point target;
    private Point nearestTarget;
    private Point myBlock;
    private Point block3;
    private Thing block3Thing;
    private String dirblock3;
    
    /**
     * Initializes a new Helper2MultiBlocksDesire.
     * 
     * @param info - the info of the task
     * @param agent - the agent who is the second helper
     */
    public Helper2MultiBlocksDesire(TaskInfo info, BdiAgentV2 agent) {
        super(agent.getBelief());
        AgentLogger.info(Thread.currentThread().getName() + " runSupervisorDecisions - Start Helper2MultiBlocksDesire");
        this.info = info;
        this.agent = agent;
    }

    /**
     * Checks if the desire is fulfilled.
     * 
     * @return if it is fulfilled or not
     */
    @Override
    public BooleanInfo isFulfilled() {
        AgentLogger.info(Thread.currentThread().getName() + " runSupervisorDecisions - Helper2MultiBlocksDesire.isFulfilled");

        return new BooleanInfo(false, "");
    }

    /**
     * Checks if the desire is executable .
     * 
     * @return if it is executable or not
     */
    @Override
    public BooleanInfo isExecutable() {
        AgentLogger.info(
                Thread.currentThread().getName() + " runSupervisorDecisions - Helper2MultiBlocksDesire.isExecutable");
        onTarget = false;

        if (belief.getRole().actions().contains(Actions.DETACH) 
                && belief.getRole().actions().contains(Actions.ATTACH)
                && belief.getRole().actions().contains(Actions.CONNECT)) {
            // 3 block task
            if (info.requirements.size() == 3) {
                AgentLogger.info(Thread.currentThread().getName() + " runSupervisorDecisions - Helper2MultiBlocksDesire.isExecutable - 3-Block-Task: " + info.name);
                // all blocks for the task are available
                if (proofBlockStructure(info)) {                    
                    distanceNearestTarget = 1000;
                    nearestMeeting = AgentMeetings.get(agent, coop.master());
                    
                    if (nearestMeeting != null) {
                        distanceAgent = AgentMeetings.getDistance(nearestMeeting);
                    } else {
                        distanceAgent = Point.distance(Point.castToPoint(agent.getBelief().getPosition()), Point.castToPoint(coop.master().getBelief().getPosition()));
                        nearestMeeting = new Meeting(agent, Point.zero(), Point.zero(), Point.zero(), coop.master(), Point.zero(), Point.zero(), Point.zero());
                    }
                    
                    AgentLogger.info(Thread.currentThread().getName() + " runSupervisorDecisions - Helper2MultiBlocksDesire.isExecutable - meeting: " 
                    + distanceAgent + " , " + nearestMeeting.toString());

                    if (distanceAgent <= 4 || agent.alwaysToTarget) {
                        // agent1 and agent2 have a distance to one another that allows them to connect
                        List<java.awt.Point> dirs = DirectionUtil.getCellsIn4Directions();

                        for (int i = 0; i < dirs.size(); i++) {
                            AgentLogger.info(Thread.currentThread().getName() + " runSupervisorDecisions - Helper2MultiBlocksDesire.isExecutable - block3: " 
                                    + block3.toString() + " , " + Point.castToPoint(dirs.get(i)).toString() + " , " + i);
                            
                            Thing t = coop.master().getBelief()
                                    .getThingAt(new Point(new Point(block3).add(Point.castToPoint(dirs.get(i)))));
                            
                            AgentLogger.info(Thread.currentThread().getName() + " runSupervisorDecisions - Helper2MultiBlocksDesire.isExecutable - block3: " 
                                    + block3.toString() + " , " + Point.castToPoint(dirs.get(i)).toString() + " , " + t + " , " 
                                    + AgentMeetings.getPositionAgent2(nearestMeeting).toString());
                              
                            target = Point.castToPoint(coop.master().getBelief().getPosition()); 
                            target = target.add(block3);
                            target = target.add(Point.castToPoint(dirs.get(i)));
                            int distanceTarget = Point.distance(Point.castToPoint(agent.getBelief().getPosition()),target);
                            
                            AgentLogger.info(Thread.currentThread().getName() + " runSupervisorDecisions - Helper2MultiBlocksDesire.isExecutable - block3: " 
                                    + block3.toString() + " , " + Point.castToPoint(dirs.get(i)).toString() + " , " + target.toString() + " , " + distanceTarget);

                            if (distanceTarget == 0) {
                                AgentLogger.info(Thread.currentThread().getName() + " runSupervisorDecisions - Helper2MultiBlocksDesire.isExecutable - on Target ");
                                onTarget = true;
                                dirblock3 = DirectionUtil.oppositeDirection(DirectionUtil.intToString(i));
                                break;
                            } else {
                                if (isFree(t) && distanceTarget < distanceNearestTarget) {
                                    nearestTarget = target;
                                    distanceNearestTarget = distanceTarget;
                                }
                            }
                        }
                    }
                    
                    return new BooleanInfo(true, "");
                } else
                    // blocks for the task are not available
                    return new BooleanInfo(false, "");
            }
        }

        return new BooleanInfo(false, "");
    }

    /**
     * Gets the next action that has to be done .
     * 
     * @return the next action
     */
    @Override
    public ActionInfo getNextActionInfo() {
        AgentLogger.info(
                Thread.currentThread().getName() + " runSupervisorDecisions - Helper2MultiBlocksDesire.getNextActionInfo");
        agent.desireProcessing.tryLastWanted = true;
    
        if (onTarget) {     //agent2 is on a target position to do a  connect
            AgentLogger.info(Thread.currentThread().getName() + " runSupervisorDecisions - Helper2MultiBlocksDesire.getNextActionInfo - AA: " 
        + DirectionUtil.getDirectionForCell(myBlock) + " , " + dirblock3);
            
            if (DirectionUtil.intToString(DirectionUtil.getDirectionForCell(myBlock)).equals(dirblock3)) {
                //block is in the right position
                AgentLogger.info(Thread.currentThread().getName() + " runSupervisorDecisions - Helper2MultiBlocksDesire.getNextActionInfo - BB");
                
                if (coop.statusHelper2().equals(Status.Connected) || coop.statusHelper2().equals(Status.ReadyToDetach)) {                   
                    AgentCooperations.setStatusHelper2(info, coop.helper2(), Status.ReadyToDetach);                  
                    return ActionInfo.DETACH(DirectionUtil.intToString(DirectionUtil.getDirectionForCell(myBlock)), getName());
                    
                } else {
                    AgentCooperations.setStatusHelper2(info, coop.helper(), Status.ReadyToConnect);
                    return ActionInfo.SKIP("1000 waiting for master to be ready to connect");
                }
            } else {
                //block has to be rotated
                AgentLogger.info(Thread.currentThread().getName() + " runSupervisorDecisions - Helper2MultiBlocksDesire.getNextActionInfo - CC");
                Point taskBlock = Point.castToPoint(DirectionUtil.getCellInDirection(dirblock3));
                Point agentBlock = myBlock;
                
                AgentCooperations.setStatusHelper2(info, nearestMeeting.agent1(), Status.Arranging);
                String clockDirection = DirectionUtil.getClockDirection(agentBlock, taskBlock);

                if (clockDirection == "") {
                    return ActionInfo.SKIP("0010 problem arranging blocks");
                } else {
                    Thing cw = belief.getThingCRotatedAt(agentBlock);
                    Thing ccw = belief.getThingCCRotatedAt(agentBlock);

                    if (clockDirection == "cw") {
                        if (isFree(cw)) {
                            return ActionInfo.ROTATE_CW(getName());
                        } else {
                            if (cw.type.equals(Thing.TYPE_OBSTACLE)) {
                                Point t = Point.castToPoint(DirectionUtil.rotateCW(agentBlock));
                                return ActionInfo.CLEAR(t, getName());
                            }
                        }
                    }
                    if (clockDirection == "ccw") {
                        if (isFree(ccw)) {
                            return ActionInfo.ROTATE_CCW(getName());
                        } else {
                            if (ccw.type.equals(Thing.TYPE_OBSTACLE)) {
                                Point t = Point.castToPoint(DirectionUtil.rotateCCW(agentBlock));
                                return ActionInfo.CLEAR(t, getName());
                            }
                        }
                    }
                    return ActionInfo.SKIP("0010 problem arranging blocks");
                }
            }
        } else {
            AgentLogger.info(Thread.currentThread().getName() + " not on target - para: " + info.name + " , "
                    + nearestMeeting.toString());
            AgentLogger.info(Thread.currentThread().getName() + " not on target - dist: " + distanceNearestTarget + " , target: "
                    + nearestTarget);

            if (distanceNearestTarget <= 3 || agent.alwaysToTarget) {
                // go to target position for connect
                AgentLogger.info(Thread.currentThread().getName()
                        + " runSupervisorDecisions - Helper2MultiBlocksDesire.getNextActionInfo - DD");
                String direction = DirectionUtil.getDirection(agent.getBelief().getPosition(), nearestTarget);
                AgentCooperations.setStatusHelper2(info, nearestMeeting.agent1(), Status.GoTarget);
                agent.alwaysToTarget = true;
                return this.agent.desireProcessing.getActionForMove(agent, direction, getName());

            } else {
                if (distanceAgent <= 6 && (coop.statusHelper().equals(Status.New) 
                        || coop.statusHelper().equals(Status.GoMaster) 
                        || coop.statusHelper().equals(Status.GoTarget))) {
                    // wait for helper, so he can move to the needed position first
                    AgentLogger.info(Thread.currentThread().getName()
                            + " runSupervisorDecisions - Helper2MultiBlocksDesire.getNextActionInfo - EE");
                    AgentCooperations.setStatusHelper2(info, nearestMeeting.agent1(), Status.GoMaster);
                    return ActionInfo.SKIP("1000 waiting for helper to be on target");
                } else {
                    // go in direction of agent
                    AgentLogger.info(Thread.currentThread().getName()
                            + " runSupervisorDecisions - Helper2MultiBlocksDesire.getNextActionInfo - FF");
                    Point posAgent2 = AgentMeetings.getPositionAgent2(nearestMeeting);
                    String direction = DirectionUtil.getDirection(agent.getBelief().getPosition(), posAgent2);
                    AgentCooperations.setStatusHelper2(info, nearestMeeting.agent1(), Status.GoMaster);
                    return this.agent.desireProcessing.getActionForMove(agent, direction, getName());
                }
            }
        }
       //return ActionInfo.SKIP(getName());  
    }
    
    /**
     * Checks if the blocks for a certain task are in the right order.
     * 
     * @param task the task which blocks are being checked
     * 
     * @return if the blocks are in the right order or not
     */
    public boolean proofBlockStructure(TaskInfo task) {
        AgentLogger.info(Thread.currentThread().getName() + " runSupervisorDecisions - proofBlockStructure");
        boolean result = false;
        boolean found = false;
        List<Thing> list = agent.desireProcessing.getTaskReqsOrdered(task);
        
        if (list.size() > 2) {
            block3 = new Point(list.get(2).x, list.get(2).y);
            block3Thing = list.get(2);
        }
        
        if (AgentCooperations.exists(task, agent, 3)) {
            // agent is a helper2 in a existing agents cooperation
            this.coop = AgentCooperations.get(task, agent, 3);
            result = true;
            AgentLogger.info(Thread.currentThread().getName()
                    + " runSupervisorDecisions - Helper2MultiBlocksDesire.getNextActionInfo - proofBlockStructure - coop: "
                    + AgentCooperations.toString(coop));
            
            for (Thing attachedThing : agent.getAttachedThings()) {
                if (block3Thing.type.equals(attachedThing.details)) {
                    found = true;
                    myBlock = new Point(attachedThing.x, attachedThing.y);
                    break;
                }
            }
            
            if (!found) {
                // despite cooperation there is no (matching) third block ( should not happen, but sadly it does)
                result = false;
                AgentCooperations.remove(coop);
            }
        }
        
        return result;
    }
}

