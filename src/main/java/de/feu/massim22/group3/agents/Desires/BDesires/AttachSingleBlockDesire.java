package de.feu.massim22.group3.agents.Desires.BDesires;

import de.feu.massim22.group3.agents.Belief;
import de.feu.massim22.group3.agents.DirectionUtil;
import de.feu.massim22.group3.agents.Reachable.ReachableDispenser;
import de.feu.massim22.group3.map.CellType;
import de.feu.massim22.group3.utils.Convert;
import eis.iilang.Action;
import eis.iilang.Identifier;
import massim.protocol.data.Thing;

public class AttachSingleBlockDesire extends BeliefDesire {

    private Thing block;
    private CellType dispenser;

    public AttachSingleBlockDesire(Belief belief, Thing block) {
        super(belief);
        this.block = block;
        this.dispenser = Convert.blockNameToDispenser(block);
    }

    @Override
    public boolean isFullfilled() {
        for (Thing t : belief.getAttachedThings()) {
            if (Math.abs(t.x) <= 1 && Math.abs(t.y) <= 1 && t.type.equals(Thing.TYPE_BLOCK)
                && t.details.equals(block.type)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean isExecutable() {
        return belief.getNearestDispenser(dispenser) != null;
    }

    @Override
    public Action getNextAction() {
        // Request
        Thing n = belief.getThingAt("n");
        Thing e = belief.getThingAt("e");
        Thing s = belief.getThingAt("s");
        Thing w = belief.getThingAt("w");
        if (n != null && n.type.equals(Thing.TYPE_DISPENSER) && n.details.equals(block.type)) {
            return new Action("request", new Identifier("n"));
        }
        if (e != null && e.type.equals(Thing.TYPE_DISPENSER) && e.details.equals(block.type)) {
            return new Action("request", new Identifier("e"));
        }
        if (s != null && s.type.equals(Thing.TYPE_DISPENSER) && s.details.equals(block.type)) {
            return new Action("request", new Identifier("s"));
        }
        if (w != null && w.type.equals(Thing.TYPE_DISPENSER) && w.details.equals(block.type)) {
            return new Action("request", new Identifier("w"));
        }
        // Attach
        if (n != null && n.type.equals(Thing.TYPE_BLOCK) && n.details.equals(block.type)) {
            return new Action("attach", new Identifier("n"));
        }
        if (e != null && e.type.equals(Thing.TYPE_BLOCK) && e.details.equals(block.type)) {
            return new Action("attach", new Identifier("e"));
        }
        if (s != null && s.type.equals(Thing.TYPE_BLOCK) && s.details.equals(block.type)) {
            return new Action("attach", new Identifier("s"));
        }
        if (w != null && w.type.equals(Thing.TYPE_BLOCK) && w.details.equals(block.type)) {
            return new Action("attach", new Identifier("w"));
        }
        
        // Move
        ReachableDispenser rd = belief.getNearestDispenser(dispenser);
        String dir = DirectionUtil.intToString(rd.direction());
        return new Action("move", new Identifier(dir.substring(0, 1)));
    }
}
