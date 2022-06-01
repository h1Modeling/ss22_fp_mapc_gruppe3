package de.feu.massim22.group3.agents.Desires.BDesires;

import java.awt.Point;

import de.feu.massim22.group3.agents.Belief;
import eis.iilang.Action;
import eis.iilang.Identifier;
import massim.protocol.data.TaskInfo;
import massim.protocol.data.Thing;

public class GetBlocksInOrderDesire extends BeliefDesire {

    private TaskInfo info;
    
    public GetBlocksInOrderDesire(Belief belief, TaskInfo info) {
        super(belief);
        this.info = info;
    }

    @Override
    public boolean isFullfilled() {
        for (Thing t : info.requirements) {
            Thing atAgent = belief.getThingAt(new Point(t.x, t.y));
            if (atAgent == null || !atAgent.type.equals(Thing.TYPE_BLOCK) || !atAgent.details.equals(t.type)) {
                return false;
            }
        }
        System.out.println("is fulfilled !!!!!!!");
        return true;
    }

    @Override
    public Action getNextAction() {
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
            if (goal.x != 1 && w == null) {
                return new Action("rotate", new Identifier("ccw"));
            }
            if (goal.x != -1 && e == null) {
                return new Action("rotate", new Identifier("cw"));
            }
            if (nn == null && belief.getGoalZones().contains(new Point(0, -1))) {
                return new Action("move", new Identifier("n"));
            }
            if (w == null && nw == null && belief.getGoalZones().contains(new Point(-1, 0))) {
                return new Action("move", new Identifier("w"));
            }
            if (e == null && ne == null && belief.getGoalZones().contains(new Point(1, 0))) {
                return new Action("move", new Identifier("e"));
            }
        }
        // Attached is south
        if (t.x == 0 && t.y == 1) {
            if (goal.x != 1 && w == null) {
                return new Action("rotate", new Identifier("cw"));
            }
            if (goal.x != -1 && e == null) {
                return new Action("rotate", new Identifier("ccw"));
            }
            if (ss == null && belief.getGoalZones().contains(new Point(0, 1))) {
                return new Action("move", new Identifier("s"));
            }
            if (w == null && sw == null && belief.getGoalZones().contains(new Point(-1, 0))) {
                return new Action("move", new Identifier("w"));
            }
            if (e == null && se == null && belief.getGoalZones().contains(new Point(1, 0))) {
                return new Action("move", new Identifier("e"));
            }
        }
        // Attached is east
        if (t.x == 1 && t.y == 0) {
            if (goal.y != 1 && n == null) {
                return new Action("rotate", new Identifier("ccw"));
            }
            if (goal.y != -1 && s == null) {
                return new Action("rotate", new Identifier("cw"));
            }
            if (ee == null && belief.getGoalZones().contains(new Point(1, 0))) {
                return new Action("move", new Identifier("e"));
            }
            if (n == null && ne == null && belief.getGoalZones().contains(new Point(0, -1))) {
                return new Action("move", new Identifier("n"));
            }
            if (s == null && se == null && belief.getGoalZones().contains(new Point(0, 1))) {
                return new Action("move", new Identifier("s"));
            }
        }
        // Attached is west
        if (t.x == -1 && t.y == 0) {
            if (goal.y != 1 && n == null) {
                return new Action("rotate", new Identifier("cw"));
            }
            if (goal.y != -1 && s == null) {
                return new Action("rotate", new Identifier("ccw"));
            }
            if (ww == null && belief.getGoalZones().contains(new Point(-1, 0))) {
                return new Action("move", new Identifier("w"));
            }
            if (n == null && ne == null && belief.getGoalZones().contains(new Point(0, -1))) {
                return new Action("move", new Identifier("n"));
            }
            if (s == null && se == null && belief.getGoalZones().contains(new Point(0, 1))) {
                return new Action("move", new Identifier("s"));
            }
        }
        return null;
    }
}
