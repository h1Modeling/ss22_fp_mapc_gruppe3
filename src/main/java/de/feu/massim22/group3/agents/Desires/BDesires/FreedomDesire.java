package de.feu.massim22.group3.agents.Desires.BDesires;

import java.awt.Point;

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
        // Test Points instead of attached Things to be extra safe
        for (Point p : belief.getAttachedPoints()) {
            Thing t = belief.getThingWithTypeAt(p, Thing.TYPE_ENTITY);
            if (t != null) {
                attached = t;
                return new BooleanInfo(true, "");
            }
        }
        return new BooleanInfo(true, "Connected to Teammate");
    }

    @Override
    public int getPriority() {
        return 1400;
    }

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
            // Avoid wrong deconnection if Agents stand side by side and are connected via 2 blocks
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
