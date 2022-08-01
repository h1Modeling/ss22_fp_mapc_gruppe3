package de.feu.massim22.group3.agents.Desires;

import java.awt.Point;

import de.feu.massim22.group3.agents.Belief;
import de.feu.massim22.group3.agents.DirectionUtil;
import de.feu.massim22.group3.agents.Reachable.ReachableDispenser;
import de.feu.massim22.group3.map.CellType;
import de.feu.massim22.group3.map.INaviAgentV1;
import de.feu.massim22.group3.map.Navi;
import de.feu.massim22.group3.utils.Convert;
import de.feu.massim22.group3.utils.logging.AgentLogger;
import massim.protocol.data.Thing;

public class AttachSingleBlockFromDispenserDesire extends BeliefDesire {

    private Thing block;
    private CellType dispenser;
    private String supervisor;

    public AttachSingleBlockFromDispenserDesire(Belief belief, Thing block, String supervisor) {
        super(belief);
        //AgentLogger.info(Thread.currentThread().getName() + " runSupervisorDecisions - Start AttachSingleBlockFromDispenserDesire");
        this.block = block;
        this.dispenser = Convert.blockNameToDispenser(block);
        this.supervisor = supervisor;
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
        Point p = belief.getNearestRelativeManhattenDispenser(block.type);
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
        /*
        // Attach (only if no other agent with higher number is present to avoid double connection)
        Point pos = belief.getPosition();
        String name = belief.getAgentShortName();
        int id = Integer.parseInt(name.substring(1));
        ActionInfo skip = ActionInfo.SKIP("waiting for other agent");

        // North
        if (nb != null && n != null && n.details.equals(block.type)) {
            Point de = new Point(pos.x + 1, pos.y - 1);
            Point dn = new Point(pos.x, pos.y - 2);
            Point dw = new Point(pos.x - 1, pos.y - 1);
            int deId = Navi.<INaviAgentV1>get().getAgentIdAtPoint(supervisor, de);
            int dnId = Navi.<INaviAgentV1>get().getAgentIdAtPoint(supervisor, dn);
            int dwId = Navi.<INaviAgentV1>get().getAgentIdAtPoint(supervisor, dw);
            return id > deId && id > dnId && id > dwId ? ActionInfo.ATTACH("n", getName()) : skip;
        }
        // East
        if (eb != null && e != null && e.details.equals(block.type)) {
            Point dn = new Point(pos.x + 1, pos.y - 1);
            Point ds = new Point(pos.x + 1, pos.y + 1);
            Point de = new Point(pos.x + 2, pos.y);
            int deId = Navi.<INaviAgentV1>get().getAgentIdAtPoint(supervisor, de);
            int dsId = Navi.<INaviAgentV1>get().getAgentIdAtPoint(supervisor, ds);
            int dnId = Navi.<INaviAgentV1>get().getAgentIdAtPoint(supervisor, dn);
            return id > deId && id > dnId && id > dsId ? ActionInfo.ATTACH("e", getName()) : skip;
        }
        // South
        if (sb != null && s != null && s.details.equals(block.type)) {
            Point de = new Point(pos.x + 1, pos.y - 1);
            Point ds = new Point(pos.x, pos.y + 2);
            Point dw = new Point(pos.x - 1, pos.y - 1);
            int dwId = Navi.<INaviAgentV1>get().getAgentIdAtPoint(supervisor, dw);
            int dsId = Navi.<INaviAgentV1>get().getAgentIdAtPoint(supervisor, ds);
            int deId = Navi.<INaviAgentV1>get().getAgentIdAtPoint(supervisor, de);
            return id > dwId && id > deId && id > dsId ? ActionInfo.ATTACH("s", getName()) : skip;
        }
        // West
        if (wb != null && w != null && w.details.equals(block.type)) {
            Point dn = new Point(pos.x - 1, pos.y - 1);
            Point ds = new Point(pos.x - 1, pos.y + 1);
            Point dw = new Point(pos.x - 2, pos.y);
            int dwId = Navi.<INaviAgentV1>get().getAgentIdAtPoint(supervisor, dw);
            int dsId = Navi.<INaviAgentV1>get().getAgentIdAtPoint(supervisor, ds);
            int dnId = Navi.<INaviAgentV1>get().getAgentIdAtPoint(supervisor, dn);
            return id > dwId && id > dnId && id > dsId ? ActionInfo.ATTACH("w", getName()) : skip;
        }
        */
        // Move
        Point p = belief.getNearestRelativeManhattenDispenser(block.type);
        int manhattenDistance = p != null ? Math.abs(p.x) + Math.abs(p.y) : 1000;
        ReachableDispenser rd = belief.getNearestDispenser(dispenser);
        // From Pathfinding
        if (rd != null && rd.distance() < 6 * manhattenDistance) {
            String dir = DirectionUtil.intToString(rd.direction());
            if (dir.length() > 0) {
                return getActionForMove(dir.substring(0, 1), getName());
            }
        }

        // Manhatten
        String dir = getDirectionToRelativePoint(p);
        return getActionForMove(dir, getName());
    }

    @Override
    public void update(String supervisor) {
        this.supervisor = supervisor;
    }
}
