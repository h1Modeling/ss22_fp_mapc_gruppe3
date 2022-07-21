package de.feu.massim22.group3.agents.Desires.BDesires.V2;

//import java.awt.Point;
import java.util.*;

import de.feu.massim22.group3.agents.*;
import de.feu.massim22.group3.agents.AgentMeetings.Meeting;
import de.feu.massim22.group3.agents.Desires.BDesires.*;
import de.feu.massim22.group3.agents.AgentCooperations.Cooperation;
import de.feu.massim22.group3.utils.logging.AgentLogger;
import eis.iilang.Action;
import eis.iilang.Identifier;
import massim.protocol.data.TaskInfo;
import massim.protocol.data.Thing;
import massim.protocol.messages.scenario.Actions;

public class ArrangeMultiBlocksDesire extends BeliefDesire {

    private TaskInfo info;    
    private BdiAgentV2 agent;
    private BdiAgentV2 possibleHelper;
    private Cooperation coop;
    private TreeMap<Integer, Meeting> foundMeetings = new TreeMap<>();
    
    public ArrangeMultiBlocksDesire(Belief belief, TaskInfo info, BdiAgentV2 agent) {
        super(belief);
        AgentLogger.info(Thread.currentThread().getName() + " runSupervisorDecisions - Start ArrangeMultiBlocksDesire");
        this.info = info;
        this.agent = agent;
    }

    @Override
    public BooleanInfo isFulfilled() {
        AgentLogger.info(Thread.currentThread().getName() + " runSupervisorDecisions - ArrangeMultiBlocksDesire.isFulfilled");
        for (Thing t : info.requirements) {
            Thing atAgent = belief.getThingAt(new Point(t.x, t.y));
            if (atAgent == null || !atAgent.type.equals(Thing.TYPE_BLOCK) || !atAgent.details.equals(t.type)) {             
                return new BooleanInfo(false, "");
            }
        }
        
        if (AgentCooperations.exists(info, agent, 1)) {
            AgentLogger.info(Thread.currentThread().getName() + " runSupervisorDecisions - proofBlockStructure - ist master");
         // Agent ist als master in einer cooperation 
            this.coop = AgentCooperations.get(info, agent, 1);
            
            if (!(coop.statusMaster().equals(Status.Connected) && coop.statusHelper().equals(Status.Detached))) {
                return new BooleanInfo(false, "");
            }
        }
        
        return new BooleanInfo(true, "");
    }

    @Override
    public BooleanInfo isExecutable() {
        AgentLogger
                .info(Thread.currentThread().getName() + " runSupervisorDecisions - ArrangeMultiBlocksDesire.isExecutable");
        if (belief.getRole().actions().contains(Actions.DETACH)
                && belief.getRole().actions().contains(Actions.ATTACH)
                && belief.getRole().actions().contains(Actions.CONNECT)) {
            //2 Block Tasks
            if(info.requirements.size() == 2) {
                return proofBlockStructure(info);
            }
        }
        return new BooleanInfo(false, "");
    }

    @Override
    public ActionInfo getNextActionInfo() {
        AgentLogger.info(Thread.currentThread().getName()
                + " runSupervisorDecisions - ArrangeMultiBlocksDesire.getNextActionInfo");
        //Cooperation coop = null;
        Point taskBlock = new Point(info.requirements.get(0).x, info.requirements.get(0).y);
        Point agentBlock = agent.getAttachedPoints().get(0);
        Thing agentThing = agent.getAttachedThings().get(0);

        AgentLogger.info(Thread.currentThread().getName()
                + " runSupervisorDecisions - ArrangeMultiBlocksDesire.getNextActionInfo agentBlocks: "
                + agent.getAttachedThings() + " , taskBlocks: " + info.requirements);

        /*
         * if (!agentThing.details.equals(info.requirements.get(0).type)) { return
         * ActionInfo.DETACH(DirectionUtil.intToString(DirectionUtil.getDirectionForCell
         * (agentBlock)), getName()); }
         */
        
        AgentLogger.info(Thread.currentThread().getName()
                + " runSupervisorDecisions - ArrangeMultiBlocksDesire.getNextActionInfo - coop: " + AgentCooperations.toString(coop));

        if (coop.statusMaster().equals(Status.Connected) && (coop.statusHelper().equals(Status.Connected) || coop.statusHelper().equals(Status.ReadyToDetach))) {  
            return ActionInfo.SKIP("1000 waiting for helper to detach");
        } else
        if (coop.statusMaster().equals(Status.ReadyToConnect)) {
            return ActionInfo.SKIP("1000 waiting for helper to be ready to connect");
        } else {
            if (taskBlock.equals(agentBlock)) {
                AgentLogger.info(Thread.currentThread().getName()
                        + " runSupervisorDecisions - ArrangeMultiBlocksDesire.getNextActionInfo - AA");
                AgentCooperations.setCooperation(new AgentCooperations.Cooperation(coop.task(), coop.master(),
                        Status.ReadyToConnect, coop.helper(), coop.statusHelper()));
                return ActionInfo.SKIP("1000 waiting for helper to be ready to connect");
            } else {
                AgentLogger.info(Thread.currentThread().getName()
                        + " runSupervisorDecisions - ArrangeMultiBlocksDesire.getNextActionInfo - BB");
                AgentCooperations.setCooperation(new AgentCooperations.Cooperation(coop.task(), coop.master(),
                        Status.Arranging, coop.helper(), coop.statusHelper()));
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
        }
    }
    
    public BooleanInfo proofBlockStructure(TaskInfo task) {
        AgentLogger.info(Thread.currentThread().getName() + " runSupervisorDecisions - proofBlockStructure");
        BooleanInfo result = new BooleanInfo(false, "");
        boolean found = false;
        int indexFound = 0;
        Point pointFound = null;
        
        AgentLogger.info(Thread.currentThread().getName()
                + " runSupervisorDecisions - proofBlockStructure - agent.isBusy: " + agent.isBusy + " , " + task.name);
        AgentLogger.info(Thread.currentThread().getName()
                + " runSupervisorDecisions - proofBlockStructure - coops: " + AgentCooperations.toString(AgentCooperations.cooperations));
        
        if (agent.isBusy && AgentCooperations.exists(task, agent, 1)) {
            AgentLogger.info(Thread.currentThread().getName() + " runSupervisorDecisions - proofBlockStructure - ist master");
         // Agent ist als master in einer cooperation 
            this.coop = AgentCooperations.get(task, agent, 1);
            result = new BooleanInfo(true, "");
            
        } else {
            if (!agent.isBusy) {
             // Agent ist weder als master noch als helper in einer cooperation
                for (Thing attachedThing : agent.getAttachedThings()) {
                    //Agent hat einen passenden Block
                    for (int i = 0; i < task.requirements.size(); i++) {
                        if (task.requirements.get(i).type.equals(attachedThing.details)
                                && ((task.requirements.get(i).x == 0 || task.requirements.get(i).y == 0)
                                        && task.requirements.get(i).x <= 1 && task.requirements.get(i).y <= 1)) {
                            found = true;
                            indexFound = i;
                            pointFound = new Point(task.requirements.get(i).x, task.requirements.get(i).y);
                            break;
                        }
                    }

                    if (found) break;
                }

                if (found) {
                    for (Meeting meeting : AgentMeetings.find(agent)) {
                        if ( !AgentCooperations.exists(meeting.agent2()) && !meeting.agent2().getAttachedThings().isEmpty()) {
                            for (Thing attachedThing2 : meeting.agent2().getAttachedThings()) {
                                // anderer Agent hat den Block der mir noch fehlt
                                for (int i = 0; i < task.requirements.size(); i++) {
                                    AgentLogger.info(Thread.currentThread().getName()
                                            + " runSupervisorDecisions - proofBlockStructure - attached: "
                                            + attachedThing2 + " , task: " + task.requirements.get(i));
                                    
                                    if ((task.requirements.get(i).x != pointFound.x || task.requirements.get(i).y != pointFound.y)
                                            && attachedThing2.details.equals(task.requirements.get(i).type)) {
                                        result = new BooleanInfo(true, "");
                                        foundMeetings.put(AgentMeetings.getDistance(meeting), meeting);
                                        break;
                                    }
                                }

                                if (result.value()) break;
                            }
                        }
                    }
                    
                    if (!result.value()) {
                        for (BdiAgentV2 help : StepUtilities.allAgents) {
                            if (!help.getName().equals(agent.getName()) && !AgentCooperations.exists(help)
                                    && !help.getAttachedThings().isEmpty()) {
                                for (Thing attachedThing2 : help.getAttachedThings()) {
                                    // anderer Agent hat den Block der mir noch fehlt
                                    for (int i = 0; i < task.requirements.size(); i++) {
                                        if ((task.requirements.get(i).x != pointFound.x
                                                || task.requirements.get(i).y != pointFound.y)
                                                && attachedThing2.details.equals(task.requirements.get(i).type)) {
                                            result = new BooleanInfo(true, "");
                                            foundMeetings.put(
                                                    Point.distance(Point.castToPoint(agent.belief.getPosition()),
                                                            Point.castToPoint(help.belief.getPosition())),
                                                    new Meeting(agent, null, null, help, null, null));
                                            break;
                                        }
                                    }
                                }
                            }
                        }
                    }

                   /* if (!result.value()) {
                        for (Meeting meeting : AgentMeetings.find(agent)) {
                            for (Meeting subMeeting : AgentMeetings.find(meeting.agent2())) {
                                if (!subMeeting.agent2().getAttachedThings().isEmpty()) {
                                    for (Thing attachedThing2 : subMeeting.agent2().getAttachedThings()) {
                                        // anderer Agent hat den Block der mir noch fehlt
                                        for (int i = 0; i < task.requirements.size(); i++) {
                                            if ((task.requirements.get(i).x != pointFound.x || task.requirements.get(i).y != pointFound.y) 
                                                    && !subMeeting.agent2().getName().equals(agent.getName())
                                                    && attachedThing2.details.equals(task.requirements.get(i).type)
                                                    && !AgentCooperations.exists(subMeeting.agent2())) {
                                                result = new BooleanInfo(true, "");
                                                foundMeetings.put(AgentMeetings.getDistance(subMeeting), subMeeting);
                                                break;
                                            }
                                        }

                                        if (result.value()) break;
                                    }
                                }
                            }
                        }
                    }*/
                }

                if (result.value()) {
                    possibleHelper = foundMeetings.get(foundMeetings.firstKey()).agent2();                    
                    this.coop = new AgentCooperations.Cooperation(info, agent, Status.InGoalZone, possibleHelper,
                                Status.New);
                    agent.isBusy = true;
                    possibleHelper.isBusy = true;
                }
            }
        }
 
        for (Meeting m : AgentMeetings.find(agent))
            AgentLogger.info(Thread.currentThread().getName() + " runSupervisorDecisions - Meetings: " + AgentMeetings.toString(m));
        AgentLogger.info(Thread.currentThread().getName() + " runSupervisorDecisions - possible Helper: " + (possibleHelper == null ? "" : possibleHelper.getName()));
        AgentLogger.info(Thread.currentThread().getName() + " runSupervisorDecisions - proofBlockStructure: " + found + " , " + result);
        return result;
    }
}

