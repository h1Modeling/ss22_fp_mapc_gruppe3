package de.feu.massim22.group3.agents.desires.V2;

//import java.awt.Point;
import java.util.*;

import de.feu.massim22.group3.agents.*;
import de.feu.massim22.group3.agents.AgentMeetings.Meeting;
import de.feu.massim22.group3.agents.belief.Belief;
import de.feu.massim22.group3.agents.desires.*;
import de.feu.massim22.group3.agents.AgentCooperations.Cooperation;
import de.feu.massim22.group3.utils.DirectionUtil;
import de.feu.massim22.group3.utils.logging.AgentLogger;
import massim.protocol.data.TaskInfo;
import massim.protocol.data.Thing;
import massim.protocol.messages.scenario.Actions;

public class HelperMultiBlocksDesire extends BeliefDesire {

    private TaskInfo info;    
    private BdiAgentV2 agent;
    private Cooperation coop;
    
    private int distanceAgent;
    private Meeting nearestMeeting;
    private boolean onTarget;
    private int distanceNearestTarget;
    private Point target;
    private Point myBlock;
    private Point block2;
    private Thing block2Thing;
    private String dirBlock2;
    private List<Point> targets = new ArrayList<Point>();
    
    public HelperMultiBlocksDesire(Belief belief, TaskInfo info, BdiAgentV2 agent) {
        super(belief);
        AgentLogger.info(Thread.currentThread().getName() + " runSupervisorDecisions - Start HelperMultiBlocksDesire");
        this.info = info;
        this.agent = agent;
    }

    @Override
    public BooleanInfo isFulfilled() {
        AgentLogger.info(Thread.currentThread().getName() + " runSupervisorDecisions - HelperMultiBlocksDesire.isFulfilled");

        return new BooleanInfo(false, "");
    }

    @Override
    public BooleanInfo isExecutable() {
        AgentLogger.info(
                Thread.currentThread().getName() + " runSupervisorDecisions - HelperMultiBlocksDesire.isExecutable");
        onTarget = false;

        if (belief.getRole().actions().contains(Actions.DETACH) 
                && belief.getRole().actions().contains(Actions.ATTACH)
                && belief.getRole().actions().contains(Actions.CONNECT)) {
            // Mehr Block Task
            if (info.requirements.size() >= 2) {
                AgentLogger.info(Thread.currentThread().getName() + " runSupervisorDecisions - HelperMultiBlocksDesire.isExecutable - Mehr-Block-Task: " + info.name);
                // Die Blöcke für die Task sind vorhanden
                if (proofBlockStructure(info)) {                    
                    distanceNearestTarget = 1000;
                    nearestMeeting = AgentMeetings.get(agent, coop.master());
                    
                    if (nearestMeeting != null) {
                        distanceAgent = AgentMeetings.getDistance(nearestMeeting);
                    } else {
                        distanceAgent = Point.distance(Point.castToPoint(agent.getBelief().getPosition()), Point.castToPoint(coop.master().getBelief().getPosition()));
                        nearestMeeting = new Meeting(agent, Point.zero(), Point.zero(), Point.zero(), coop.master(), Point.zero(), Point.zero(), Point.zero());
                    }
                    
                    AgentLogger.info(Thread.currentThread().getName() + " runSupervisorDecisions - HelperMultiBlocksDesire.isExecutable - meeting: " 
                    + distanceAgent + " , " + nearestMeeting.toString());

                    if (distanceAgent <= 3 || agent.alwaysToTarget) {
                        // Agent1 hat Agent2 in connect-Entfernung
                        List<java.awt.Point> dirs = DirectionUtil.getCellsIn4Directions();

                        for (int i = 0; i < dirs.size(); i++) {
                            AgentLogger.info(Thread.currentThread().getName() + " runSupervisorDecisions - HelperMultiBlocksDesire.isExecutable - block2: " 
                                    + block2.toString() + " , " + Point.castToPoint(dirs.get(i)).toString() + " , " + i);
                            
                            Thing t = coop.master().getBelief()
                                    .getThingAt(new Point(new Point(block2).add(Point.castToPoint(dirs.get(i)))));
                            
                            AgentLogger.info(Thread.currentThread().getName() + " runSupervisorDecisions - HelperMultiBlocksDesire.isExecutable - block2: " 
                                    + block2.toString() + " , " + Point.castToPoint(dirs.get(i)).toString() + " , " + t + " , " 
                                    + AgentMeetings.getPositionAgent2(nearestMeeting).toString());
                            
                            //target = AgentMeetings.getPositionAgent2(nearestMeeting).add(block2).add(Point.castToPoint(dirs.get(i)));
                            //target = Point.castToPoint(coop.master().getBelief().getPosition()).translate2To1(nearestMeeting);  
                            target = Point.castToPoint(coop.master().getBelief().getPosition()); 
                            target = target.add(block2);
                            target = target.add(Point.castToPoint(dirs.get(i)));
                            
                            AgentLogger.info(Thread.currentThread().getName() + " runSupervisorDecisions - HelperMultiBlocksDesire.isExecutable - block2: " 
                                    + block2.toString() + " , " + Point.castToPoint(dirs.get(i)).toString() + " , " + target.toString());
                            
                            int distanceTarget = Point.distance(Point.castToPoint(agent.getBelief().getPosition()),target);

                            if (distanceTarget == 0) {
                                AgentLogger.info(Thread.currentThread().getName() + " runSupervisorDecisions - HelperMultiBlocksDesire.isExecutable - on Target ");
                                onTarget = true;
                                dirBlock2 = DirectionUtil.oppositeDirection(DirectionUtil.intToString(i));
                                break;
                            } else {
                                if (isFree(t)) {
                                    targets.add(new Point(dirs.get(i).x, dirs.get(i).y));
                                    
                                    if (distanceTarget < distanceNearestTarget) {
                                        distanceNearestTarget = distanceTarget;
                                    }
                                }
                            }
                        }
                    }
                    
                    return new BooleanInfo(true, "");
                } else
                    // Die Blöcke für die Task sind nicht vorhanden
                    return new BooleanInfo(false, "");
            }
        }

        return new BooleanInfo(false, "");
    }

    @Override
    public ActionInfo getNextActionInfo() {
        AgentLogger.info(
                Thread.currentThread().getName() + " runSupervisorDecisions - HelperMultiBlocksDesire.getNextActionInfo");
        agent.desireProcessing.tryLastWanted = true;
    
        if (onTarget) {     //Agent2 steht auf einer der Target-Positionen für den Connect
            AgentLogger.info(Thread.currentThread().getName() + " runSupervisorDecisions - HelperMultiBlocksDesire.getNextActionInfo - AA: " 
        + DirectionUtil.getDirectionForCell(myBlock) + " , " + dirBlock2);
            
            if (DirectionUtil.intToString(DirectionUtil.getDirectionForCell(myBlock)).equals(dirBlock2)) {
                //Block ist bereits an der richtigen Position
                AgentLogger.info(Thread.currentThread().getName() + " runSupervisorDecisions - HelperMultiBlocksDesire.getNextActionInfo - BB");
                
                if (coop.statusHelper().equals(Status.Connected) || coop.statusHelper().equals(Status.ReadyToDetach)) {                   
                    AgentCooperations.setStatusHelper(info, coop.helper(), Status.ReadyToDetach);                  
                    return ActionInfo.DETACH(DirectionUtil.intToString(DirectionUtil.getDirectionForCell(myBlock)), getName());
                    
                } else {
                    AgentCooperations.setStatusHelper(info, coop.helper(), Status.ReadyToConnect);
                    return ActionInfo.SKIP("1000 waiting for master to be ready to connect");
                }
            } else {
                //Block muss noch gedreht werden
                AgentLogger.info(Thread.currentThread().getName() + " runSupervisorDecisions - HelperMultiBlocksDesire.getNextActionInfo - CC");
                Point taskBlock = Point.castToPoint(DirectionUtil.getCellInDirection(dirBlock2));
                Point agentBlock = myBlock;
                
                AgentCooperations.setStatusHelper(info, nearestMeeting.agent1(), Status.Arranging);
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
                                Point target = Point.castToPoint(DirectionUtil.rotateCW(agentBlock));
                                return ActionInfo.CLEAR(target, getName());
                            }
                        }
                    }
                    if (clockDirection == "ccw") {
                        if (isFree(ccw)) {
                            return ActionInfo.ROTATE_CCW(getName());
                        } else {
                            if (ccw.type.equals(Thing.TYPE_OBSTACLE)) {
                                Point target = Point.castToPoint(DirectionUtil.rotateCCW(agentBlock));
                                return ActionInfo.CLEAR(target, getName());
                            }
                        }
                    }
                    return ActionInfo.SKIP("0011 problem arranging blocks");
                }
            }
        } else {
            AgentLogger.info(Thread.currentThread().getName() + " vor getStatusMaster - para: " + info.name + " , " + nearestMeeting.toString());
            
            if (distanceNearestTarget <= 3 || agent.alwaysToTarget) {
              //gehe zur Target-Position für den Connect
                AgentLogger.info(Thread.currentThread().getName() + " runSupervisorDecisions - HelperMultiBlocksDesire.getNextActionInfo - DD");
                String direction = DirectionUtil.getDirection(agent.getBelief().getPosition(), target);
                AgentCooperations.setStatusHelper(info, nearestMeeting.agent1(), Status.GoTarget);
                agent.alwaysToTarget = true; 
                String dirAlt = "";
                AgentLogger.info(Thread.currentThread().getName() + "HelperMultiBlocksDesire - nextActionDirectionManhatten: "
                        + direction + " , altdir: " + dirAlt);
                return agent.desireProcessing.getActionForMove(agent, direction, getName());
                
            } else {
             //gehe Richtung Agent 
                AgentLogger.info(Thread.currentThread().getName() + " runSupervisorDecisions - HelperMultiBlocksDesire.getNextActionInfo - EE");
                 Point posAgent2 = AgentMeetings.getPositionAgent2(nearestMeeting);
                 String direction = DirectionUtil.getDirection(agent.getBelief().getPosition(), posAgent2);
                 AgentCooperations.setStatusHelper(info, nearestMeeting.agent1(), Status.GoMaster);
                return this.agent.desireProcessing.getActionForMove(agent, direction, getName());
            }
        }
       //return ActionInfo.SKIP(getName());  
    }
    
    public boolean proofBlockStructure(TaskInfo task) {
        AgentLogger.info(Thread.currentThread().getName() + " runSupervisorDecisions - proofBlockStructure");
        boolean result = false;
        boolean found = false;
        List<Thing> list = agent.desireProcessing.getTaskReqsOrdered(task);
        if (list.size() > 1) {
            block2 = new Point(list.get(1).x, list.get(1).y);
            block2Thing = list.get(1);
        }
        
        if (agent.isBusy && AgentCooperations.exists(task, agent, 2)) {
            // Agent ist als helper in einer cooperation
            this.coop = AgentCooperations.get(task, agent, 2);
            result = true;
            AgentLogger.info(Thread.currentThread().getName()
                    + " runSupervisorDecisions - HelperMultiBlocksDesire.getNextActionInfo - proofBlockStructure - coop: "
                    + AgentCooperations.toString(coop));
            
            for (Thing attachedThing : agent.getAttachedThings()) {
                // ich habe einen passenden 2.Block
                if (block2Thing.type.equals(attachedThing.details)) {
                    found = true;
                    myBlock = new Point(attachedThing.x, attachedThing.y);
                    break;
                }
            }
            
            if (!found) {
                // trotz Cooperation kein passender 2.Block (sollte nicht passieren, kommt aber leider vor)
                result = false;
                AgentCooperations.remove(coop);
            }
        }
        
        return result;
    }
    
    private boolean existsCommonEdge(Point p2) {
        for (java.awt.Point p1 : DirectionUtil.getCellsIn4Directions()) {
            if ((Math.abs(p2.x - p1.x) == 0 && Math.abs(p2.y - p1.y) == 1)
                    ||
                    (Math.abs(p2.y - p1.y) == 0 && Math.abs(p2.x - p1.x) == 1)) {
                    return true;     
                }  
        }

        return false;
    }
}

