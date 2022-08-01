package de.feu.massim22.group3.agents.Desires;

import java.awt.Point;

import de.feu.massim22.group3.agents.Belief;
import massim.protocol.data.TaskInfo;
import massim.protocol.data.Thing;

public class LooseWeightDesire extends BeliefDesire {

    public LooseWeightDesire(Belief belief) {
        super(belief);
    }

    @Override
    public BooleanInfo isFulfilled() {
        int attached = belief.getOwnAttachedPoints().size();
        if (attached == 0) {
            return new BooleanInfo(true, getName());
        }
        boolean hasOneBlockTask = false;
        for (TaskInfo info : belief.getTaskInfo()) {
            if (info.requirements.size() == 1) {
                hasOneBlockTask = true;
                break;
            }
        }
        if (attached == 1) {
            // Test if block is useful
            Thing block = belief.getAttachedThings().get(0);
            for (TaskInfo ti : belief.getTaskInfo()) {
                for (Thing t : ti.requirements) {
                    if (!hasOneBlockTask && t.type.equals(block.details) || ti.requirements.size() == 1 && t.type.equals(block.details)) {
                        return new BooleanInfo(true, getName());
                    }
                }
            }

        }
        String info = belief.getOwnAttachedPoints().size() + " Things attached";
        return new BooleanInfo(false, info);
    }

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

    @Override
    public int getPriority() {
        return 20;
    }
}
