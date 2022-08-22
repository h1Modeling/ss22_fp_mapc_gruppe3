package de.feu.massim22.group3.agents.desires.V2;

import java.awt.Point;
import java.util.*;

import de.feu.massim22.group3.agents.*;
import de.feu.massim22.group3.agents.belief.Belief;
import de.feu.massim22.group3.agents.desires.*;
import de.feu.massim22.group3.utils.logging.AgentLogger;
import massim.protocol.data.TaskInfo;
import massim.protocol.data.Thing;

public class DisconnectMultiBlocksDesire extends BeliefDesire {

    private TaskInfo info;    
    private BdiAgentV2 agent;
    
    public DisconnectMultiBlocksDesire(Belief belief, TaskInfo info, BdiAgentV2 agent) {
        super(belief);
        AgentLogger
                .info(Thread.currentThread().getName() + " runSupervisorDecisions - Start DisconnectMultiBlocksDesire");
        this.info = info;
        this.agent = agent;
    }

    @Override
    public BooleanInfo isFulfilled() {
        return new BooleanInfo(false, "");
    }

    @Override
    public BooleanInfo isExecutable() {
        return new BooleanInfo(true, "");
    }

    @Override
    public ActionInfo getNextActionInfo() {
        AgentLogger.info(Thread.currentThread().getName()
                + " runSupervisorDecisions - DisconnectMultiBlocksDesire.getNextActionInfo");

        List<Thing> list = agent.desireProcessing.getTaskReqsOrdered(info);
        Point block1 = new Point(list.get(0).x, list.get(0).y);
        Point block2 = new Point(list.get(1).x, list.get(1).y);

        AgentLogger.info(Thread.currentThread().getName()
                + " runSupervisorDecisions - DisconnectMultiBlocksDesire.getNextActionInfo agentBlocks: "
                + agent.getAttachedThings() + " , taskBlocks: " + info.requirements);

        return ActionInfo.DISCONNECT(block1, block2, getName());
    }
}
