package de.feu.massim22.group3.agents.desires;

import massim.protocol.data.Thing;

import java.awt.Point;

import de.feu.massim22.group3.agents.belief.Belief;

public class WaitAtGoalZoneDesire extends BeliefDesire {

    public WaitAtGoalZoneDesire(Belief belief) {
        super(belief);
    }

    @Override
    public BooleanInfo isFulfilled() {
        return new BooleanInfo(false, "");
    }

    @Override
    public BooleanInfo isExecutable() {
        var atGoalZone = belief.getGoalZones().contains(new Point(0, 0));
        var blockAttached = belief.getAttachedThings().size() == 1;
        var value = atGoalZone && blockAttached;
        return new BooleanInfo(value, getName());
    }

    @Override
    public ActionInfo getNextActionInfo() {
        belief.setWaiting(true);
        // Clear blocks around while waiting
        var n = belief.getThingAt("n");
        var s = belief.getThingAt("s");
        var w = belief.getThingAt("w");
        var e = belief.getThingAt("e");
        var nw = belief.getThingAt(new Point(-1, -1));
        var ne = belief.getThingAt(new Point(1, -1));
        var sw = belief.getThingAt(new Point(-1, 1));
        var se = belief.getThingAt(new Point(1, 1));
        if (n != null && n.type.equals(Thing.TYPE_OBSTACLE)) {
            return ActionInfo.CLEAR(new Point(0, -1), getName());
        }
        if (s != null && s.type.equals(Thing.TYPE_OBSTACLE)) {
            return ActionInfo.CLEAR(new Point(0, 1), getName());
        }
        if (e != null && e.type.equals(Thing.TYPE_OBSTACLE)) {
            return ActionInfo.CLEAR(new Point(1, 0), getName());
        }
        if (w != null && w.type.equals(Thing.TYPE_OBSTACLE)) {
            return ActionInfo.CLEAR(new Point(-1, 0), getName());
        }
        if ((nw != null && nw.type.equals(Thing.TYPE_OBSTACLE) || ne != null && ne.type.equals(Thing.TYPE_OBSTACLE))
            && belief.getGoalZones().contains(new Point(0, -1))) {
            return getActionForMove("n", getName());
        }
        if ((sw != null && sw.type.equals(Thing.TYPE_OBSTACLE) || se != null && se.type.equals(Thing.TYPE_OBSTACLE))
            && belief.getGoalZones().contains(new Point(0, 1))) {
            return getActionForMove("s", getName());
        }
        // Move closer to center
        if (!belief.getGoalZones().contains(new Point(0, -1))) {
            return getActionForMove("s", getName());
        }
        if (!belief.getGoalZones().contains(new Point(0, 1))) {
            return getActionForMove("n", getName());
        }
        if (!belief.getGoalZones().contains(new Point(1, 0))) {
            return getActionForMove("w", getName());
        }
        if (!belief.getGoalZones().contains(new Point(-1, 0))) {
            return getActionForMove("e", getName());
        }
        // Move away from dispenser
        if (belief.getOwnAttachedPoints().size() > 0) {
            var attached = belief.getOwnAttachedPoints().get(0);
            var atAttached = belief.getThingWithTypeAt(attached, Thing.TYPE_DISPENSER);
            if (atAttached != null) {
                var dir = getDirectionFromPoint(new Point(-attached.x, -attached.y));
                return getActionForMove(dir, getName());
            }
        }
        return ActionInfo.SKIP(getName());
    }

    @Override
    public int getPriority() {
        return 15;
    }
}
