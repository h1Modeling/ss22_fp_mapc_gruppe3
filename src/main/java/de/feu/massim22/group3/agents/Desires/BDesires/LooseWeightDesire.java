package de.feu.massim22.group3.agents.Desires.BDesires;

import java.awt.Point;

import de.feu.massim22.group3.agents.BdiAgentV2;
import de.feu.massim22.group3.agents.Belief;

public class LooseWeightDesire extends BeliefDesire {

    public LooseWeightDesire(Belief belief) {
        super(belief);
    }

    @Override
    public BooleanInfo isFulfilled() {
        boolean value = ((BdiAgentV2) belief.getAgent()).getAttachedPoints().size() == 0;
        String info = value ? "" : ((BdiAgentV2) belief.getAgent()).getAttachedPoints().size() + " Things attached";
        return new BooleanInfo(value, info);
    }

    @Override
    public ActionInfo getNextActionInfo() {
        for (Point p: ((BdiAgentV2) belief.getAgent()).getAttachedPoints()) {
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
        //Melinda (da "return null"  2-3 Sekunden Wartezeit des Schedulers kosten kann)
        //return null;
        return ActionInfo.SKIP(getName());
        //Melinda Ende
    }

    @Override
    public int getPriority() {
        return 20;
    }
}
