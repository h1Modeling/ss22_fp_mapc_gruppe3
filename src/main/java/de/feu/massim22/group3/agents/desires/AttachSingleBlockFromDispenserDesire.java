package de.feu.massim22.group3.agents.desires;

import java.awt.Point;

import de.feu.massim22.group3.agents.belief.Belief;
import de.feu.massim22.group3.agents.belief.reachable.ReachableDispenser;
import de.feu.massim22.group3.map.CellType;
import de.feu.massim22.group3.utils.Convert;
import de.feu.massim22.group3.utils.DirectionUtil;
import massim.protocol.data.Thing;

public class AttachSingleBlockFromDispenserDesire extends BeliefDesire {

    private Thing block;
    private CellType dispenser;

    public AttachSingleBlockFromDispenserDesire(Belief belief, Thing block) {
        super(belief);
        this.block = block;
        this.dispenser = Convert.blockNameToDispenser(block);
    }

    @Override
    public BooleanInfo isFulfilled() {
        for (Thing t : belief.getAttachedThings()) {
            if (Math.abs(t.x) <= 1 && Math.abs(t.y) <= 1 && t.type.equals(Thing.TYPE_BLOCK)
                && t.details.equals(block.type)) {
                return new BooleanInfo(true, "");
            }
        }
        return new BooleanInfo(false, "No block " + block.type + " attached");
    }

    @Override
    public BooleanInfo isExecutable() {
        ReachableDispenser rd = belief.getNearestDispenser(dispenser);
        Point p = belief.getNearestRelativeManhattanDispenser(block.type);
        boolean value = rd != null || p != null;
        String info = !value ? "No dispenser " + block.type + " visible" : ""; 
        return new BooleanInfo(value, info);
    }

    @Override
    public ActionInfo getNextActionInfo() {
        // Request from Dispenser
        Thing n = belief.getThingWithTypeAt("n", Thing.TYPE_DISPENSER);
        Thing e = belief.getThingWithTypeAt("e", Thing.TYPE_DISPENSER);
        Thing s = belief.getThingWithTypeAt("s", Thing.TYPE_DISPENSER);
        Thing w = belief.getThingWithTypeAt("w", Thing.TYPE_DISPENSER);
        Thing nb = belief.getThingWithTypeAt("n", Thing.TYPE_BLOCK);
        Thing eb = belief.getThingWithTypeAt("e", Thing.TYPE_BLOCK);
        Thing sb = belief.getThingWithTypeAt("s", Thing.TYPE_BLOCK);
        Thing wb = belief.getThingWithTypeAt("w", Thing.TYPE_BLOCK);

        // Request
        if (n != null && nb == null && n.details.equals(block.type)) {
            return ActionInfo.REQUEST("n", getName());
        }
        if (e != null && eb == null && e.details.equals(block.type)) {
            return ActionInfo.REQUEST("e", getName());
        }
        if (s != null && sb == null && s.details.equals(block.type)) {
            return ActionInfo.REQUEST("s", getName());
        }
        if (w != null && wb == null && w.details.equals(block.type)) {
            return ActionInfo.REQUEST("w", getName());
        }

        // Move
        Point p = belief.getNearestRelativeManhattanDispenser(block.type);
        int manhattenDistance = p != null ? Math.abs(p.x) + Math.abs(p.y) : 1000;
        ReachableDispenser rd = belief.getNearestDispenser(dispenser);
        // From Pathfinding
        if (rd != null && rd.distance() < 10 * manhattenDistance) {
            String dir = DirectionUtil.intToString(rd.direction());
            if (dir.length() > 0) {
                return getActionForMove(dir, getName());
            }
        }

        // Manhatten
        String dir = getDirectionToRelativePoint(p);
        return getActionForMove(dir, getName());
    }
}
