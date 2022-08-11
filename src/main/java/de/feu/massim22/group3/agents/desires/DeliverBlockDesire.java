package de.feu.massim22.group3.agents.desires;

import de.feu.massim22.group3.agents.belief.Belief;
import de.feu.massim22.group3.agents.belief.reachable.ReachableTeammate;
import de.feu.massim22.group3.agents.events.EventName;
import de.feu.massim22.group3.agents.supervisor.Supervisable;
import eis.iilang.Percept;
import massim.protocol.data.Thing;

/**
 * The Class <code>DeliverBlockDesire</code> models the desire to deliver a block to another agent.
 * 
 * @author Heinz Stadler
 */
public class DeliverBlockDesire extends BeliefDesire {

    private Supervisable communicator;
    private String agent;

    /**
     * Instantiates an new DeliverBlockDesire.
     * 
     * @param belief the belief of the agent
     * @param block the block which should be delivered
     * @param supervisor the supervisor of the agent group
     * @param agent the name of the agent to which the block should be delivered
     * @param communicator an instance which can send messages to other agents which is normally the agent which holds the desire
     */
    public DeliverBlockDesire(Belief belief, Thing block, String supervisor, String agent, Supervisable communicator) {
        super(belief);
        this.agent = agent;
        this.communicator = communicator;
        precondition.add(new MeetAgentToDeliverBlockDesire(belief, agent));
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
        // Cancel delivery if goal is nearer than teammate
        boolean cancelDesire = false;
        for (ReachableTeammate mate : belief.getReachableTeammates()) {
            if (mate.name().equals(agent)) {
                int distanceGoalZone = belief.getNearestGoalZone().distance();
                int distanceMate = mate.distance();
                if (distanceMate >= distanceGoalZone && distanceMate > 0) {
                    cancelDesire = true;
                }
                break;
            }
        }
        if (!cancelDesire) {
            for (IDesire d : precondition) {
                if (!d.isFulfilled().value()) {
                    return d.isFulfilled();
                }
            }
        }

        // Inform team mate
        belief.setGroupDesirePartner("");
        Percept message = new Percept(EventName.SUPERVISOR_PERCEPT_DELIVER_BLOCK_DONE.name());
        communicator.forwardMessage(message, this.agent, belief.getAgentShortName());
        return new BooleanInfo(true, "");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public BooleanInfo isUnfulfillable() {
        return isFulfilled();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getPriority() {
        return 950;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isGroupDesire() {
        return true;
    }
}
