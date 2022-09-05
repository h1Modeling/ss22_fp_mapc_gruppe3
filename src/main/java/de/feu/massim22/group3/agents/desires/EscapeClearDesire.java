package de.feu.massim22.group3.agents.desires;

import de.feu.massim22.group3.agents.belief.Belief;

import java.awt.Point;

/**
 * The Class <code>EscapeClearDesire</code> models the desire to avoid regions which have a clear marker attached.
 * 
 * @author Heinz Stadler
 */
public class EscapeClearDesire extends BeliefDesire {

    /**
     * Instantiates a new EscapeClearDesire.
     * 
     * @param belief the belief of the agent
     */
    public EscapeClearDesire(Belief belief) {
        super(belief);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public BooleanInfo isFulfilled() {
        return belief.isInClearDanger() 
            ? new BooleanInfo(false, "")
            : new BooleanInfo(true, "not threatened by clear");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ActionInfo getNextActionInfo() {
        Point nearestEscape = getNearestEscape();
        String dir = getDirectionFromPoint(nearestEscape);
        return getActionForMove(dir + dir, getName());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getPriority() {
        return 10000;
    }

    private Point getNearestEscape() {
        var marker = belief.getMarker();
        marker.sort((a,b) -> Math.abs(a.x) + Math.abs(a.y) - Math.abs(b.x) - Math.abs(b.y));

        for (int i = 1; i < belief.getVision(); i++) {
            // Test if marker is at position
            for (int x = -i; x <= i; x++) {
                for (int y = -i; y <= i; y++) {
                    final int xx = x;
                    final int yy = y;
                    boolean remove = marker.removeIf(m -> m.x == xx && m.y == yy);
                    if (!remove) {
                        return new Point(x, y);
                    }
                }
            }
        }
        return new Point(-belief.getVision() - 1, 0);
    }
}
