package de.feu.massim22.group3.agents.Desires.V2;

import java.awt.Point;
import java.util.*;

import de.feu.massim22.group3.agents.*;
import de.feu.massim22.group3.agents.AgentMeetings.Meeting;
import de.feu.massim22.group3.agents.Desires.*;
import de.feu.massim22.group3.agents.AgentCooperations.Cooperation;
import de.feu.massim22.group3.utils.logging.AgentLogger;
import eis.iilang.Action;
import eis.iilang.Identifier;
import massim.protocol.data.TaskInfo;
import massim.protocol.data.Thing;
import massim.protocol.messages.scenario.Actions;

public class ConnectMultiBlocksDesire extends BeliefDesire {

    private TaskInfo info;    
    private BdiAgentV2 agent;
    private BdiAgentV2 possibleHelper;
    private Cooperation coop;
    private TreeMap<Integer, Meeting> foundMeetings = new TreeMap<>();
    
    public ConnectMultiBlocksDesire(Belief belief, TaskInfo info, BdiAgentV2 agent) {
        super(belief);
        AgentLogger.info(Thread.currentThread().getName() + " runAgentDecisionsWithTask - Start ConnectMultiBlocksDesire");
        this.info = info;
        this.agent = agent;
    }

    @Override
    public BooleanInfo isFulfilled() {
        AgentLogger.info(Thread.currentThread().getName() + " runAgentDecisionsWithTask - ConnectMultiBlocksDesire.isFulfilled");
        
        if (AgentCooperations.exists(info, agent)) {
         // Agent ist als master in einer cooperation 
            this.coop = AgentCooperations.get(info, agent);
            
            if ((coop.statusMaster().equals(Status.Connected) || coop.statusMaster().equals(Status.Submitted))
                    && (coop.statusHelper().equals(Status.Detached) || coop.statusHelper().equals(Status.Connected))) {
                return new BooleanInfo(true, "");
            }
        }
        
        return new BooleanInfo(false, "");
    }

    @Override
    public BooleanInfo isExecutable() {
        AgentLogger.info(
                Thread.currentThread().getName() + " runAgentDecisionsWithTask - ConnectMultiBlocksDesire.isExecutable");
        if (belief.getRole().actions().contains(Actions.DETACH) && belief.getRole().actions().contains(Actions.ATTACH)
                && belief.getRole().actions().contains(Actions.CONNECT)) {

            if (AgentCooperations.exists(info, agent)) {
                AgentLogger.info(Thread.currentThread().getName()
                        + " runAgentDecisionsWithTask - proofBlockStructure - ist master");
                // Agent ist als master in einer cooperation

                if (coop.statusMaster().equals(Status.ReadyToConnect)
                        && coop.statusHelper().equals(Status.ReadyToConnect)) {
                    return new BooleanInfo(true, "");
                }
            }
        }

        return new BooleanInfo(false, "");
    }

    @Override
    public ActionInfo getNextActionInfo() {
        AgentLogger.info(Thread.currentThread().getName()
                + " runAgentDecisionsWithTask - ConnectMultiBlocksDesire.getNextActionInfo");

        Point agentBlock = agent.getAttachedPoints().get(0);

        AgentLogger.info(Thread.currentThread().getName()
                + " runAgentDecisionsWithTask - ConnectMultiBlocksDesire.getNextActionInfo agentBlocks: "
                + agent.getAttachedThings() + " , taskBlocks: " + info.requirements);

        if (agent.getName().equals(coop.master().getName())) {
            return ActionInfo.CONNECT(coop.helper().belief.getAgentFullName(), new java.awt.Point(agentBlock.x, agentBlock.y), getName());
        } else {
            return ActionInfo.CONNECT(coop.master().belief.getAgentFullName(), new java.awt.Point(agentBlock.x, agentBlock.y), getName());
        }
    }
}
