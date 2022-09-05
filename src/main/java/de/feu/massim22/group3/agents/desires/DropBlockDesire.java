package de.feu.massim22.group3.agents.desires;

import java.awt.Point;

import de.feu.massim22.group3.agents.belief.Belief;

/**
 * The Class <code>DropBlockDesire</code> models the desire to drop all blocks the agent is currently carrying.
 * 
 * @author Heinz Stadler
 */
public class DropBlockDesire extends BeliefDesire {

    /**
     * Instantiates a new DropBlockDesire.
     * 
     * @param belief the belief of the agent
     */
    public DropBlockDesire(Belief belief) {
        super(belief);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public BooleanInfo isFulfilled() {
        return new BooleanInfo(belief.getOwnAttachedPoints().size() == 0, "");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ActionInfo getNextActionInfo() {
        if (belief.getOwnAttachedPoints().size() > 0) {
            Point p = belief.getOwnAttachedPoints().get(0);
            String dir = getDirectionFromPoint(p);
            return ActionInfo.DETACH(dir, getName());
        }
        return ActionInfo.SKIP(getName());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isGroupDesire() {
        return true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getPriority() {
        return 20000;
    }
}
