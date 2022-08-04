package de.feu.massim22.group3.agents.desires;

import java.awt.Point;

import de.feu.massim22.group3.agents.belief.Belief;
import massim.protocol.data.TaskInfo;
import massim.protocol.data.Thing;

/**
 * The Class <code>LooseWeightDesire</code> models the desire to only have blocks attached which can be used für active tasks.
 * 
 * @author Heinz Stadler
 */
public class LooseWeightDesire extends BeliefDesire {

    /**
     * Instantiates a new LooseWeightDesire.
     * 
     * @param belief the belief of the agent
     */
    public LooseWeightDesire(Belief belief) {
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
        if (attached == 1) {
            // Test if block is useful
            Thing block = belief.getAttachedThings().get(0);
            for (TaskInfo ti : belief.getTaskInfo()) {
                for (Thing t : ti.requirements) {
                    if (ti.requirements.size() <= 2 && t.type.equals(block.details)) {
                        return new BooleanInfo(true, getName());
                    }
                }
            }

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
        return 20;
    }
}
