package de.feu.massim22.group3.agents.desires;

import massim.protocol.data.Thing;

import java.awt.Point;
import java.util.List;

import de.feu.massim22.group3.agents.belief.Belief;

/**
 * The Class <code>DigFreeDesire</code> models the desire to free the agent from a deadlock
 * where the agent unintentionally hasn't moved a while.
 * 
 * @author Heinz Stadler
 */
public class DigFreeDesire extends BeliefDesire {

    private Point beforeLastPosition = new Point(0, 0);
    private Point lastPosition = new Point(0, 0);
    private int limit = 8;
    private int atSamePosition = 0;
    private String dir;

    /**
     * Instantiates a new DigFreeDesire.
     * 
     * @param belief the belief of the agent
     */
    public DigFreeDesire(Belief belief) {
        super(belief);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public BooleanInfo isFulfilled() {
        boolean value = (atSamePosition < limit && dir == null);
        return new BooleanInfo(value, "");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public BooleanInfo isExecutable() {
        boolean value = ((atSamePosition >= limit || dir != null));
        var isWaiting = belief.isWaiting();
        belief.setWaiting(false);
        value = value && !isWaiting;
        return new BooleanInfo(value, "");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void update(String supervisor) {
        Point p = belief.getPosition();
        atSamePosition = p.equals(lastPosition) || p.equals(beforeLastPosition) ? atSamePosition + 1 : 0;
        beforeLastPosition = (Point)lastPosition.clone();
        lastPosition = (Point)p.clone();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ActionInfo getNextActionInfo() {
        // Remove Attachments
        List<Point> attached = belief.getOwnAttachedPoints();
        if (attached.size() > 0) {
            for (Point p : attached) {
                if (p.x == 0 || p.y == 0) {
                    String toDetach = getDirectionFromPoint(p);
                    return ActionInfo.DETACH(toDetach, "");
                }
            }
        }

        Thing n = belief.getThingAt("n");
        Thing s = belief.getThingAt("s");
        Thing w = belief.getThingAt("w");
        Thing e = belief.getThingAt("e");

        // Continue last move
        if (dir != null) {
            Thing t = belief.getThingAt(dir);
            if (isFree(t)) {
                String dir2 = dir;
                // Make random decision to avoid similar behavior between agents in stuck group
                dir = Math.random() > 0.5 ? null : dir2;
                return ActionInfo.MOVE(dir2, "");
            }
            if (isClearable(t)) {
                Point dirPoint = getPointFromDirection(dir);
                return ActionInfo.CLEAR(dirPoint, "");
            }
        }

        // Move
        if (isFree(n)) {
            dir = "n";
            return ActionInfo.MOVE(dir, "");
        }
        if (isFree(s)) {
            dir = "s";
            return ActionInfo.MOVE(dir, "");
        }
        if (isFree(e)) {
            dir = "e";
            return ActionInfo.MOVE(dir, "");
        }
        if (isFree(w)) {
            dir = "w";
            return ActionInfo.MOVE(dir, "");
        }

        // Dig out
        if (isClearable(n)) {
            return ActionInfo.CLEAR(new Point(0, -1), "");
        }
        if (isClearable(s)) {
            return ActionInfo.CLEAR(new Point(0, 1), "");
        }
        if (isClearable(w)) {
            return ActionInfo.CLEAR(new Point(-1, 0), "");
        }
        if (isClearable(e)) {
            return ActionInfo.CLEAR(new Point(1, 0), "");
        }

        return ActionInfo.SKIP("Dig out failed");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getPriority() {
        return 1000;
    }
}
