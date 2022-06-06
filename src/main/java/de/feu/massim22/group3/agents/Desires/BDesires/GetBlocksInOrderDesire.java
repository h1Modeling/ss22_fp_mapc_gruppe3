package de.feu.massim22.group3.agents.Desires.BDesires;

import java.awt.Point;

import de.feu.massim22.group3.agents.Belief;
import massim.protocol.data.TaskInfo;
import massim.protocol.data.Thing;

public class GetBlocksInOrderDesire extends BeliefDesire {

    private TaskInfo info;
    
    public GetBlocksInOrderDesire(Belief belief, TaskInfo info) {
        super(belief);
        this.info = info;
    }

    @Override
    public BooleanInfo isFullfilled() {
        for (Thing t : info.requirements) {
            Thing atAgent = belief.getThingAt(new Point(t.x, t.y));
            if (atAgent == null || !atAgent.type.equals(Thing.TYPE_BLOCK) || !atAgent.details.equals(t.type)) {
                String ea = atAgent == null ? t.details + " not at agent" : "";
                String et = atAgent != null && !atAgent.type.equals(Thing.TYPE_BLOCK) ?  "Attached is no block" : "";
                String ed = atAgent != null && !atAgent.details.equals(t.type) ? "Wrong Block attached" : "";
                return new BooleanInfo(false, ea + et + ed);
            }
        }
        return new BooleanInfo(true, "");
    }

    @Override
    public ActionInfo getNextActionInfo() {
        Thing t = belief.getAttachedThings().get(0);
        Thing n = belief.getThingAt("n");
        Thing nn = belief.getThingAt(new Point(0, -2));
        Thing nw = belief.getThingAt(new Point(-1, -1));
        Thing ne = belief.getThingAt(new Point(1, -1));
        Thing e = belief.getThingAt("e");
        Thing ee = belief.getThingAt(new Point(2, 0));
        Thing s = belief.getThingAt("s");
        Thing ss = belief.getThingAt(new Point(0, 2));
        Thing sw = belief.getThingAt(new Point(-1, 1));
        Thing se = belief.getThingAt(new Point(1, 1));
        Thing w = belief.getThingAt("w");
        Thing ww = belief.getThingAt(new Point(-2, 0));
        Thing goal = info.requirements.get(0);
        // Attached is north
        if (t.x == 0 && t.y == -1) {
            if (goal.x != 1 && isFree(w)) {
                return ActionInfo.ROTATE_CCW(getName());
            }
            if (goal.x != -1 && isFree(e)) {
                return ActionInfo.ROTATE_CW(getName());
            }
            if (isFree(nn) && belief.getGoalZones().contains(new Point(0, -1))) {
                return ActionInfo.MOVE("n", getName());
            }
            if (isFree(w) && isFree(nw) && belief.getGoalZones().contains(new Point(-1, 0))) {
                return ActionInfo.MOVE("w", getName());
            }
            if (isFree(e) && isFree(ne) && belief.getGoalZones().contains(new Point(1, 0))) {
                return ActionInfo.MOVE("e", getName());
            }
        }
        // Attached is south
        if (t.x == 0 && t.y == 1) {
            if (goal.x != 1 && isFree(w)) {
                return ActionInfo.ROTATE_CW(getName());
            }
            if (goal.x != -1 && isFree(e)) {
                return ActionInfo.ROTATE_CCW(getName());
            }
            if (isFree(ss) && belief.getGoalZones().contains(new Point(0, 1))) {
                return ActionInfo.MOVE("s", getName());
            }
            if (isFree(w) && isFree(sw) && belief.getGoalZones().contains(new Point(-1, 0))) {
                return ActionInfo.MOVE("w", getName());
            }
            if (isFree(e) && isFree(se) && belief.getGoalZones().contains(new Point(1, 0))) {
                return ActionInfo.MOVE("e", getName());
            }
        }
        // Attached is east
        if (t.x == 1 && t.y == 0) {
            if (goal.y != 1 && isFree(n)) {
                return ActionInfo.ROTATE_CCW(getName());
            }
            if (goal.y != -1 && isFree(s)) {
                return ActionInfo.ROTATE_CW(getName());
            }
            if (isFree(ee) && belief.getGoalZones().contains(new Point(1, 0))) {
                return ActionInfo.MOVE("e", getName());
            }
            if (isFree(n) && isFree(ne) && belief.getGoalZones().contains(new Point(0, -1))) {
                return ActionInfo.MOVE("n", getName());
            }
            if (isFree(s) && isFree(se) && belief.getGoalZones().contains(new Point(0, 1))) {
                return ActionInfo.MOVE("s", getName());
            }
        }
        // Attached is west
        if (t.x == -1 && t.y == 0) {
            if (goal.y != 1 && isFree(n)) {
                return ActionInfo.ROTATE_CW(getName());
            }
            if (goal.y != -1 && isFree(s)) {
                return ActionInfo.ROTATE_CCW(getName());
            }
            if (isFree(ww) && belief.getGoalZones().contains(new Point(-1, 0))) {
                return ActionInfo.MOVE("w", getName());
            }
            if (isFree(n) && isFree(ne) && belief.getGoalZones().contains(new Point(0, -1))) {
                return ActionInfo.MOVE("n", getName());
            }
            if (isFree(s) && isFree(se) && belief.getGoalZones().contains(new Point(0, 1))) {
                return ActionInfo.MOVE("s", getName());
            }
        }
        return ActionInfo.CLEAR(new Point(goal.x, goal.y), getName());
    }
}
