package de.feu.massim22.group3.agents.Desires.BDesires;

import de.feu.massim22.group3.EventName;
import de.feu.massim22.group3.agents.Belief;
import de.feu.massim22.group3.agents.Supervisable;
import de.feu.massim22.group3.agents.Reachable.ReachableTeammate;
import eis.iilang.Percept;
import massim.protocol.data.Thing;

public class DeliverBlockDesire extends BeliefDesire {

    private Supervisable communicator;
    private String agent;

    public DeliverBlockDesire(Belief belief, Thing block, String supervisor, String agent, Supervisable communicator) {
        super(belief);
        this.agent = agent;
        this.communicator = communicator;
        precondition.add(new MeetAgentToDeliverBlockDesire(belief, agent));
    }

    public ActionInfo getNextActionInfo() {
        return fullfillPreconditions();
    }

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
        Percept message = new Percept(EventName.SUPERVISOR_PERCEPT_DELIVER_BLOCK_DONE.name());
        communicator.forwardMessage(message, this.agent, belief.getAgentShortName());
        return new BooleanInfo(true, "");
    }

    @Override
    public BooleanInfo isUnfulfillable() {
        return isFulfilled();
    }

    @Override
    public int getPriority() {
        return 950;
    }

    @Override
    public boolean isGroupDesire() {
        return true;
    }
}
