package de.feu.massim22.group3.agents.desires;

import java.awt.Point;

import de.feu.massim22.group3.agents.belief.Belief;
import massim.protocol.data.Thing;

/**
 * The Class <code>FreedomDesire</code> models the desire to stay unconnected to other agents.
 * 
 * @author Heinz Stadler
 */
public class FreedomDesire extends BeliefDesire {

    private Thing attached;

    /**
     * Instantiates a new FreedomDesire.
     *
     * @param belief the belief of the agent
     */
    public FreedomDesire(Belief belief) {
        super(belief);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public BooleanInfo isFulfilled() {
        attached = null;
        // Test Points instead of attached Things to be extra safe
        for (Point p : belief.getAttachedPoints()) {
            Thing t = belief.getThingWithTypeAt(p, Thing.TYPE_ENTITY);
            if (t != null) {
                attached = t;
                return new BooleanInfo(false, "");
            }
        }
        return new BooleanInfo(true, "Connected to Teammate");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getPriority() {
        return 1400;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ActionInfo getNextActionInfo() {
        // indirect connection
        if (getDistance(attached) > 1) {
            for (Point p : belief.getOwnAttachedPoints()) {
                if (getDistance(p) == 1) {
                    String dir = getDirectionFromPoint(p);
                    return ActionInfo.DETACH(dir, getName()); 
                }
            }
        }
        // possible direct connection
        else {
            // Avoid wrong connection if Agents stand side by side and are connected via 2 blocks
            for (Point p : belief.getOwnAttachedPoints()) {
                if (getDistance(p) == 1) {
                    String dir = getDirectionFromPoint(p);
                    return ActionInfo.DETACH(dir, getName()); 
                }
            }
            String dir = getDirectionFromPoint(new Point(attached.x, attached.y));
            return ActionInfo.DETACH(dir, getName());
        }
        // Try general points
        for (Point p : belief.getAttachedPoints()) {
            if (getDistance(p) == 1) {
                String dir = getDirectionFromPoint(p);
                return ActionInfo.DETACH(dir, getName());
            }
        }
        return ActionInfo.SKIP(getName());
    }
}
