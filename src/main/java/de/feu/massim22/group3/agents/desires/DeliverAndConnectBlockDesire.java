package de.feu.massim22.group3.agents.desires;

import de.feu.massim22.group3.agents.belief.Belief;
import de.feu.massim22.group3.agents.supervisor.Supervisable;
import massim.protocol.data.TaskInfo;
import massim.protocol.data.Thing;

/**
 * The Class <code>DeliverAndConnectBlockDesire</code> models the desire to deliver and connect a block to another agent.
 * 
 * @author Heinz Stadler
 */
public class DeliverAndConnectBlockDesire extends BeliefDesire {

    private TaskInfo task;

    /**
     * Instantiates a new DeliverAndConnectBlockDesire.
     * 
     * @param belief the belief of the agent
     * @param task the task on which the desire is based on
     * @param agent the name of the agent to which the block should be attached
     * @param agentFullName the full name of the agent to which the block should be attache - this is the name provided by the server
     * @param supervisor the supervisor of the agent group
     * @param block the block which should be attached
     * @param communicator an instance which can send messages to other agents which is normally the agent which holds the desire
     */
    public DeliverAndConnectBlockDesire(Belief belief, TaskInfo task, String agent, String agentFullName, String supervisor, Thing block, Supervisable communicator) {
        super(belief);
        this.task = task;
        
        String[] neededActions = {"connect"};
        precondition.add(new ActionDesire(belief, neededActions));
        precondition.add(new OrDesire(
            new AttachAbandonedBlockDesire(belief, block.type, supervisor),
            new RequestBlockFromDispenserDesire(belief, block))
        );
        precondition.add(new MeetAgentAtGoalZoneDesire(belief, agent));
        precondition.add(new ConnectBlockToAgentDesire(belief, agent, agentFullName, task, block, communicator));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ActionInfo getNextActionInfo() {
        return fulfillPreconditions();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public BooleanInfo isFulfilled() {
        for (IDesire d : precondition) {
            if (!d.isFulfilled().value()) {
                return d.isFulfilled();
            }
        }
        belief.setGroupDesireBlockDetail("");
        belief.setGroupDesirePartner("");
        return new BooleanInfo(true, getName());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public BooleanInfo isUnfulfillable() {
        if (belief.getStep() > task.deadline) {
            belief.setGroupDesireBlockDetail("");
            belief.setGroupDesirePartner("");
            return new BooleanInfo(true, "deadline has passed");
        }
        return new BooleanInfo(false, getName());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getPriority() {
        return 1100;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isGroupDesire() {
        return true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void update(String supervisor) {
        super.update(supervisor);
    }
}
