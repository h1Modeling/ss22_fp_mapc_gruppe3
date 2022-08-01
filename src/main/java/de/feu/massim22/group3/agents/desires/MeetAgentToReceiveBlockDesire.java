package de.feu.massim22.group3.agents.desires;

import java.util.List;
import java.util.Set;

import de.feu.massim22.group3.agents.belief.Belief;
import de.feu.massim22.group3.agents.belief.reachable.ReachableTeammate;
import de.feu.massim22.group3.utils.DirectionUtil;
import massim.protocol.data.Thing;

import java.awt.Point;

public class MeetAgentToReceiveBlockDesire extends BeliefDesire {

    private String teammate;

    public MeetAgentToReceiveBlockDesire(Belief belief, String teammate) {
        super(belief);
        this.teammate = teammate;
    }

    @Override
    public BooleanInfo isFulfilled() {
        Point p = getTeammatePosition();
        if (p == null) {
            return new BooleanInfo(false, "Teammate not in vision");
        }
        int distance = Math.abs(p.x) + Math.abs(p.y);
        return new BooleanInfo(distance < belief.getVision(), "");
    }

    @Override
    public ActionInfo getNextActionInfo() {
        // Move closer to agent
        List<ReachableTeammate> mates = belief.getReachableTeammates();
        for (ReachableTeammate m: mates) {
            if (m.name().equals(teammate)) {
                int dirCode = m.direction();
                String dir = DirectionUtil.intToString(dirCode);
                return getActionForMove(dir.substring(0, 1), getName());
            }
        }
        return ActionInfo.SKIP("No Direction to Agent " + teammate + " found");
    }

    private Point getTeammatePosition() {
        Set<Thing> things = belief.getThings();
        for (Thing t : things) {
            if (t.details.equals(teammate)) {
                return new Point(t.x, t.y);
            } 
        }
        return null;
    }
}
