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

public class Help2MultiBlocksDesire extends BeliefDesire {

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
    private Point block2;
    private Point block3;
    private String dirblock3;
    
    public Help2MultiBlocksDesire(Belief belief, TaskInfo info, BdiAgentV2 agent) {
        super(belief);
        AgentLogger.info(Thread.currentThread().getName() + " runSupervisorDecisions - Start Help2MultiBlocksDesire");
        this.info = info;
        this.agent = agent;
    }

    @Override
    public BooleanInfo isFulfilled() {
        AgentLogger.info(Thread.currentThread().getName() + " runSupervisorDecisions - Help2MultiBlocksDesire.isFulfilled");

        return new BooleanInfo(false, "");
    }

    @Override
    public BooleanInfo isExecutable() {
        AgentLogger.info(
                Thread.currentThread().getName() + " runSupervisorDecisions - Help2MultiBlocksDesire.isExecutable");
        onTarget = false;

        if (belief.getRole().actions().contains(Actions.DETACH) 
                && belief.getRole().actions().contains(Actions.ATTACH)
                && belief.getRole().actions().contains(Actions.CONNECT)) {
            // 3 Block Task
            if (info.requirements.size() == 3) {
                AgentLogger.info(Thread.currentThread().getName() + " runSupervisorDecisions - Help2MultiBlocksDesire.isExecutable - 3-Block-Task: " + info.name);
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
                    
                    AgentLogger.info(Thread.currentThread().getName() + " runSupervisorDecisions - Help2MultiBlocksDesire.isExecutable - meeting: " 
                    + distanceAgent + " , " + nearestMeeting.toString());

                    if (distanceAgent <= 4 || agent.alwaysToTarget) {
                        // Agent1 hat Agent2 in connect-Entfernung
                        ArrayList<java.awt.Point> dirs = DirectionUtil.getCellsIn4Directions();

                        for (int i = 0; i < dirs.size(); i++) {
                            AgentLogger.info(Thread.currentThread().getName() + " runSupervisorDecisions - Help2MultiBlocksDesire.isExecutable - block3: " 
                                    + block3.toString() + " , " + Point.castToPoint(dirs.get(i)).toString() + " , " + i);
                            
                            Thing t = coop.master().belief
                                    .getThingAt(new Point(new Point(block3).add(Point.castToPoint(dirs.get(i)))));
                            
                            AgentLogger.info(Thread.currentThread().getName() + " runSupervisorDecisions - Help2MultiBlocksDesire.isExecutable - block3: " 
                                    + block3.toString() + " , " + Point.castToPoint(dirs.get(i)).toString() + " , " + t + " , " 
                                    + AgentMeetings.getPositionAgent2(nearestMeeting).toString());
                            
                            //target = AgentMeetings.getPositionAgent2(nearestMeeting).add(block3).add(Point.castToPoint(dirs.get(i)));
                            //target = Point.castToPoint(coop.master().belief.getPosition()).translate2To1(nearestMeeting);  
                            target = Point.castToPoint(coop.master().belief.getPosition()); 
                            target = target.add(block3);
                            target = target.add(Point.castToPoint(dirs.get(i)));
                            
                            AgentLogger.info(Thread.currentThread().getName() + " runSupervisorDecisions - Help2MultiBlocksDesire.isExecutable - block3: " 
                                    + block3.toString() + " , " + Point.castToPoint(dirs.get(i)).toString() + " , " + target.toString());
                            
                            int distanceTarget = Point.distance(Point.castToPoint(agent.belief.getPosition()),target);

                            if (distanceTarget == 0) {
                                AgentLogger.info(Thread.currentThread().getName() + " runSupervisorDecisions - Help2MultiBlocksDesire.isExecutable - on Target ");
                                onTarget = true;
                                dirblock3 = DirectionUtil.oppositeDirection(DirectionUtil.intToString(i));
                                break;
                            } else {
                                if (isFree(t) && distanceTarget < distanceNearestTarget) {
                                    distanceNearestTarget = distanceTarget;
                                    nearestTarget = i;
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
                Thread.currentThread().getName() + " runSupervisorDecisions - Help2MultiBlocksDesire.getNextActionInfo");
    
        if (onTarget) {     //Agent2 steht auf einer der Target-Positionen für den Connect
            AgentLogger.info(Thread.currentThread().getName() + " runSupervisorDecisions - Help2MultiBlocksDesire.getNextActionInfo - AA: " 
        + DirectionUtil.getDirectionForCell(myBlock) + " , " + dirblock3);
            
            if (DirectionUtil.intToString(DirectionUtil.getDirectionForCell(myBlock)).equals(dirblock3)) {
                //Block ist bereits an der richtigen Position
                AgentLogger.info(Thread.currentThread().getName() + " runSupervisorDecisions - Help2MultiBlocksDesire.getNextActionInfo - BB");
                
                if (coop.statusHelper2().equals(Status.Connected) || coop.statusHelper2().equals(Status.ReadyToDetach)) {                   
                    AgentCooperations.setStatusHelper2(info, coop.helper2(), Status.ReadyToDetach);                  
                    return ActionInfo.DETACH(DirectionUtil.intToString(DirectionUtil.getDirectionForCell(myBlock)), getName());
                    
                } else {
                    AgentCooperations.setStatusHelper2(info, coop.helper(), Status.ReadyToConnect);
                    return ActionInfo.SKIP("1000 waiting for master to be ready to connect");
                }
            } else {
                //Block muss noch gedreht werden
                AgentLogger.info(Thread.currentThread().getName() + " runSupervisorDecisions - Help2MultiBlocksDesire.getNextActionInfo - CC");
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
            AgentLogger.info(Thread.currentThread().getName() + " vor getStatusHelper - para: " + info.name + " , " + nearestMeeting.toString());
            
            if (distanceNearestTarget <= 3 || agent.alwaysToTarget) {
              //gehe zur Target-Position für den Connect
                AgentLogger.info(Thread.currentThread().getName() + " runSupervisorDecisions - Help2MultiBlocksDesire.getNextActionInfo - DD");
                String direction = DirectionUtil.getDirection(agent.belief.getPosition(), target);
                AgentCooperations.setStatusHelper2(info, nearestMeeting.agent1(), Status.GoTarget);
                agent.alwaysToTarget = true;
                return this.agent.desireProcessing.getActionForMove(agent, direction, getName());
                
            } else {
             //gehe Richtung Agent 
                AgentLogger.info(Thread.currentThread().getName() + " runSupervisorDecisions - Help2MultiBlocksDesire.getNextActionInfo - EE");
                 Point posAgent2 = AgentMeetings.getPositionAgent2(nearestMeeting);
                 String direction = DirectionUtil.getDirection(agent.belief.getPosition(), posAgent2);
                 AgentCooperations.setStatusHelper2(info, nearestMeeting.agent1(), Status.GoMaster);
                return this.agent.desireProcessing.getActionForMove(agent, direction, getName());
            }
        }
       //return ActionInfo.SKIP(getName());  
    }
    
    public boolean proofBlockStructure(TaskInfo task) {
        AgentLogger.info(Thread.currentThread().getName() + " runSupervisorDecisions - proofBlockStructure");
        boolean result = false;
        boolean found = false;
        
        if (AgentCooperations.exists(task, agent, 3)) {
            // Agent ist als helper2 in einer cooperation
            this.coop = AgentCooperations.get(task, agent, 3);
            result = true;
            AgentLogger.info(Thread.currentThread().getName()
                    + " runSupervisorDecisions - Help2MultiBlocksDesire.getNextActionInfo - proofBlockStructure - coop: "
                    + AgentCooperations.toString(coop));
            
            for (Thing attachedThing : agent.getAttachedThings()) {
                // was ist der 2.Block
                for (int i = 0; i < task.requirements.size(); i++) {
                    AgentLogger.info(Thread.currentThread().getName()
                            + " runSupervisorDecisions - Help2MultiBlocksDesire.getNextActionInfo - proofBlockStructure - attached: "
                            + attachedThing + " , task: " + task.requirements.get(i));
                    
                    if (task.requirements.get(i).type.equals(attachedThing.details)
                            && !DirectionUtil.getCellsIn4Directions().contains(new java.awt.Point (task.requirements.get(i).x, task.requirements.get(i).y))
                            && existsCommonEdge(new Point(task.requirements.get(i).x, task.requirements.get(i).y))) {
                        block2 = new Point(task.requirements.get(i).x, task.requirements.get(i).y);
                        break;
                    }
                }
                
                // ich habe einen passenden 3.Block
                for (int i = 0; i < task.requirements.size(); i++) {
                    AgentLogger.info(Thread.currentThread().getName()
                            + " runSupervisorDecisions - Help2MultiBlocksDesire.getNextActionInfo - proofBlockStructure - attached: "
                            + attachedThing + " , task: " + task.requirements.get(i));
                    
                    if (task.requirements.get(i).type.equals(attachedThing.details)
                            && !DirectionUtil.getCellsIn4Directions().contains(new java.awt.Point (task.requirements.get(i).x, task.requirements.get(i).y))
                            && !existsCommonEdge(new Point(task.requirements.get(i).x, task.requirements.get(i).y))) {
                        found = true;
                        myBlock = new Point(attachedThing.x, attachedThing.y);
                        block3 = new Point(task.requirements.get(i).x, task.requirements.get(i).y);
                        break;
                    }
                }

                if (found)
                    break;
            }
            
            if (!found) {
                // trotz Cooperation kein passender 3.Block (sollte nicht passieren, kommt aber leider vor)
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

