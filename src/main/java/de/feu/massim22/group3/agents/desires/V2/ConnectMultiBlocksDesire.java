package de.feu.massim22.group3.agents.desires.V2;

import java.awt.Point;
import java.util.*;

import de.feu.massim22.group3.agents.*;
import de.feu.massim22.group3.agents.AgentMeetings.Meeting;
import de.feu.massim22.group3.agents.belief.Belief;
import de.feu.massim22.group3.agents.desires.*;
import de.feu.massim22.group3.agents.AgentCooperations.Cooperation;
import de.feu.massim22.group3.utils.DirectionUtil;
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
                Thread.currentThread().getName() + " runSupervisorDecisions - ConnectMultiBlocksDesire.isExecutable");
        if (belief.getRole().actions().contains(Actions.DETACH) && belief.getRole().actions().contains(Actions.ATTACH)
                && belief.getRole().actions().contains(Actions.CONNECT)) {

            if (AgentCooperations.exists(info, agent)) {
                AgentLogger.info(Thread.currentThread().getName()
                        + " runSupervisorDecisions - proofBlockStructure - ist master");
                // Agent ist in einer cooperation

                if (coop.statusMaster().equals(Status.ReadyToConnect)
                        && (coop.statusHelper().equals(Status.ReadyToConnect)
                        || coop.statusHelper2().equals(Status.ReadyToConnect))) {
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

        Point agentBlock = agent.getAttachedPoints().get(0);

        AgentLogger.info(Thread.currentThread().getName()
                + " runSupervisorDecisions - ConnectMultiBlocksDesire.getNextActionInfo agentBlocks: "
                + agent.getAttachedThings() + " , taskBlocks: " + info.requirements);

        if (agent.getName().equals(coop.master().getName())) {
            if ( coop.statusHelper2().equals(Status.ReadyToConnect)) {
                for (int i = 0; i < agent.getAttachedPoints().size(); i++) {
                    if (!DirectionUtil.getCellsIn4Directions().contains(agent.getAttachedPoints().get(i))) {
                        agentBlock = agent.getAttachedPoints().get(i);
                        break;
                    }
                }
                
                return ActionInfo.CONNECT(coop.helper2().belief.getAgentFullName(), new java.awt.Point(agentBlock.x, agentBlock.y), getName());               
            } else {
                return ActionInfo.CONNECT(coop.helper().belief.getAgentFullName(), new java.awt.Point(agentBlock.x, agentBlock.y), getName());              
            }
        } else {
            return ActionInfo.CONNECT(coop.master().belief.getAgentFullName(), new java.awt.Point(agentBlock.x, agentBlock.y), getName());
        }
    }
}
