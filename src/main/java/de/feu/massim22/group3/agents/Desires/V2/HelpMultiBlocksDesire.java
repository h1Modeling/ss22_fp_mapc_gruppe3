package de.feu.massim22.group3.agents.Desires.V2;

//import java.awt.Point;
import java.util.*;

import de.feu.massim22.group3.agents.*;
import de.feu.massim22.group3.agents.AgentMeetings.Meeting;
import de.feu.massim22.group3.agents.Desires.*;
import de.feu.massim22.group3.agents.AgentCooperations.Cooperation;
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
    private boolean blockStructureOk = false;    
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
    
    public HelpMultiBlocksDesire(Belief belief, TaskInfo info, BdiAgentV2 agent) {
        super(belief);
        AgentLogger.info(Thread.currentThread().getName() + " runAgentDecisionsWithTask - Start HelpMultiBlocksDesire");
        this.info = info;
        this.agent = agent;
    }

    @Override
    public BooleanInfo isFulfilled() {
        AgentLogger.info(Thread.currentThread().getName() + " runAgentDecisionsWithTask - HelpMultiBlocksDesire.isFulfilled");

        return new BooleanInfo(false, "");
    }

    @Override
    public BooleanInfo isExecutable() {
        AgentLogger.info(
                Thread.currentThread().getName() + " runAgentDecisionsWithTask - HelpMultiBlocksDesire.isExecutable");
        onTarget = false;

        if (belief.getRole().actions().contains(Actions.DETACH) 
                && belief.getRole().actions().contains(Actions.ATTACH)
                && belief.getRole().actions().contains(Actions.CONNECT)) {
            // Mehr Block Task
            if (info.requirements.size() == 2) {
                AgentLogger.info(Thread.currentThread().getName() + " runAgentDecisionsWithTask - HelpMultiBlocksDesire.isExecutable - 2-Block-Task: " + info.name);
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
                    
                    AgentLogger.info(Thread.currentThread().getName() + " runAgentDecisionsWithTask - HelpMultiBlocksDesire.isExecutable - meeting: " 
                    + distanceAgent + " , " + nearestMeeting.toString());

                    if (distanceAgent <= 3 || agent.alwaysToTarget) {
                        // Agent1 hat Agent2 in connect-Entfernung
                        ArrayList<java.awt.Point> dirs = DirectionUtil.getCellsIn4Directions();

                        for (int i = 0; i < dirs.size(); i++) {
                            AgentLogger.info(Thread.currentThread().getName() + " runAgentDecisionsWithTask - HelpMultiBlocksDesire.isExecutable - block2: " 
                                    + block2.toString() + " , " + Point.castToPoint(dirs.get(i)).toString() + " , " + i);
                            
                            Thing t = coop.master().belief
                                    .getThingAt(new Point(new Point(block2).add(Point.castToPoint(dirs.get(i)))));
                            
                            AgentLogger.info(Thread.currentThread().getName() + " runAgentDecisionsWithTask - HelpMultiBlocksDesire.isExecutable - block2: " 
                                    + block2.toString() + " , " + Point.castToPoint(dirs.get(i)).toString() + " , " + t + " , " 
                                    + AgentMeetings.getPositionAgent2(nearestMeeting).toString());
                            
                            //target = AgentMeetings.getPositionAgent2(nearestMeeting).add(block2).add(Point.castToPoint(dirs.get(i)));
                            //target = Point.castToPoint(coop.master().belief.getPosition()).translate2To1(nearestMeeting);  
                            target = Point.castToPoint(coop.master().belief.getPosition()); 
                            target = target.add(block2);
                            target = target.add(Point.castToPoint(dirs.get(i)));
                            
                            AgentLogger.info(Thread.currentThread().getName() + " runAgentDecisionsWithTask - HelpMultiBlocksDesire.isExecutable - block2: " 
                                    + block2.toString() + " , " + Point.castToPoint(dirs.get(i)).toString() + " , " + target.toString());
                            
                            int distanceTarget = Point.distance(Point.castToPoint(agent.belief.getPosition()),target);

                            if (distanceTarget == 0) {
                                AgentLogger.info(Thread.currentThread().getName() + " runAgentDecisionsWithTask - HelpMultiBlocksDesire.isExecutable - on Target ");
                                onTarget = true;
                                dirBlock2 = DirectionUtil.oppositeDirection(DirectionUtil.intToString(i));
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
                Thread.currentThread().getName() + " runAgentDecisionsWithTask - HelpMultiBlocksDesire.getNextActionInfo");
    
        if (onTarget) {     //Agent2 steht auf einer der Target-Positionen für den Connect
            AgentLogger.info(Thread.currentThread().getName() + " runAgentDecisionsWithTask - HelpMultiBlocksDesire.getNextActionInfo - AA: " 
        + DirectionUtil.getDirectionForCell(myBlock) + " , " + dirBlock2);
            
            if (DirectionUtil.intToString(DirectionUtil.getDirectionForCell(myBlock)).equals(dirBlock2)) {
                //Block ist bereits an der richtigen Position
                AgentLogger.info(Thread.currentThread().getName() + " runAgentDecisionsWithTask - HelpMultiBlocksDesire.getNextActionInfo - BB");
                
                if (coop.statusHelper().equals(Status.Connected) || coop.statusHelper().equals(Status.ReadyToDetach)) {
                    AgentCooperations.setCooperation(new AgentCooperations.Cooperation(info, coop.master(), 
                            AgentCooperations.getStatusMaster(info, coop.master(), coop.helper()), 
                            coop.helper(), Status.ReadyToDetach));
                    
                    return ActionInfo.DETACH(DirectionUtil.intToString(DirectionUtil.getDirectionForCell(myBlock)), getName());
                } else {
                    AgentCooperations.setCooperation(new AgentCooperations.Cooperation(info, coop.master(),
                            AgentCooperations.getStatusMaster(info, coop.master(), coop.helper()), coop.helper(),
                            Status.ReadyToConnect));

                    return ActionInfo.SKIP("1000 waiting for master to be ready to connect");
                }
            } else {
                //Block muss noch gedreht werden
                AgentLogger.info(Thread.currentThread().getName() + " runAgentDecisionsWithTask - HelpMultiBlocksDesire.getNextActionInfo - CC");
                Point taskBlock = Point.castToPoint(DirectionUtil.getCellInDirection(dirBlock2));
                Point agentBlock = myBlock;
               // Thing agentThing = agent.getAttachedThings().get(0);
                
               /* if (!agentThing.details.equals(info.requirements.get(0).type)) {
                    return ActionInfo.DETACH(DirectionUtil.intToString(DirectionUtil.getDirectionForCell(agentBlock)), getName());
                }*/
                
                AgentCooperations.setCooperation(new AgentCooperations.Cooperation(info, nearestMeeting.agent2(), 
                        AgentCooperations.getStatusMaster(info, nearestMeeting.agent2(), nearestMeeting.agent1()), 
                        nearestMeeting.agent1(), Status.Arranging));
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
                AgentLogger.info(Thread.currentThread().getName() + " runAgentDecisionsWithTask - HelpMultiBlocksDesire.getNextActionInfo - DD");
                String direction = DirectionUtil.getDirection(agent.belief.getPosition(), target);
                AgentCooperations.setCooperation(new AgentCooperations.Cooperation(info, nearestMeeting.agent2(), 
                        AgentCooperations.getStatusMaster(info, nearestMeeting.agent2(), nearestMeeting.agent1()), 
                        nearestMeeting.agent1(), Status.GoTarget));
                agent.alwaysToTarget = true;
                //return ActionInfo.MOVE(direction, getName());
                return this.agent.desireProcessing.getActionForMove(agent, direction, getName());
            } else {
             //gehe Richtung Agent 
                AgentLogger.info(Thread.currentThread().getName() + " runAgentDecisionsWithTask - HelpMultiBlocksDesire.getNextActionInfo - EE");
                 Point posAgent2 = AgentMeetings.getPositionAgent2(nearestMeeting);
                 String direction = DirectionUtil.getDirection(agent.belief.getPosition(), posAgent2);
                 AgentCooperations.setCooperation(new AgentCooperations.Cooperation(info, nearestMeeting.agent2(), 
                         AgentCooperations.getStatusMaster(info, nearestMeeting.agent2(), nearestMeeting.agent1()), 
                         nearestMeeting.agent1(), Status.GoMaster));
                //return ActionInfo.MOVE(direction, getName());
                return this.agent.desireProcessing.getActionForMove(agent, direction, getName());
            }
        }
       //return ActionInfo.SKIP(getName());  
    }
    
    public boolean proofBlockStructure(TaskInfo task) {
        AgentLogger.info(Thread.currentThread().getName() + " runAgentDecisionsWithTask - proofBlockStructure");
        boolean result = false;
        boolean found = false;
        int indexFound = 0;
        
        if (agent.isBusy && AgentCooperations.exists(task, agent, 2)) {
            // Agent ist als helper in einer cooperation
            this.coop = AgentCooperations.get(task, agent, 2);
            result = true;
            AgentLogger.info(Thread.currentThread().getName()
                    + " runAgentDecisionsWithTask - HelpMultiBlocksDesire.getNextActionInfo - proofBlockStructure - coop: "
                    + AgentCooperations.toString(coop));
            
            for (Thing attachedThing : agent.getAttachedThings()) {
                // ich habe einen passenden 2.Block
                for (int i = 0; i < task.requirements.size(); i++) {
                    AgentLogger.info(Thread.currentThread().getName()
                            + " runAgentDecisionsWithTask - HelpMultiBlocksDesire.getNextActionInfo - proofBlockStructure - attached: "
                            + attachedThing + " , task: " + task.requirements.get(i));
                    
                    if (task.requirements.get(i).type.equals(attachedThing.details)
                            && !DirectionUtil.getCellsIn4Directions().contains(new Point (task.requirements.get(i).x, task.requirements.get(i).y))) {
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
        
        blockStructureOk = result;
        return result;

        /*if (found) {
            for (Meeting meeting : AgentMeetings.find(agent)) {
                if (!agent.getAttachedThings().isEmpty()) {
                    for (Thing attachedThing : meeting.agent2().getAttachedThings()) {
                        // anderer Agent hat den dazu passenden 1.Block
                        for (int i = 0; i < task.requirements.size(); i++) {
                            if (task.requirements.get(i).type.equals(attachedThing.details)
                                    && (task.requirements.get(i).x == 0
                                    || task.requirements.get(i).y == 0)
                                    && (attachedThing.x == 0
                                    || attachedThing.y == 0)) {
                                result = true;
                                foundMeetings.put(AgentMeetings.getDistance(meeting), meeting);
                                block1 = new Point(task.requirements.get(i).x, task.requirements.get(i).y);
                                break;
                            }
                        }
                        
                        if (result) break;
                    }
                }
            }
        }*/
        
        /*if (found) {
            if (AgentCooperations.exists(info, agent)) {
                Cooperation coop = AgentCooperations.get(agent);

                if (coop.helper().equals(agent) && coop.task().equals(info)) {
                    result = true;
                    this.coop = coop;
                    AgentLogger.info(Thread.currentThread().getName()
                            + " runAgentDecisionsWithTask - ArrangeMultiBlocksDesire.getNextActionInfo - proofBlockStructure - coop: "
                            + AgentCooperations.toString(coop));
                }
            }
        }*/
        
        //AgentLogger.info(Thread.currentThread().getName() + " runAgentDecisionsWithTask - proofBlockStructure: " + found + " , " + result);

    }
}
