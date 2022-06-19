package de.feu.massim22.group3.agents.Desires.BDesires;

import java.util.ArrayList;
import java.util.List;
import eis.iilang.Action;

import de.feu.massim22.group3.agents.Belief;
import de.feu.massim22.group3.agents.DirectionUtil;
import de.feu.massim22.group3.utils.logging.AgentLogger;
import massim.protocol.data.Thing;

import java.awt.Point;

public abstract class BeliefDesire implements IDesire {
    protected Belief belief;
    protected List<IDesire> precondition = new ArrayList<>();
    private int moveIteration = 0;
    
    public BeliefDesire(Belief belief) {
        this.belief = belief;
    }

    //Melinda
    private Action outputAction;
    @Override
    public void setOutputAction(Action action) {
        this.outputAction = action;
    }
    @Override
    public Action getOutputAction() {
        return this.outputAction;
    }
    //Melinda Ende

    protected ActionInfo fullfillPreconditions() {
        for (IDesire d : precondition) {
            if (!d.isFulfilled().value()) {
                AgentLogger.info("Next action for agent " + belief.getAgentName() + " from " + d.getName());
                return d.getNextActionInfo();
            }
        }
        return null;
    }

    @Override
    public BooleanInfo isExecutable() {
        for (IDesire d : precondition) {
            BooleanInfo r = d.isExecutable();
            BooleanInfo f = d.isFulfilled();
            if (!r.value() && !f.value()) {
                AgentLogger.info(d.getName() + " is not executable for " + belief.getAgentName());
                return r;
            }
        }
        return new BooleanInfo(true, "");
    }

    @Override
    public String getName() {
        return getClass().getSimpleName();
    }

    @Override
    public ActionInfo getNextActionInfo() {
        return null;
    }

    @Override
    public void update(String supervisor) {
        moveIteration = 0;
        for (IDesire d: precondition) {
            d.update(supervisor);
        }
    }

    @Override
    public int getPriority() {
        return 0;
    }

    private ActionInfo getIteratedActionForMove(String dir, String desire) {
        moveIteration++;
        if (moveIteration < 4) {
            return getActionForMove(dir, desire);
        }
        // TODO AGENT is STuck
        return ActionInfo.SKIP("Agent is Stuck");
    }

    protected ActionInfo getActionForMove(String dir, String desire) {
        Point dirPoint = DirectionUtil.getCellInDirection(dir);
        List<Point> attached = belief.getAttachedPoints();
        // Rotate attached
        for (Point p : attached) {
            Point testPoint = new Point(p.x + dirPoint.x, p.y + dirPoint.y);
            Thing t = belief.getThingAt(testPoint);
            AgentLogger.info(Thread.currentThread().getName() + " getActionForMove - Direction: " + dir + " , Block attached: " + p + " , in Richtung: " + testPoint);
            if (!isFree(t) && !testPoint.equals(new Point(0, 0))) {
                // Can be rotated
                Thing cw = belief.getThingCRotatedAt(p);
                Thing ccw = belief.getThingCCRotatedAt(p);
                Point cwP = getCRotatedPoint(p);
                Point ccwP = getCCRotatedPoint(p);
                AgentLogger.info(Thread.currentThread().getName() + " getActionForMove - cw: " + cwP + " , ccw: " + ccwP);
 
                //Melinda
                if (DirectionUtil.getCellInDirection(DirectionUtil.oppositeDirection(dir)).equals(cwP)) {
                    if (isFree(cw)) {
                        return ActionInfo.ROTATE_CW(desire);
                    } else {
                        if (isFree(ccw)) {
                            return ActionInfo.ROTATE_CCW(desire);
                        }
                    }
                } else {
                    if (isFree(ccw)) {
                        return ActionInfo.ROTATE_CCW(desire);
                    } else {
                        if (isFree(cw)) {
                            return ActionInfo.ROTATE_CW(desire);
                        }
                    }                    
                }
                
                if (cw != null && cw.type.equals(Thing.TYPE_OBSTACLE) && !cwP.equals(dirPoint)) {
                    Point target = DirectionUtil.rotateCW(p);
                    return ActionInfo.CLEAR(target, desire);
                }
                if (ccw != null && ccw.type.equals(Thing.TYPE_OBSTACLE) && !ccwP.equals(dirPoint)) {
                    Point target = DirectionUtil.rotateCW(p);
                    return ActionInfo.CLEAR(target, desire);
                }
                //Melinda Ende
            }
        }
        // Test Agent
        Thing t = belief.getThingAt(dirPoint);
        if (t != null && t.type.equals(Thing.TYPE_OBSTACLE)) {
            return ActionInfo.CLEAR(dirPoint, desire);
        } else if (isFree(t) || attached.contains(dirPoint)) {
            return ActionInfo.MOVE(dir, desire);
        } else if (t != null && (t.type.equals(Thing.TYPE_ENTITY)
                //Melinda
                || (t.type.equals(Thing.TYPE_BLOCK) && !attached.contains(dirPoint)))) {
            //Melinda Ende
            // Try to move around agent
            boolean inDirection = true; // dir.equals("n") || dir.equals("e");
            String dir1 = inDirection ? getCRotatedDirection(dir) : getCCRotatedDirection(dir);
            String dir2 = inDirection ? getCCRotatedDirection(dir) : getCRotatedDirection(dir);
            Thing tDir1 = belief.getThingAt(dir1);
            Thing tDir2 = belief.getThingAt(dir2);

            if (isFree(tDir1) || isClearable(tDir1)) {
                return getIteratedActionForMove(dir1, desire);
            }

            if (isFree(tDir2) || isClearable(tDir2)) {
                return getIteratedActionForMove(dir2, desire);
            }
            return ActionInfo.SKIP("Agent is stuck");

        } else {
            return ActionInfo.SKIP("Agent is stuck");
        }
    }

    protected String getDirectionToRelativePoint(Point p) {
        if (p == null) {
            return "";
        }
        if (p.x == 0) {
            return p.y < 0 ? "n" : "s";
        }
        if (p.y == 0) {
            return p.x < 0 ? "w" : "e";
        }
        // Avoid obstacles if possible
        Thing n = belief.getThingAt("n");
        Thing s = belief.getThingAt("s");
        Thing e = belief.getThingAt("e");
        Thing w = belief.getThingAt("w");
        if (p.x < 0 && isFree(w)) {
            return "w";
        }
        if (p.x > 0 && isFree(e)) {
            return "e";
        }
        if (p.y < 0 && isFree(n)) {
            return "n";
        }
        if (p.y > 0 && isFree(s)) {
            return "s";
        }
        // Go through wall
        if (p.x < 0) {
            return "w";
        }
        if (p.x > 0) {
            return "e";
        }
        if (p.y < 0) {
            return "n";
        }
        return "s";
    }

    protected boolean isFree(Thing t) {
        return t == null || t.type.equals(Thing.TYPE_DISPENSER);
    }

    protected boolean isClearable(Thing t) {
        return t != null && (t.type.equals(Thing.TYPE_BLOCK) || t.type.equals(Thing.TYPE_OBSTACLE));
    }

    public BooleanInfo isUnfulfillable() {
        return new BooleanInfo(false, "");
    }

    protected Point getCRotatedPoint(Point p) {
        return new Point(-p.y, p.x);
    }

    protected String getCRotatedDirection(String dir) {
        switch (dir) {
            case "n": return "e";
            case "e": return "s";
            case "s": return "w";
            default: return "n";
        }
    }

    protected String getCCRotatedDirection(String dir) {
        switch (dir) {
            case "n": return "w";
            case "e": return "n";
            case "s": return "e";
            default: return "s";
        }
    }

    protected String getDirectionFromPoint(Point p) {
        if (p.x == 0) {
            return p.y < 0 ? "n" : "s";
        }
        return p.x < 0 ? "w" : "e";
    }

    protected Point getPointFromDirection(String dir) {
        switch (dir) {
            case "n": return new Point(0, -1);
            case "e": return new Point(1, 0);
            case "s": return new Point(0, 1);
            default: return new Point(-1, 0);
        }
    }

    public Point getCCRotatedPoint(Point p) {
        return new Point(p.y, -p.x);
    }
}
