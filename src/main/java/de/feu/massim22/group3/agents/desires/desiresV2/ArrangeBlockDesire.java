package de.feu.massim22.group3.agents.desires.desiresV2;

import java.awt.Point;

import de.feu.massim22.group3.agents.BdiAgentV2;
import de.feu.massim22.group3.agents.desires.*;
import de.feu.massim22.group3.utils.DirectionUtil;
//import de.feu.massim22.group3.utils.logging.AgentLogger;
import massim.protocol.data.TaskInfo;
import massim.protocol.data.Thing;
import massim.protocol.messages.scenario.Actions;

/**
 * The class <code>ArrangeBlockDesire</code> models the desire to arrange a block.
 * 
 * @author Melinda Betz
 */
public class ArrangeBlockDesire extends BeliefDesire {

    private TaskInfo info;    
    private BdiAgentV2 agent;
    private Thing block;
    
    /**
     * Instantiates a new ArrangeBlockDesire.
     * 
     * @param info - the info of the task
     * @param agent - the agent who wants to arrange a block
     */
    public ArrangeBlockDesire(TaskInfo info, BdiAgentV2 agent) {
        super(agent.getBelief());
        //AgentLogger.info(Thread.currentThread().getName() + " runSupervisorDecisions - Start ArrangeBlockDesire");
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
        //AgentLogger.info(Thread.currentThread().getName() + " runSupervisorDecisions - ArrangeBlockDesire.isFulfilled");
        block = info.requirements.get(0);
        Thing atAgent = belief.getThingWithTypeAt(new Point(block.x, block.y), Thing.TYPE_BLOCK);

        if (atAgent == null || !atAgent.type.equals(Thing.TYPE_BLOCK) || !atAgent.details.equals(block.type))
            return new BooleanInfo(false, "");
        else
            return new BooleanInfo(true, "");
    }

    /**
     * Checks if the desire is executable .
     * 
     * @return if it is executable or not
     */
	@Override
	public BooleanInfo isExecutable() {
		/*AgentLogger
				.info(Thread.currentThread().getName() + " runSupervisorDecisions - ArrangeBlockDesire.isExecutable");*/
		if (belief.getRole().actions().contains(Actions.DETACH)
				&& belief.getRole().actions().contains(Actions.ATTACH)) {
			//Ein Block Task
			if(info.requirements.size() == 1) 
				return new BooleanInfo(true, "");
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
        /*AgentLogger.info(
                Thread.currentThread().getName() + " runSupervisorDecisions - ArrangeBlockDesire.getNextActionInfo");*/
        Point taskBlock = new Point(info.requirements.get(0).x, info.requirements.get(0).y);
        
        if (agent.getAttachedPoints().size() == 0)
            return ActionInfo.SKIP("0008 no block attached ???");
        
        Point agentBlock = agent.getAttachedPoints().get(0);
        Thing agentThing = agent.getAttachedThings().get(0);
        
        /*AgentLogger.info(
                Thread.currentThread().getName() + " runSupervisorDecisions - ArrangeBlockDesire.getNextActionInfo agentBlock: " 
                + agentThing + " , taskBlock: " + info.requirements.get(0));*/
        
        if (!existsTask(agentThing)) {
            return ActionInfo.DETACH(DirectionUtil.intToString(DirectionUtil.getDirectionForCell(agentBlock)), getName());
        }
        
        if (!agentThing.details.equals(info.requirements.get(0).type)) {
            return ActionInfo.SKIP("0009 wrong block for task");
        }

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
                Point target = DirectionUtil.rotateCW(agentBlock);
                return ActionInfo.CLEAR(target, getName());
            }
            if (ccw.type.equals(Thing.TYPE_OBSTACLE)) {
                Point target = DirectionUtil.rotateCCW(agentBlock);
                return ActionInfo.CLEAR(target, getName());
            }                    
            return ActionInfo.SKIP("0010 problem arranging blocks");
        } else {
            if (clockDirection == "cw") {
                if (isFree(cw)) {
                    return ActionInfo.ROTATE_CW(getName());
                } else {
                    if (cw.type.equals(Thing.TYPE_OBSTACLE)) {
                        Point target = DirectionUtil.rotateCW(agentBlock);
                        return ActionInfo.CLEAR(target, getName());
                    }
                }
            }
            if (clockDirection == "ccw") {
                if (isFree(ccw)) {
                    return ActionInfo.ROTATE_CCW(getName());
                } else {
                    if (ccw.type.equals(Thing.TYPE_OBSTACLE)) {
                        Point target = DirectionUtil.rotateCCW(agentBlock);
                        return ActionInfo.CLEAR(target, getName());
                    }
                }
            }
            return ActionInfo.SKIP("0011 problem arranging blocks");
        }
    }
 
    private boolean existsTask(Thing inBlock) {
        for (TaskInfo task : belief.getTaskInfo()) {
            if (!agent.desireProcessing.taskReachedDeadline(agent, info) &&
                 ((task.requirements.size() == 1 && (inBlock.details.equals(task.requirements.get(0).type)))
                    || (task.requirements.size() == 2 && (inBlock.details.equals(task.requirements.get(0).type)
                            || inBlock.details.equals(task.requirements.get(1).type)))
                    || (task.requirements.size() == 3 && (inBlock.details.equals(task.requirements.get(0).type)
                            || inBlock.details.equals(task.requirements.get(1).type)
                            || inBlock.details.equals(task.requirements.get(2).type))))) {
                return true;
            }
        }
        return false;
    }
}