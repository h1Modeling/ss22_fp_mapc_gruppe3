package de.feu.massim22.group3.agents.Desires.BDesires;

import java.awt.Point;

import de.feu.massim22.group3.agents.Belief;

public class LooseWeightDesire extends BeliefDesire {

    public LooseWeightDesire(Belief belief) {
        super(belief);
    }

    @Override
    public BooleanInfo isFullfilled() {
        boolean value = belief.getAttachedPoints().size() == 0;
        String info = value ? "" : belief.getAttachedPoints().size() + " Things attached";
        return new BooleanInfo(value, info);
    }

    public ActionInfo getNextActionInfo() {
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
