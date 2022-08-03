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

public class HelpMultiBlocksDesire extends BeliefDesire {

    private TaskInfo info;    
    private BdiAgentV2 agent;
    private Cooperation coop;
    private int distanceMaster;
    
    private TreeMap<Integer, Meeting> foundMeetings = new TreeMap<>(); 
    private int distanceAgent;
    private Meeting nearestMeeting;
    private boolean onTarget;
    private int distanceNearestTarget;
    private int nearestTarget;
    private Point target;
    private Point myBlock;
    private Point block1;
    private Point block2;
    private String dirBlock2;
    private List<Point> targets = new ArrayList<Point>();
    
    public HelpMultiBlocksDesire(Belief belief, TaskInfo info, BdiAgentV2 agent) {
        super(belief);
        AgentLogger.info(Thread.currentThread().getName() + " runSupervisorDecisions - Start HelpMultiBlocksDesire");
        this.info = info;
        this.agent = agent;
    }

    @Override
    public BooleanInfo isFulfilled() {
        AgentLogger.info(Thread.currentThread().getName() + " runSupervisorDecisions - HelpMultiBlocksDesire.isFulfilled");

        return new BooleanInfo(false, "");
    }

    @Override
    public BooleanInfo isExecutable() {
        AgentLogger.info(
                Thread.currentThread().getName() + " runSupervisorDecisions - HelpMultiBlocksDesire.isExecutable");
        onTarget = false;

        if (belief.getRole().actions().contains(Actions.DETACH) 
                && belief.getRole().actions().contains(Actions.ATTACH)
                && belief.getRole().actions().contains(Actions.CONNECT)) {
            // Mehr Block Task
            if (info.requirements.size() >= 2) {
                AgentLogger.info(Thread.currentThread().getName() + " runSupervisorDecisions - HelpMultiBlocksDesire.isExecutable - Mehr-Block-Task: " + info.name);
                // Die Blöcke für die Task sind vorhanden
                if (proofBlockStructure(info)) {                    
                    distanceNearestTarget = 1000;
                    nearestTarget = 0; 
                    //TODO agents kennen sich nur indirekt
                    nearestMeeting = AgentMeetings.get(agent, coop.master());
                    
                    if (nearestMeeting != null) {
                        distanceAgent = AgentMeetings.getDistance(nearestMeeting);
                    } else {
                        distanceAgent = Point.distance(Point.castToPoint(agent.belief.getPosition()), Point.castToPoint(coop.master().belief.getPosition()));
                        nearestMeeting = new Meeting(agent, Point.zero(), Point.zero(), Point.zero(), coop.master(), Point.zero(), Point.zero(), Point.zero());
                    }
                    
                    AgentLogger.info(Thread.currentThread().getName() + " runSupervisorDecisions - HelpMultiBlocksDesire.isExecutable - meeting: " 
                    + distanceAgent + " , " + nearestMeeting.toString());

                    if (distanceAgent <= 3 || agent.alwaysToTarget) {
                        // Agent1 hat Agent2 in connect-Entfernung
                        ArrayList<java.awt.Point> dirs = DirectionUtil.getCellsIn4Directions();

                        for (int i = 0; i < dirs.size(); i++) {
                            AgentLogger.info(Thread.currentThread().getName() + " runSupervisorDecisions - HelpMultiBlocksDesire.isExecutable - block2: " 
                                    + block2.toString() + " , " + Point.castToPoint(dirs.get(i)).toString() + " , " + i);
                            
                            Thing t = coop.master().belief
                                    .getThingAt(new Point(new Point(block2).add(Point.castToPoint(dirs.get(i)))));
                            
                            AgentLogger.info(Thread.currentThread().getName() + " runSupervisorDecisions - HelpMultiBlocksDesire.isExecutable - block2: " 
                                    + block2.toString() + " , " + Point.castToPoint(dirs.get(i)).toString() + " , " + t + " , " 
                                    + AgentMeetings.getPositionAgent2(nearestMeeting).toString());
                            
                            //target = AgentMeetings.getPositionAgent2(nearestMeeting).add(block2).add(Point.castToPoint(dirs.get(i)));
                            //target = Point.castToPoint(coop.master().belief.getPosition()).translate2To1(nearestMeeting);  
                            target = Point.castToPoint(coop.master().belief.getPosition()); 
                            target = target.add(block2);
                            target = target.add(Point.castToPoint(dirs.get(i)));
                            
                            AgentLogger.info(Thread.currentThread().getName() + " runSupervisorDecisions - HelpMultiBlocksDesire.isExecutable - block2: " 
                                    + block2.toString() + " , " + Point.castToPoint(dirs.get(i)).toString() + " , " + target.toString());
                            
                            int distanceTarget = Point.distance(Point.castToPoint(agent.belief.getPosition()),target);

                            if (distanceTarget == 0) {
                                AgentLogger.info(Thread.currentThread().getName() + " runSupervisorDecisions - HelpMultiBlocksDesire.isExecutable - on Target ");
                                onTarget = true;
                                dirBlock2 = DirectionUtil.oppositeDirection(DirectionUtil.intToString(i));
                                break;
                            } else {
                                if (isFree(t)) {
                                    targets.add(new Point(dirs.get(i).x, dirs.get(i).y));
                                    
                                    if (distanceTarget < distanceNearestTarget) {
                                        distanceNearestTarget = distanceTarget;
                                        nearestTarget = i;
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
                Thread.currentThread().getName() + " runSupervisorDecisions - HelpMultiBlocksDesire.getNextActionInfo");
    
        if (onTarget) {     //Agent2 steht auf einer der Target-Positionen für den Connect
            AgentLogger.info(Thread.currentThread().getName() + " runSupervisorDecisions - HelpMultiBlocksDesire.getNextActionInfo - AA: " 
        + DirectionUtil.getDirectionForCell(myBlock) + " , " + dirBlock2);
            
            if (DirectionUtil.intToString(DirectionUtil.getDirectionForCell(myBlock)).equals(dirBlock2)) {
                //Block ist bereits an der richtigen Position
                AgentLogger.info(Thread.currentThread().getName() + " runSupervisorDecisions - HelpMultiBlocksDesire.getNextActionInfo - BB");
                
                if (coop.statusHelper().equals(Status.Connected) || coop.statusHelper().equals(Status.ReadyToDetach)) {                   
                    AgentCooperations.setStatusHelper(info, coop.helper(), Status.ReadyToDetach);                  
                    return ActionInfo.DETACH(DirectionUtil.intToString(DirectionUtil.getDirectionForCell(myBlock)), getName());
                    
                } else {
                    AgentCooperations.setStatusHelper(info, coop.helper(), Status.ReadyToConnect);
                    return ActionInfo.SKIP("1000 waiting for master to be ready to connect");
                }
            } else {
                //Block muss noch gedreht werden
                AgentLogger.info(Thread.currentThread().getName() + " runSupervisorDecisions - HelpMultiBlocksDesire.getNextActionInfo - CC");
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
                    return ActionInfo.SKIP("0010 problem arranging blocks");
                }
            }
        } else {
            AgentLogger.info(Thread.currentThread().getName() + " vor getStatusMaster - para: " + info.name + " , " + nearestMeeting.toString());
            
            if (distanceNearestTarget <= 3 || agent.alwaysToTarget) {
              //gehe zur Target-Position für den Connect
                AgentLogger.info(Thread.currentThread().getName() + " runSupervisorDecisions - HelpMultiBlocksDesire.getNextActionInfo - DD");
                String direction = DirectionUtil.getDirection(agent.belief.getPosition(), target);
                AgentCooperations.setStatusHelper(info, nearestMeeting.agent1(), Status.GoTarget);
                agent.alwaysToTarget = true; 
                String dirAlt = "";
                AgentLogger.info(Thread.currentThread().getName() + "HelpMultiBlocksDesire - nextActionDirectionManhatten: "
                        + direction + " , altdir: " + dirAlt);
                return agent.desireProcessing.getActionForMove(agent, direction, getName());

                /*for (int i = 1; i < targets.size(); i++) {
                    dirAlt = getDirectionToRelativePoint(targets.get(i));

                    if (!dirAlt.equals(direction)) {
                        break;
                    }
                } 
                
                if (dirAlt.equals(""))
                    return agent.desireProcessing.getActionForMove(agent, direction, getName());
                else
                    return agent.desireProcessing.getActionForMoveWithAlternate(agent, direction, "s", getName());*/
                
            } else {
             //gehe Richtung Agent 
                AgentLogger.info(Thread.currentThread().getName() + " runSupervisorDecisions - HelpMultiBlocksDesire.getNextActionInfo - EE");
                 Point posAgent2 = AgentMeetings.getPositionAgent2(nearestMeeting);
                 String direction = DirectionUtil.getDirection(agent.belief.getPosition(), posAgent2);
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
        int indexFound = 0;
        
        if (agent.isBusy && AgentCooperations.exists(task, agent, 2)) {
            // Agent ist als helper in einer cooperation
            this.coop = AgentCooperations.get(task, agent, 2);
            result = true;
            AgentLogger.info(Thread.currentThread().getName()
                    + " runSupervisorDecisions - HelpMultiBlocksDesire.getNextActionInfo - proofBlockStructure - coop: "
                    + AgentCooperations.toString(coop));
            
            for (Thing attachedThing : agent.getAttachedThings()) {
                // ich habe einen passenden 2.Block
                for (int i = 0; i < task.requirements.size(); i++) {
                    AgentLogger.info(Thread.currentThread().getName()
                            + " runSupervisorDecisions - HelpMultiBlocksDesire.getNextActionInfo - proofBlockStructure - attached: "
                            + attachedThing + " , task: " + task.requirements.get(i));
                    
                    if (task.requirements.get(i).type.equals(attachedThing.details)
                            && !DirectionUtil.getCellsIn4Directions().contains(new java.awt.Point (task.requirements.get(i).x, task.requirements.get(i).y))
                            && existsCommonEdge(new Point(task.requirements.get(i).x, task.requirements.get(i).y))) {
                        found = true;
                        myBlock = new Point(attachedThing.x, attachedThing.y);
                        block2 = new Point(task.requirements.get(i).x, task.requirements.get(i).y);
                        break;
                    }
                }

                if (found)
                    break;
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

