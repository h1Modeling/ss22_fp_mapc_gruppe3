package de.feu.massim22.group3.agents.Desires.BDesires;

import java.awt.Point;
import java.util.ArrayList;
import java.util.List;

import de.feu.massim22.group3.agents.Belief;
import de.feu.massim22.group3.map.Navi;
import massim.protocol.data.Thing;

public class AttachAbandonedBlockDesire extends BeliefDesire {

    private String block;
    private Thing nearest;
    private String supervisor;

    public AttachAbandonedBlockDesire(Belief belief, String block, String supervisor) {
        super(belief);
        this.block = block;
        this.supervisor = supervisor;
    }

    @Override
    public BooleanInfo isFulfilled() {
        for (Thing t : belief.getAttachedThings()) {
            if (Math.abs(t.x) <= 1 && Math.abs(t.y) <= 1 && t.type.equals(Thing.TYPE_BLOCK)
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
        String team = belief.getTeam();
        Point position = belief.getPosition();
        for (Thing t : belief.getThings()) {
            // Thing is forbidden
            Point absolutePos = new Point(position.x + t.x, position.y + t.y);
            if (belief.isForbidden(absolutePos)) {
                continue;
            }
            int distance = Math.abs(t.x) + Math.abs(t.y);
            if (t.type.equals(Thing.TYPE_BLOCK) && t.details.equals(block) && distance < vision) {
                Thing n = t.x == 0 && t.y == 1 ? null : belief.getThingAt(new Point(t.x, t.y -1));
                Thing s = t.x == 0 && t.y == -1 ? null : belief.getThingAt(new Point(t.x, t.y + 1));
                Thing e = t.x == -1 && t.y == 0 ? null : belief.getThingAt(new Point(t.x + 1, t.y));
                Thing w = t.x == 1 && t.y == 0 ? null : belief.getThingAt(new Point(t.x - 1, t.y));
                // Test if agent is around
                Point absolute = new Point(position.x + t.x, position.y + t.y);
                boolean isAttached = Navi.get().isBlockAttached(supervisor, absolute);
                if ((n == null || (!isAttached && !isEnemy(n, team)))
                    && (s == null || (!isAttached && !isEnemy(s, team)))
                    && (w == null || (!isAttached && !isEnemy(w, team)))
                    && (e == null || (!isAttached && !isEnemy(e, team)))) {
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

        Thing n = belief.getThingWithTypeAndDetailAt("n", Thing.TYPE_BLOCK, block);
        Thing e = belief.getThingWithTypeAndDetailAt("e", Thing.TYPE_BLOCK, block);
        Thing s = belief.getThingWithTypeAndDetailAt("s", Thing.TYPE_BLOCK, block);
        Thing w = belief.getThingWithTypeAndDetailAt("w", Thing.TYPE_BLOCK, block);

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

    private boolean isEnemy(Thing t, String team) {
        return t.type.equals(Thing.TYPE_ENTITY) && !t.details.equals(team);
    }

    @Override
    public void update(String supervisor) {
        this.supervisor = supervisor;
    }
}
