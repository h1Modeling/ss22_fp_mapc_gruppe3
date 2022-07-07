package de.feu.massim22.group3.agents.Desires.BDesires;

import java.util.List;
import java.util.Set;

import de.feu.massim22.group3.agents.Belief;
import de.feu.massim22.group3.agents.DirectionUtil;
import de.feu.massim22.group3.agents.Reachable.ReachableTeammate;
import massim.protocol.data.Thing;

import java.awt.Point;

public class MeetAgentToDeliverBlockDesire extends BeliefDesire {

    private String agent;

    public MeetAgentToDeliverBlockDesire(Belief belief, String agent) {
        super(belief);
        this.agent = agent;
    }

    @Override
    public BooleanInfo isFulfilled() {
        return new BooleanInfo(belief.getOwnAttachedPoints().size() == 0, "");
    }

    @Override
    public ActionInfo getNextActionInfo() {
        // Test if teammate is in vision
        Point p = getTeammatePosition();
        if (p != null) {
            Point attached = belief.getOwnAttachedPoints().get(0);
            // Rotation would help
            if (p.x * attached.x < 0 && p.y * attached.y < 0) {
                Point cRotated = getCRotatedPoint(attached);
                // Rotate clockwise
                if (p.x * cRotated.x >= 0 || p.y * cRotated.y >= 0) {
                    Thing t = belief.getThingAt(cRotated);
                    if (isFree(t)) {
                        return ActionInfo.ROTATE_CW(getName());
                    }
                    if (isClearable(t)) {
                        return ActionInfo.CLEAR(cRotated, getName());
                    }
                }
                Point ccRotated = getCCRotatedPoint(attached);
                // Rotate counter clockwise
                Thing t = belief.getThingAt(ccRotated);
                if (isFree(t)) {
                    return ActionInfo.ROTATE_CW(getName());
                }
                if (isClearable(t)) {
                    return ActionInfo.CLEAR(cRotated, getName());
                }
            }
        }
        // Move closer to agent
        List<ReachableTeammate> mates = belief.getReachableTeammates();
        for (ReachableTeammate m: mates) {
            if (m.name().equals(agent) && m.distance() > belief.getVision()) {
                int dirCode = m.direction();
                String dir = DirectionUtil.intToString(dirCode);
                return getActionForMove(dir.substring(0, 1), getName());
            }
        }
        // Detach
        Point attached = belief.getOwnAttachedPoints().get(0);
        String dir = getDirectionFromPoint(attached);
        Point position = belief.getPosition();
        Point absoluteAttached = new Point(position.x + attached.x, position.y + attached.y);
        belief.addForbiddenThing(absoluteAttached, 15);
        return ActionInfo.DETACH(dir, getName());
    } 

    private Point getTeammatePosition() {
        Set<Thing> things = belief.getThings();
        for (Thing t : things) {
            if (t.details.equals(agent)) {
                return new Point(t.x, t.y);
            } 
        }
        return null;
    }
}
