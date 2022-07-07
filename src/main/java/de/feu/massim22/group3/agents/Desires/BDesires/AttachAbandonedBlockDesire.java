package de.feu.massim22.group3.agents.Desires.BDesires;

import java.awt.Point;
import java.util.ArrayList;
import java.util.List;

import de.feu.massim22.group3.agents.BdiAgentV2;
import de.feu.massim22.group3.agents.Belief;
import massim.protocol.data.Thing;

public class AttachAbandonedBlockDesire extends BeliefDesire {

    private String block;
    private Thing nearest;

    public AttachAbandonedBlockDesire(Belief belief, String block) {
        super(belief);
        this.block = block;
    }

    @Override
    public BooleanInfo isFulfilled() {
        for (Thing t : ((BdiAgentV2) belief.getAgent()).getAttachedThings()) {
            if ((Math.abs(t.x) == 0 || Math.abs(t.y) == 0) && t.type.equals(Thing.TYPE_BLOCK)
                && t.details.equals(block)) {
                return new BooleanInfo(true, "");
            }
        }
        return new BooleanInfo(false, "No block " + block + " attached");
    }

    @Override
    public BooleanInfo isExecutable() {
        nearest = null;
        int vision = belief.getVision();
        List<Thing> possibleThings = new ArrayList<>();
        
        for (Thing t : belief.getThings()) {
            int distance = Math.abs(t.x) + Math.abs(t.y);
            
            if (t.type.equals(Thing.TYPE_BLOCK) && t.details.equals(block) && distance < vision) {
                Thing n = t.x == 0 && t.y == 1 ? null : belief.getThingAt(new Point(t.x, t.y -1));
                Thing s = t.x == 0 && t.y == -1 ? null : belief.getThingAt(new Point(t.x, t.y + 1));
                Thing e = t.x == -1 && t.y == 0 ? null : belief.getThingAt(new Point(t.x + 1, t.y));
                Thing w = t.x == 1 && t.y == 0 ? null : belief.getThingAt(new Point(t.x - 1, t.y));
                // Test if agent is around
                if ((n == null || !n.type.equals(Thing.TYPE_ENTITY))
                    && (s == null || !s.type.equals(Thing.TYPE_ENTITY))
                    && (w == null || !w.type.equals(Thing.TYPE_ENTITY))
                    && (e == null || !e.type.equals(Thing.TYPE_ENTITY))) {
                    possibleThings.add(t);
                }
            }
        }
        if (possibleThings.size() > 0) {
            possibleThings.sort((a, b) -> Math.abs(a.x) + Math.abs(a.y) - Math.abs(b.x) - Math.abs(b.y));
            nearest = possibleThings.get(0);
            return new BooleanInfo(true, "");
        } 
        return new BooleanInfo(false, "No abandoned block in vision");
    }

    @Override
    public ActionInfo getNextActionInfo() {

        Thing n = belief.getThingWithTypeAt("n", Thing.TYPE_BLOCK);
        Thing e = belief.getThingWithTypeAt("e", Thing.TYPE_BLOCK);
        Thing s = belief.getThingWithTypeAt("s", Thing.TYPE_BLOCK);
        Thing w = belief.getThingWithTypeAt("w", Thing.TYPE_BLOCK);

        // Attach North
        if (n != null && n.x == nearest.x && n.y == nearest.y) {
            return ActionInfo.ATTACH("n", getName());
        }
        // Attach East
        if (e != null && e.x == nearest.x && e.y == nearest.y) {
            return ActionInfo.ATTACH("e", getName());
        }
        // Attach South
        if (s != null && s.x == nearest.x && s.y == nearest.y) {
            return ActionInfo.ATTACH("s", getName());
        }
        // Attach West
        if (w != null && w.x == nearest.x && w.y == nearest.y) {
            return ActionInfo.ATTACH("w", getName());
        }
        
        // Move
        String dir = getDirectionToRelativePoint(new Point(nearest.x, nearest.y));
        return getActionForMove(dir, getName());
    }
}
