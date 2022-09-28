package de.feu.massim22.group3.agents.desires.desiresV2;

import java.awt.Point;
import java.util.*;

import de.feu.massim22.group3.agents.*;
import de.feu.massim22.group3.agents.desires.*;
//import de.feu.massim22.group3.utils.logging.AgentLogger;
import massim.protocol.data.TaskInfo;
import massim.protocol.data.Thing;

/**
 * The class <code>DisconnectMultiBlocksDesire</code> models the desire to disconnect blocks form a agent after a successfully submitted multi-block-task.
 * 
 * @author Melinda Betz
 */
public class DisconnectMultiBlocksDesire extends BeliefDesire {

    private TaskInfo info;    
    private BdiAgentV2 agent;
    
    /**
     * Initializes a new DisconnectMultiBlocksDesire.
     * 
     * @param info - the info of the task
     * @param agent - the agent who has submitted the task
     */
    public DisconnectMultiBlocksDesire(TaskInfo info, BdiAgentV2 agent) {
        super(agent.getBelief());
        /*AgentLogger
                .info(Thread.currentThread().getName() + " runSupervisorDecisions - Start DisconnectMultiBlocksDesire");*/
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
        return new BooleanInfo(false, "");
    }

    /**
     * Checks if the desire is executable .
     * 
     * @return if it is executable or not
     */
    @Override
    public BooleanInfo isExecutable() {
        return new BooleanInfo(true, "");
    }

    /**
     * Gets the next action that has to be done .
     * 
     * @return the next action
     */
    @Override
    public ActionInfo getNextActionInfo() {
        /*AgentLogger.info(Thread.currentThread().getName()
                + " runSupervisorDecisions - DisconnectMultiBlocksDesire.getNextActionInfo");*/

        List<Thing> list = agent.desireProcessing.getTaskReqsOrdered(info);
        Point block1 = new Point(list.get(0).x, list.get(0).y);
        Point block2 = new Point(list.get(1).x, list.get(1).y);

        /*AgentLogger.info(Thread.currentThread().getName()
                + " runSupervisorDecisions - DisconnectMultiBlocksDesire.getNextActionInfo agentBlocks: "
                + agent.getAttachedThings() + " , taskBlocks: " + info.requirements);*/

        return ActionInfo.DISCONNECT(block1, block2, getName());
    }
}
