package de.feu.massim22.group3.agents.Desires.BDesires;

import java.util.List;

import de.feu.massim22.group3.agents.Belief;
import de.feu.massim22.group3.agents.DirectionUtil;
import de.feu.massim22.group3.agents.Reachable.ReachableTeammate;

public class MeetAgentAtGoalZoneDesire extends BeliefDesire {

    private String agent;

    public MeetAgentAtGoalZoneDesire(Belief belief, String agent) {
        super(belief);
        this.agent = agent;
    }

    @Override
    public BooleanInfo isFulfilled() {
        List<ReachableTeammate> mates = belief.getReachableTeammates();
        for (ReachableTeammate m : mates) {
            if (m.name().equals(agent)) {
                return m.distance() > belief.getVision() && m.distance() > 0
                    ? new BooleanInfo(false, "Teammate not in vision")
                    : new BooleanInfo(true, getName());
            }
        }
        return new BooleanInfo(false, "Teammate not reachable");
    }

    @Override
    public ActionInfo getNextActionInfo() {
        for (ReachableTeammate mate : belief.getReachableTeammates()) {
            if (mate.name().equals(agent)) {
                int dirCode = mate.direction();
                String dir = DirectionUtil.intToString(dirCode);
                return getActionForMove(dir.substring(0, 1), getName());
            }
        }
        return ActionInfo.SKIP("Teammate not reachable");
    }
}
