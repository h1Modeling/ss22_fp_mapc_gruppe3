package de.feu.massim22.group3.agents.desires;

import java.awt.Point;

import de.feu.massim22.group3.agents.belief.Belief;
import massim.protocol.data.TaskInfo;
import massim.protocol.data.Thing;

/**
 * The Class <code>DisconnectAllDesire</code> models the desire disconnect from all attached
 * blocks and entities.
 * 
 * @author Phil Heger
 */
public class DisconnectAllDesire extends BeliefDesire {

    /**
     * Instantiates a new DisconnectAllDesire.
     * 
     * @param belief the belief of the agent
     */
    public DisconnectAllDesire(Belief belief) {
        super(belief);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public BooleanInfo isFulfilled() {
        int attached = belief.getOwnAttachedPoints().size();
        if (attached == 0) {
            return new BooleanInfo(true, getName());
        }
        String info = belief.getOwnAttachedPoints().size() + " Things attached";
        return new BooleanInfo(false, info);
    }

    /**
     * {@inheritDoc}
     */
    public ActionInfo getNextActionInfo() {
        for (Point p: belief.getOwnAttachedPoints()) {
            if (p.x == 0 &&  p.y == 1) {
                return ActionInfo.DETACH("s", getName());
            }
            if (p.x == 0 &&  p.y == -1) {
                return ActionInfo.DETACH("n", getName());
            }
            if (p.x == 1 &&  p.y == 0) {
                return ActionInfo.DETACH("e", getName());
            }
            if (p.x == -1 &&  p.y == 0) {
                return ActionInfo.DETACH("w", getName());
            }
        }
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getPriority() {
        return 2000;
    }
}
