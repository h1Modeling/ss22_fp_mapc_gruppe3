package de.feu.massim22.group3.agents.Desires.BDesires;

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
        int attached = belief.getAttachedPoints().size();
        if (attached == 0) {
            return new BooleanInfo(true, getName());
        }
        if (attached == 1) {
            // Test if block is usefull
            Thing block = belief.getAttachedThings().get(0);
            for (TaskInfo ti : belief.getTaskInfo()) {
                for (Thing t : ti.requirements) {
                    if (t.type.equals(block.details)) {
                        return new BooleanInfo(true, getName());
                    }
                }
            }
        }
        String info = belief.getAttachedPoints().size() + " Things attached";
        return new BooleanInfo(false, info);
    }

    public ActionInfo getNextActionInfo() {
        System.out.println("HAS DETACHED1111");
        for (Point p: belief.getAttachedPoints()) {
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
