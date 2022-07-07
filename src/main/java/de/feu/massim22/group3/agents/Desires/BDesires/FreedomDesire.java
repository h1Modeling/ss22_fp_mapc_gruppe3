package de.feu.massim22.group3.agents.Desires.BDesires;

import java.awt.Point;

import de.feu.massim22.group3.agents.BdiAgentV2;
import de.feu.massim22.group3.agents.Belief;
import massim.protocol.data.Thing;

public class FreedomDesire extends BeliefDesire {

    private Thing attached;

    public FreedomDesire(Belief belief) {
        super(belief);
    }

    @Override
    public BooleanInfo isFulfilled() {
        attached = null;
        for (Thing t : ((BdiAgentV2) belief.getAgent()).getAttachedThings()) {
            if (t.type.equals(Thing.TYPE_ENTITY)) {
                attached = t;
                return new BooleanInfo(false, "");
            }
        }
        return new BooleanInfo(true, "Connected to Teammate");
    }

    @Override
    public BooleanInfo isExecutable() {
        BooleanInfo isFulfilled = isFulfilled();
        return new BooleanInfo(!isFulfilled.value(), isFulfilled.info());
    }

    @Override
    public int getPriority() {
        return 2000;
    }

    @Override
    public ActionInfo getNextActionInfo() {
        String dir = getDirectionFromPoint(new Point(attached.x, attached.y));
        return ActionInfo.DETACH(dir, "");
    }
}
