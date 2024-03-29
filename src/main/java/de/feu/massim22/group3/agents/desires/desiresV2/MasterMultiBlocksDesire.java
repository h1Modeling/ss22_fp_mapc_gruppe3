package de.feu.massim22.group3.agents.desires.desiresV2;

//import java.awt.Point;
import java.util.*;

import de.feu.massim22.group3.agents.*;
import de.feu.massim22.group3.agents.desires.*;
import de.feu.massim22.group3.agents.utilsV2.*;
import de.feu.massim22.group3.agents.utilsV2.AgentCooperations.Cooperation;
import de.feu.massim22.group3.agents.utilsV2.AgentMeetings.Meeting;
import de.feu.massim22.group3.utils.DirectionUtil;
//import de.feu.massim22.group3.utils.logging.AgentLogger;
import massim.protocol.data.TaskInfo;
import massim.protocol.data.Thing;
import massim.protocol.messages.scenario.Actions;

/**
 * The class <code>MasterMultiBlocksDesire</code> models the desire for a agent to become a master.
 * 
 * @author Melinda Betz
 */
public class MasterMultiBlocksDesire extends BeliefDesire {
    
    private TaskInfo info;
    private BdiAgentV2 agent;
    private BdiAgentV2 possibleHelper;
    private BdiAgentV2 possibleHelper2;
    private Cooperation coop;
    private TreeMap<Integer, Meeting> foundMeetings = new TreeMap<>();
    private Point block1;
    private Point block2;
    private Point block3;
    private Thing block3Thing;

    /**
     * Initializes a new MasterMultiBlocksDesire.
     * 
     * @param info - the info of the task
     * @param agent - the agent who wants to become a master
     */
    public MasterMultiBlocksDesire(TaskInfo info, BdiAgentV2 agent) {
        super(agent.getBelief());
        //AgentLogger.info(Thread.currentThread().getName() + " runSupervisorDecisions - Start MasterMultiBlocksDesire");
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
        /*AgentLogger.info(
                Thread.currentThread().getName() + " runSupervisorDecisions - MasterMultiBlocksDesire.isFulfilled");*/
        for (Thing t : info.requirements) {
            Thing atAgent = belief.getThingAt(new Point(t.x, t.y));
            if (atAgent == null || !atAgent.type.equals(Thing.TYPE_BLOCK) || !atAgent.details.equals(t.type)) {
                return new BooleanInfo(false, "");
            }
        }

        if (AgentCooperations.exists(info, agent, 1)) {
            /*AgentLogger.info(
                    Thread.currentThread().getName() + " runSupervisorDecisions - findHelper - ist master");*/
            // agent is master of a cooperation
            this.coop = AgentCooperations.get(info, agent, 1);

            if (info.requirements.size() == 2) {
            if (!(coop.statusMaster().equals(Status.Connected) 
                    && coop.statusHelper().equals(Status.Detached))) {
                return new BooleanInfo(false, "");
            }
            }
            
            if (info.requirements.size() == 3) {
            if (!(coop.statusMaster().equals(Status.Connected) 
                    && coop.statusHelper().equals(Status.Detached) 
                    && coop.statusHelper2().equals(Status.Detached))) {
                return new BooleanInfo(false, "");
            }
            }
        }

        return new BooleanInfo(true, "");
    }

    /**
     * Checks if the desire is executable .
     * 
     * @return if it is executable or not
     */
    @Override
    public BooleanInfo isExecutable() {
        /*AgentLogger.info(
                Thread.currentThread().getName() + " runSupervisorDecisions - MasterMultiBlocksDesire.isExecutable");*/
        if (belief.getRole().actions().contains(Actions.DETACH) && belief.getRole().actions().contains(Actions.ATTACH)
                && belief.getRole().actions().contains(Actions.CONNECT)) {
            // multi-blocks-task
            if (info.requirements.size() >= 2) {
                BooleanInfo ret = findHelper(info);
                
                if (ret.value()) {
                    // BooleanInfo ret2 = findHelper2(info);                   
                }

                return ret;
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
        //AgentLogger.info(Thread.currentThread().getName()
         //       + " runSupervisorDecisions - MasterMultiBlocksDesire.getNextActionInfo");
        agent.desireProcessing.tryLastWanted = true;
        
        Point taskBlock = block1;
        for (int i = 0; i < info.requirements.size(); i++) {
            if (DirectionUtil.getCellsIn4Directions().contains(new java.awt.Point(info.requirements.get(i).x, info.requirements.get(i).y))) {
                taskBlock = new Point(info.requirements.get(i).x, info.requirements.get(i).y);
                break;
            }
        }
        /*AgentLogger.info(Thread.currentThread().getName()
                + " runSupervisorDecisions - MasterMultiBlocksDesire.getNextActionInfo block1: " 
                + block1 + " block2: " + block2 + " block3: " + block3 + " taskBlock: " + taskBlock); */       
        Point agentBlock = agent.getAttachedPoints().get(0);
        // Thing agentThing = agent.getAttachedThings().get(0);

        /*AgentLogger.info(Thread.currentThread().getName()
                + " runSupervisorDecisions - MasterMultiBlocksDesire.getNextActionInfo agentBlocks: "
                + agent.getAttachedThings() + " , taskBlocks: " + info.requirements);

        AgentLogger.info(Thread.currentThread().getName()
                + " runSupervisorDecisions - MasterMultiBlocksDesire.getNextActionInfo - coop: "
                + AgentCooperations.toString(coop));*/

        if (coop.statusMaster().equals(Status.Connected)) {
            if (coop.task().requirements.size() == 2) {
                if (coop.statusHelper().equals(Status.Connected) || coop.statusHelper().equals(Status.ReadyToDetach)) {
                    return ActionInfo.SKIP("1000 waiting for helper to detach");
                } else {
                    return ActionInfo.SKIP("1000 waiting for helper");
                }
            } else {
                if (coop.statusHelper2().equals(Status.Connected) || coop.statusHelper2().equals(Status.ReadyToDetach)) {
                    return ActionInfo.SKIP("1000 waiting for helper2 to detach");
                } else {
                    return ActionInfo.SKIP("1000  waiting for helper2");
                }
            }
        } else if (coop.statusMaster().equals(Status.ReadyToConnect)) {
            return ActionInfo.SKIP("1000 waiting for helper to be ready to connect");
        } else {
            if (taskBlock.equals(agentBlock)) {
                //AgentLogger.info(Thread.currentThread().getName()
                  //      + " runSupervisorDecisions - MasterMultiBlocksDesire.getNextActionInfo - AA");
                AgentCooperations.setCooperation(
                        new AgentCooperations.Cooperation(coop.task(), coop.master(), Status.ReadyToConnect,
                                coop.helper(), coop.statusHelper(), coop.helper2(), coop.statusHelper2()));
                return ActionInfo.SKIP("1000 waiting for helper to be ready to connect");
            } else {
                //AgentLogger.info(Thread.currentThread().getName()
                    //    + " runSupervisorDecisions - MasterMultiBlocksDesire.getNextActionInfo - BB");
                AgentCooperations.setCooperation(new AgentCooperations.Cooperation(coop.task(), coop.master(),
                        Status.Arranging, coop.helper(), coop.statusHelper(), coop.helper2(), coop.statusHelper2()));
                String clockDirection = DirectionUtil.getClockDirection(agentBlock, taskBlock);
                Thing cw = belief.getThingCRotatedAt(agentBlock);
                Thing ccw = belief.getThingCCRotatedAt(agentBlock);

                if (clockDirection == "") {
                    if (isFree(cw)) {
                        return ActionInfo.ROTATE_CW(getName());
                    }
                    if (isFree(ccw)) {
                        return ActionInfo.ROTATE_CCW(getName());
                    }            
                    if (cw.type.equals(Thing.TYPE_OBSTACLE)) {
                        Point target = Point.castToPoint(DirectionUtil.rotateCW(agentBlock));
                        return ActionInfo.CLEAR(target, getName());
                    }
                    if (ccw.type.equals(Thing.TYPE_OBSTACLE)) {
                        Point target = Point.castToPoint(DirectionUtil.rotateCCW(agentBlock));
                        return ActionInfo.CLEAR(target, getName());
                    }                    
                    return ActionInfo.SKIP("0010 problem arranging blocks");
                } else {
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

    /**
     * Searches for a first helper for a certain task .
     * 
     * @param task the task for which the helper is needed
     * 
     * @return if a helper could be found or not
     */
    public BooleanInfo findHelper(TaskInfo task) {
        //AgentLogger.info(Thread.currentThread().getName() + " runSupervisorDecisions - findHelper");
        BooleanInfo result = new BooleanInfo(false, "");
        boolean found = false;

        /*//AgentLogger.info(Thread.currentThread().getName()
                + " runSupervisorDecisions - findHelper - agent.isBusy: " + agent.isBusy + " , " + task.name);
        //AgentLogger.info(Thread.currentThread().getName() + " runSupervisorDecisions - findHelper - coops: "
                + AgentCooperations.toString(AgentCooperations.cooperations));*/
        
        List<Thing> list = agent.desireProcessing.getTaskReqsOrdered(task);
        block1 = new Point(list.get(0).x, list.get(0).y);
        
        if (list.size() > 1) {
            block2 = new Point(list.get(1).x, list.get(1).y);       
        }
        
        if (list.size() > 2) {
            block3 = new Point(list.get(2).x, list.get(2).y);
            block3Thing = (task.requirements.size() >= 3 ? list.get(2) : null);            
        }

        if (AgentCooperations.exists(task, agent, 1)) {
            /*AgentLogger.info(
                    Thread.currentThread().getName() + " runSupervisorDecisions - findHelper - ist master");*/
            // agent is master in a cooperation of this task 
            this.coop = AgentCooperations.get(task, agent, 1);
            result = new BooleanInfo(true, "");

        } else {
            if (AgentCooperations.anotherMasterIsPossible() && !AgentCooperations.exists(agent) 
                    && AgentCooperations.getCountMaster(task.requirements.size()) < AgentCooperations.getMaxMaster(task.requirements.size())) { 
                // there is no maxMaster yet and the agent is neither as a master or  as a helper part of a existing cooperation 
                for (Thing attachedThing : agent.getAttachedThings()) {
                    // agent has a matching block
                    for (int i = 0; i < task.requirements.size(); i++) {
                        if (task.requirements.get(i).type.equals(attachedThing.details)
                                && ((task.requirements.get(i).x == 0 || task.requirements.get(i).y == 0)
                                        && task.requirements.get(i).x <= 1 && task.requirements.get(i).y <= 1)) {
                            found = true;
                            break;
                        }
                    }

                    if (found) break;
                }

                if (found) {
                    for (Meeting meeting : AgentMeetings.find(agent)) {
                        if (!AgentCooperations.exists(meeting.agent2())
                                && !meeting.agent2().getAttachedThings().isEmpty()) {
                            for (Thing attachedThing2 : meeting.agent2().getAttachedThings()) {
                                // other agent has the block which I still need
                                for (int i = 0; i < task.requirements.size(); i++) {
                                    /*AgentLogger.info(Thread.currentThread().getName()
                                            + " runSupervisorDecisions - findHelper - attached: "
                                            + attachedThing2 + " , task: " + task.requirements.get(i));*/

                                    if ((task.requirements.get(i).x != block1.x || task.requirements.get(i).y != block1.y)
                                            && attachedThing2.details.equals(task.requirements.get(i).type)
                                            && existsCommonEdge(block1, new Point(task.requirements.get(i).x, task.requirements.get(i).y))) {
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
                            if (help.getBelief().getTeam().equals(agent.getBelief().getTeam())) {
                                if (!help.getName().equals(agent.getName()) && !AgentCooperations.exists(help)
                                        && !help.getAttachedThings().isEmpty()) {
                                    for (Thing attachedThing2 : help.getAttachedThings()) {
                                        // other agent has the block which I still need
                                        for (int i = 0; i < task.requirements.size(); i++) {
                                            if ((task.requirements.get(i).x != block1.x
                                                    || task.requirements.get(i).y != block1.y)
                                                    && attachedThing2.details.equals(task.requirements.get(i).type)
                                                    && existsCommonEdge(block1, new Point(task.requirements.get(i).x,
                                                            task.requirements.get(i).y))) {
                                                result = new BooleanInfo(true, "");
                                                foundMeetings.put(
                                                        Point.distance(Point.castToPoint(agent.getBelief().getPosition()),
                                                                Point.castToPoint(help.getBelief().getPosition())),
                                                        new Meeting(agent, null, null, null, help, null, null, null));
                                                break;
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                if (result.value()) {
                    possibleHelper = foundMeetings.get(foundMeetings.firstKey()).agent2();
                    this.coop = new AgentCooperations.Cooperation(info, agent, Status.InGoalZone, possibleHelper,
                            Status.New, null, Status.No2);
                    agent.isBusy = true;
                    possibleHelper.isBusy = true;
                }
            }
        }
        
        /*for (Meeting m : AgentMeetings.find(agent))
            AgentLogger.info(Thread.currentThread().getName() + " runSupervisorDecisions - Meetings: "
                    + AgentMeetings.toString(m));
        AgentLogger.info(Thread.currentThread().getName() + " runSupervisorDecisions - possible Helper: "
                + (possibleHelper == null ? "" : possibleHelper.getName()));
        AgentLogger.info(Thread.currentThread().getName() + " runSupervisorDecisions - findHelper: " + found
                + " , " + result + " , " + (this.coop == null ? "" : this.coop.toString()));*/
        return result;
    }

    /**
     * Searches for a second helper for a certain task .
     * 
     * @param task the task for which the helper is needed
     * 
     * @return if a helper could be found or not
     */
    public BooleanInfo findHelper2(TaskInfo task) {
        //AgentLogger.info(Thread.currentThread().getName() + " runSupervisorDecisions - findHelper2");
        BooleanInfo result = new BooleanInfo(false, "");
        foundMeetings = new TreeMap<>();

        /*//AgentLogger.info(Thread.currentThread().getName()
                + " runSupervisorDecisions - findHelper2 - agent.isBusy: " + agent.isBusy + " , " + task.name);
        //AgentLogger.info(Thread.currentThread().getName() + " runSupervisorDecisions - findHelper2 - coops: "
                + AgentCooperations.toString(AgentCooperations.cooperations));*/
        
        if (task.requirements.size() > 2) {
            //the task is a 3-blocks-task
            if (AgentCooperations.exists(task, agent, 1) 
                    && !AgentCooperations.get(task, agent, 1).statusHelper2().equals(Status.No2)) {
                // agent is a master in a cooperation form this task, which already has a second helper (helper2)
                //AgentLogger.info(Thread.currentThread().getName()
                    //    + " runSupervisorDecisions - findHelper2 - ist master");
                result = new BooleanInfo(true, "");

            } else {
                if (AgentCooperations.exists(task, agent, 1) || !AgentCooperations.exists(agent)) {
                    // agent is either master without a second helper (helper2) or he is not in a cooperation 
                    for (Meeting meeting : AgentMeetings.find(agent)) {
                        /*AgentLogger.info(Thread.currentThread().getName()
                                + " runSupervisorDecisions - findHelper2 - met: "
                                + meeting.agent2().getName() + " , helper: " + this.coop.helper().getName());*/

                        if ((!meeting.agent2().equals(this.coop.helper()) && !AgentCooperations.exists(meeting.agent2())
                                || meeting.agent2().equals(this.coop.helper())
                                        && this.coop.statusHelper().equals(Status.Detached))
                                && !meeting.agent2().getAttachedThings().isEmpty()) {

                            for (Thing attachedThing2 : meeting.agent2().getAttachedThings()) {
                                //AgentLogger.info(Thread.currentThread().getName()
                                  //      + " runSupervisorDecisions - findHelper2 - in1 ");

                                if (block3Thing.type.equals(attachedThing2.details)) {
                                    // other agent has the block which I still need
                                    //AgentLogger.info(Thread.currentThread().getName()
                                      //      + " runSupervisorDecisions - findHelper2 - infound ");
                                    result = new BooleanInfo(true, "");
                                    foundMeetings.put(AgentMeetings.getDistance(meeting), meeting);
                                    break;
                                }
                            }
                        }


                        if (!result.value()) {
                            for (BdiAgentV2 help : StepUtilities.allAgents) {
                                if (help.getBelief().getTeam().equals(agent.getBelief().getTeam())) {
                                    /*AgentLogger.info(Thread.currentThread().getName()
                                            + " runSupervisorDecisions - findHelper2 - allA: " + help.getName()
                                            + " , helper: " + this.coop.helper().getName());*/
                                    if (!help.getName().equals(agent.getName())
                                            && (!help.getName().equals(this.coop.helper().getName())
                                                    && !AgentCooperations.exists(help)
                                                    || help.getName().equals(this.coop.helper().getName())
                                                            && this.coop.statusHelper().equals(Status.Detached))
                                            && !help.getAttachedThings().isEmpty()) {
                                        //AgentLogger.info(Thread.currentThread().getName()
                                            //    + " runSupervisorDecisions - findHelper2 - in1 ");
                                        for (Thing attachedThing2 : help.getAttachedThings()) {
                                            // aother agent has the block which I still need
                                            //AgentLogger.info(Thread.currentThread().getName()
                                               //     + " runSupervisorDecisions - findHelper2 - in2 ");
                                            if (block3Thing.type.equals(attachedThing2.details)) {
                                                //AgentLogger.info(Thread.currentThread().getName()
                                                   //     + " runSupervisorDecisions - findHelper2 - infound ");
                                                result = new BooleanInfo(true, "");
                                                foundMeetings.put(
                                                        Point.distance(Point.castToPoint(agent.getBelief().getPosition()),
                                                                Point.castToPoint(help.getBelief().getPosition())),
                                                        new Meeting(agent, null, null, null, help, null, null, null));
                                                break;
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    if (result.value()) {
                        possibleHelper2 = foundMeetings.get(foundMeetings.firstKey()).agent2();
                        this.coop = new AgentCooperations.Cooperation(info, agent,  this.coop.statusMaster(),
                                this.coop.helper(), this.coop.statusHelper(), possibleHelper2, Status.New);
                        agent.isBusy = true;
                        possibleHelper2.isBusy = true;
                    }
                }
            }
        } else {
            //no 3-blocks.task always true
            result = new BooleanInfo(true, "");
        }

        /*for (Meeting m : AgentMeetings.find(agent))
              AgentLogger.info(Thread.currentThread().getName() + " runSupervisorDecisions - Meetings: "
                    + AgentMeetings.toString(m));
        //AgentLogger.info(Thread.currentThread().getName() + " runSupervisorDecisions - possible Helper2: "
                + (possibleHelper2 == null ? "" : possibleHelper2.getName()));
        //AgentLogger.info(Thread.currentThread().getName() + " runSupervisorDecisions - findHelper2: " + found
                + " , " + result + " , " + this.coop.toString());*/
        return result;
    }
    
    private boolean existsCommonEdge(Point p1, Point p2) {
        if ((Math.abs(p2.x - p1.x) == 0 && Math.abs(p2.y - p1.y) == 1)
            ||
            (Math.abs(p2.y - p1.y) == 0 && Math.abs(p2.x - p1.x) == 1)) {
            return true;     
        }

        return false;
    }
    
    /**
     * Gets the information (requirements, deadline, name etc) of a task .
     * 
     * @return the information
     */
    public TaskInfo getTask() {
        return info;
    }
}

