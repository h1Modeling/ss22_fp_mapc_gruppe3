package de.feu.massim22.group3.agents.desires;

import java.awt.Point;
import java.util.ArrayList;
import java.util.List;

import de.feu.massim22.group3.agents.belief.Belief;
import massim.protocol.data.Thing;

/**
 * The Class <code>AttachAbandonedBlockDesire</code> models the desire to attach an unattached block in vision.
 * 
 * @author Heinz Stadler
 */
public class AttachAbandonedBlockDesire extends BeliefDesire {

    private String block;
    private Thing nearest;
    private String supervisor;

    /**
     * Instantiates a new AttachAbandonedBlockDesire.
     * 
     * @param belief the belief of the agent
     * @param block the type of the block which should be attached
     * @param supervisor the supervisor of the agent
     */
    public AttachAbandonedBlockDesire(Belief belief, String block, String supervisor) {
        super(belief);
        this.block = block;
        this.supervisor = supervisor;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public BooleanInfo isFulfilled() {
        for (Thing t : belief.getAttachedThings()) {
            if (Math.abs(t.x) <= 1 && Math.abs(t.y) <= 1 && t.type.equals(Thing.TYPE_BLOCK)
                && t.details.equals(block)) {
                return new BooleanInfo(true, "");
            }
        }
        return new BooleanInfo(false, "No block " + block + " attached");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public BooleanInfo isExecutable() {
        nearest = null;
        List<Thing> possibleThings = new ArrayList<>();
        Point position = belief.getPosition();
        for (Thing t : belief.getThings()) {
            // Thing is forbidden
            Point absolutePos = new Point(position.x + t.x, position.y + t.y);
            if (belief.isForbidden(absolutePos)) {
                continue;
            }
            if (t.type.equals(Thing.TYPE_BLOCK) && t.details.equals(block)) {
                Point p = new Point(position.x + t.x, position.y + t.y);
                List<Point> attached = belief.getAttachedPoints();
                if (!attached.contains(new Point(t.x, t.y))) {
                    int id = belief.getAgentId();
                    int adjacentId = getBiggestAdjacentAgentId(p, supervisor);
                    if (id == adjacentId || adjacentId == 0) {
                        possibleThings.add(t);
                    }
                }
            }
        }
        if (possibleThings.size() > 0) {
            possibleThings.sort((a, b) -> Math.abs(a.x) + Math.abs(a.y) - Math.abs(b.x) - Math.abs(b.y));
            nearest = possibleThings.get(0);
            return new BooleanInfo(true, "");
        } 
        return new BooleanInfo(false, "No abandoned block in vision");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ActionInfo getNextActionInfo() {

        Thing n = belief.getThingWithTypeAndDetailAt("n", Thing.TYPE_BLOCK, block);
        Thing e = belief.getThingWithTypeAndDetailAt("e", Thing.TYPE_BLOCK, block);
        Thing s = belief.getThingWithTypeAndDetailAt("s", Thing.TYPE_BLOCK, block);
        Thing w = belief.getThingWithTypeAndDetailAt("w", Thing.TYPE_BLOCK, block);
        // Attach North
        if (n != null && n.x == nearest.x && n.y == nearest.y) {
            return ActionInfo.ATTACH("n", getName());
        }
        // Attach East
        if (e != null && e.x == nearest.x && e.y == nearest.y) {
            return ActionInfo.ATTACH("e", getName());
        }
        // Attach South
        if (s != null && s.x == nearest.x && s.y == nearest.y) {
            return ActionInfo.ATTACH("s", getName());
        }
        // Attach West
        if (w != null && w.x == nearest.x && w.y == nearest.y) {
            return ActionInfo.ATTACH("w", getName());
        }
        
        // Move
        Point nearestPoint = new Point(nearest.x, nearest.y);
        String dir = getDirectionToRelativePoint(nearestPoint);
        Point p = getPointFromDirection(dir);
        String dir2 = getDirectionFromAndToRelativePoint(p, nearestPoint);
        return getActionForMove(dir + dir2, getName());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void update(String supervisor) {
        this.supervisor = supervisor;
    }
}
