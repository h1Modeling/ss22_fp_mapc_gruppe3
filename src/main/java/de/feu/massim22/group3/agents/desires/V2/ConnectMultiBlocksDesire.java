package de.feu.massim22.group3.agents.desires.V2;

import java.awt.Point;
import java.util.*;

import de.feu.massim22.group3.agents.*;
import de.feu.massim22.group3.agents.belief.Belief;
import de.feu.massim22.group3.agents.desires.*;
import de.feu.massim22.group3.agents.AgentCooperations.Cooperation;
import de.feu.massim22.group3.utils.logging.AgentLogger;
import massim.protocol.data.TaskInfo;
import massim.protocol.data.Thing;
import massim.protocol.messages.scenario.Actions;

public class ConnectMultiBlocksDesire extends BeliefDesire {

    private TaskInfo info;    
    private BdiAgentV2 agent;
    private Cooperation coop;
    
    public ConnectMultiBlocksDesire(Belief belief, TaskInfo info, BdiAgentV2 agent) {
        super(belief);
        AgentLogger.info(Thread.currentThread().getName() + " runSupervisorDecisions - Start ConnectMultiBlocksDesire");
        this.info = info;
        this.agent = agent;
    }

    @Override
    public BooleanInfo isFulfilled() {
        AgentLogger.info(Thread.currentThread().getName() + " runSupervisorDecisions - ConnectMultiBlocksDesire.isFulfilled");
        
        if (AgentCooperations.exists(info, agent)) {
         // Agent ist als master in einer cooperation 
            this.coop = AgentCooperations.get(info, agent);
 
            if (info.requirements.size() == 2) {
                if ((coop.statusMaster().equals(Status.Connected) || coop.statusMaster().equals(Status.Submitted))
                        && (coop.statusHelper().equals(Status.Detached) || coop.statusHelper().equals(Status.Connected))) {
                    return new BooleanInfo(true, "");
                }              
            }
            
            if (info.requirements.size() == 3) {
                if ((coop.statusMaster().equals(Status.Connected) || coop.statusMaster().equals(Status.Submitted))
                        && coop.statusHelper().equals(Status.Detached)
                        && (coop.statusHelper2().equals(Status.Detached) || coop.statusHelper2().equals(Status.Connected))) {
                    return new BooleanInfo(true, "");
                }              
            }
        }
        
        return new BooleanInfo(false, "");
    }

    @Override
    public BooleanInfo isExecutable() {
        AgentLogger.info(
                Thread.currentThread().getName() + " runSupervisorDecisions - ConnectMultiBlocksDesire.isExecutable");
        if (belief.getRole().actions().contains(Actions.DETACH) && belief.getRole().actions().contains(Actions.ATTACH)
                && belief.getRole().actions().contains(Actions.CONNECT)) {

            if (AgentCooperations.exists(info, agent)) {
                AgentLogger.info(Thread.currentThread().getName()
                        + " runSupervisorDecisions - ConnectMultiBlocksDesire.isExecutable - ist master: " + coop.toString());
                // Agent ist in einer cooperation

                if ((coop.statusMaster().equals(Status.ReadyToConnect) 
                        || coop.statusMaster().equals(Status.Connected))
                        && (coop.statusHelper().equals(Status.ReadyToConnect)
                                || (coop.statusHelper().equals(Status.Detached)
                                       && coop.statusHelper2().equals(Status.ReadyToConnect)))) {
                    return new BooleanInfo(true, "");
                }
            }
        }

        return new BooleanInfo(false, "");
    }

    @Override
    public ActionInfo getNextActionInfo() {
        AgentLogger.info(Thread.currentThread().getName()
                + " runSupervisorDecisions - ConnectMultiBlocksDesire.getNextActionInfo");

        Point helperBlock = agent.getAttachedPoints().get(0);
        List<Thing> list = agent.desireProcessing.getTaskReqsOrdered(info);
        Point block1 = new Point(list.get(0).x, list.get(0).y);
        Point block2 = new Point(list.get(1).x, list.get(1).y);

        AgentLogger.info(Thread.currentThread().getName()
                + " runSupervisorDecisions - ConnectMultiBlocksDesire.getNextActionInfo agentBlocks: "
                + agent.getAttachedThings() + " , taskBlocks: " + info.requirements);

        if (agent.getName().equals(coop.master().getName())) {
            // Connect of master
            if (coop.statusHelper().equals(Status.ReadyToConnect)) {
                // Connect with helper
                return ActionInfo.CONNECT(coop.helper().belief.getAgentFullName(),
                        new java.awt.Point(block1.x, block1.y), getName());
            } else {
                // Connect with helper2
                return ActionInfo.CONNECT(coop.helper2().belief.getAgentFullName(),
                        new java.awt.Point(block2.x, block2.y), getName());
            }
        } else {
            // Connect of helpers
            return ActionInfo.CONNECT(coop.master().belief.getAgentFullName(),
                    new java.awt.Point(helperBlock.x, helperBlock.y), getName());
        }
    }
}
