package de.feu.massim22.group3.agents.desires;

import java.util.ArrayList;
import java.util.List;
import eis.iilang.Action;
import de.feu.massim22.group3.agents.DirectionUtil;
import de.feu.massim22.group3.agents.belief.Belief;
import de.feu.massim22.group3.agents.belief.reachable.ReachableTeammate;
import de.feu.massim22.group3.map.INaviAgentV1;
import de.feu.massim22.group3.map.Navi;
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
    private String dir2;
    private boolean dir2Used = false;
    
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
                AgentLogger.info("Next action for agent " + belief.getAgentShortName() + " from " + d.getName());
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
                AgentLogger.info(d.getName() + " is not executable for " + belief.getAgentShortName());
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

    @Override
    public boolean isGroupDesire() {
        return false;
    }

    private ActionInfo getIteratedActionForMove(String dir, String desire) {
        moveIteration++;
        if (moveIteration < 4) {
            return getActionForMove(dir, desire);
        }
        // Try to clear obstacles around
        Thing n = belief.getThingAt("n");
        if (n != null && n.type.equals(Thing.TYPE_OBSTACLE)) return ActionInfo.CLEAR(new Point(0,-1), getName());
        Thing s = belief.getThingAt("s");
        if (s != null && s.type.equals(Thing.TYPE_OBSTACLE)) return ActionInfo.CLEAR(new Point(0, 1), getName());
        Thing e = belief.getThingAt("e");
        if (e != null && e.type.equals(Thing.TYPE_OBSTACLE)) return ActionInfo.CLEAR(new Point(1, 0), getName());
        Thing w = belief.getThingAt("w");
        if (w != null && w.type.equals(Thing.TYPE_OBSTACLE)) return ActionInfo.CLEAR(new Point(-1, 0), getName());
        
        // TODO AGENT is STuck
        return ActionInfo.SKIP("Agent is Stuck in getInteratedActionForMove");
    }

    protected ActionInfo getActionForMove(String dir, String desire) {
        Point dirPoint = DirectionUtil.getCellInDirection(dir);
        List<Point> attached = belief.getOwnAttachedPoints();

        // Rotate attached
        for (Point p : attached) {
            Point testPoint = new Point(p.x + dirPoint.x, p.y + dirPoint.y);
            Thing t = belief.getThingAt(testPoint);
            if (!isFree(t) && !testPoint.equals(new Point(0, 0))) {
                // Can be rotated
                Thing cw = belief.getThingCRotatedAt(p);
                Thing ccw = belief.getThingCCRotatedAt(p);
                Point cwP = getCRotatedPoint(p);
                Point ccwP = getCCRotatedPoint(p);
                Point cwP2 = new Point(cwP.x + dirPoint.x, cwP.y + dirPoint.y);
                Point ccwP2 = new Point(ccwP.x + dirPoint.x, ccwP.y + dirPoint.y);
                Thing cw2 = belief.getThingAt(cwP2);
                Thing ccw2 = belief.getThingAt(ccwP2);
                // Move away from direction if possible
                if (isFree(cw) && isFree(ccw)) {
                    if (isFree(cw) && !cwP.equals(dirPoint)) {
                        return ActionInfo.ROTATE_CW(desire);
                    }
                    if (isFree(ccw) && !ccwP.equals(dirPoint)) {
                        return ActionInfo.ROTATE_CCW(desire);
                    }
                }
                if (isFree(cw)) {
                    return ActionInfo.ROTATE_CW(desire);
                }
                if (isFree(ccw)) {
                    return ActionInfo.ROTATE_CCW(desire);
                }
                if (cw != null && cw.type.equals(Thing.TYPE_OBSTACLE) && !cwP.equals(dirPoint)) {
                    Point target = DirectionUtil.rotateCW(p);
                    return ActionInfo.CLEAR(target, desire);
                }
                if (ccw != null && ccw.type.equals(Thing.TYPE_OBSTACLE) && !ccwP.equals(dirPoint)) {
                    Point target = DirectionUtil.rotateCCW(p);
                    return ActionInfo.CLEAR(target, desire);
                }

                return ActionInfo.SKIP(desire);
            }
        }
        // Test Agent
        Thing t = belief.getThingAt(dirPoint);
        if (t != null && t.type.equals(Thing.TYPE_OBSTACLE)) {
            return ActionInfo.CLEAR(dirPoint, desire);
        } else if (isFree(t)|| attached.contains(dirPoint)) {
            return ActionInfo.MOVE(dir, desire);
        } else if (t != null && t.type.equals(Thing.TYPE_ENTITY)) {
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
            return ActionInfo.SKIP("Agent is stuck in getActionForMove avoiding Agent");

        } else {
            // Try to move in different direction to improve situation
            if (attached.size() > 0) {
                // Move in oposite direction of attached block
                Point p = attached.get(0);
                Point newDir = new Point(-p.x, -p.y);
                String newDirString = getDirectionFromPoint(newDir);
                return getIteratedActionForMove(newDirString, desire);
            }
            return ActionInfo.SKIP("Agent is stuck in getActionForMove");
        }
    }

/*     protected boolean getActionForMove2(String dir, String desire) {
        Point dirPoint = DirectionUtil.getCellInDirection(dir);
        List<Point> attached = belief.getAttachedPoints();
        // Rotate attached
        for (Point p : attached) {
            Point testPoint = new Point(p.x + dirPoint.x, p.y + dirPoint.y);
            
            Thing t = belief.getThingAt(testPoint);
            //if (t.type.equals(Thing.TYPE_OBSTACLE)) return false;
            if (!isFree(t) && !testPoint.equals(new Point(0, 0))) {
                return false;
            }
        }
        return true;
    } */

    protected ActionInfo getActionForMove(Point absolutePoint, String desire) {
        Point position = belief.getPosition();
        Point relativePoint = new Point(absolutePoint.x - position.x, absolutePoint.y - position.y);
        String dir = getDirectionToRelativePoint(relativePoint);
        return getActionForMove(dir, desire);
    }

    protected ActionInfo getActionForCWRotation(String desire) {
        var things = belief.getAttachedThings();
        if (things.size() > 0) {
            Thing t = things.get(0);
            Point p = getCRotatedPoint(new Point(t.x, t.y));
            // Rotate CW
            if (isFreeInVision(p)) {
                return ActionInfo.ROTATE_CW(desire);
            }
            Thing atP = belief.getThingAt(p);
            // Clear
            if (atP.type.equals(Thing.TYPE_OBSTACLE)) {
                return ActionInfo.CLEAR(p, desire);
            }
            // Rotate CCW
            Point ccP = getCCRotatedPoint(new Point(t.x, t.y));
            if (isFreeInVision(ccP)) {
                return ActionInfo.ROTATE_CCW(desire);
            }
            Thing atCcP = belief.getThingAt(ccP);
            // Clear
            if (atCcP.type.equals(Thing.TYPE_OBSTACLE)) {
                return ActionInfo.CLEAR(ccP, desire);
            }
            return ActionInfo.SKIP(desire);
        }
        return ActionInfo.ROTATE_CW(desire);
    }

    protected ActionInfo getActionForCCWRotation(String desire) {
        var things = belief.getAttachedThings();
        if (things.size() > 0) {
            Thing t = things.get(0);
            Point p = getCCRotatedPoint(new Point(t.x, t.y));
            // Rotate CCW
            if (isFreeInVision(p)) {
                return ActionInfo.ROTATE_CCW(desire);
            }
            Thing atP = belief.getThingAt(p);
            // Clear
            if (atP.type.equals(Thing.TYPE_OBSTACLE)) {
                return ActionInfo.CLEAR(p, desire);
            }
            // Rotate CW
            Point cP = getCRotatedPoint(new Point(t.x, t.y));
            if (isFreeInVision(cP)) {
                return ActionInfo.ROTATE_CW(desire);
            }
            Thing atCP = belief.getThingAt(cP);
            // Clear
            if (atCP.type.equals(Thing.TYPE_OBSTACLE)) {
                return ActionInfo.CLEAR(cP, desire);
            }
            return ActionInfo.SKIP(desire);
        }
        return ActionInfo.ROTATE_CW(desire);
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
        return t == null || t.type.equals(Thing.TYPE_DISPENSER) || (t.x == 0 && t.y == 0);
    }

    protected boolean isFreeInVision(Point p) {
        for (Thing t: belief.getThings()) {
            if (t.x == p.x && t.y == p.y && !isFree(t)) {
                return false;
            }
        }
        return true;
    }

    protected boolean isClearable(Thing t) {
        return t != null && (t.type.equals(Thing.TYPE_BLOCK) || t.type.equals(Thing.TYPE_OBSTACLE));
    }

    public BooleanInfo isUnfulfillable() {
        for (IDesire d : precondition) {
            if (d.isUnfulfillable().value()) {
                return d.isUnfulfillable();
            }
        }
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
        if (p.x == 0 || Math.abs(p.y) > Math.abs(p.x)) {
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

    protected int getDistance(Point p) {
        return Math.abs(p.x) + Math.abs(p.y);
    }

    protected int getDistance(Thing t) {
        return Math.abs(t.x) + Math.abs(t.y);
    }

    protected int getDistance(Point a, Point b) {
        return Math.abs(a.x - b.x) + Math.abs(a.y - b.y);
    }

    protected int getDistanceToAgent(String agent) {
        List<ReachableTeammate> mates = belief.getReachableTeammates();
        for (ReachableTeammate m : mates) {
            if (m.name().equals(agent)) {
                return m.distance();
            }
        }
        return 0;
    }

    protected Point getAgentPosition(String agent) {
        List<ReachableTeammate> mates = belief.getReachableTeammates();
        for (ReachableTeammate m : mates) {
            if (m.name().equals(agent)) {
                return m.position();
            }
        }
        return null;
    }

    protected int getBiggestAdjacentAgentId(Point p, String supervisor) {
        Point e = new Point(p.x + 1, p.y);
        Point n = new Point(p.x, p.y - 1);
        Point w = new Point(p.x - 1, p.y);
        Point s = new Point(p.x, p.y + 1);
        int eId = Navi.<INaviAgentV1>get().getAgentIdAtPoint(supervisor, e);
        int nId = Navi.<INaviAgentV1>get().getAgentIdAtPoint(supervisor, n);
        int wId = Navi.<INaviAgentV1>get().getAgentIdAtPoint(supervisor, w);
        int sId = Navi.<INaviAgentV1>get().getAgentIdAtPoint(supervisor, s);
        return Math.max(Math.max(Math.max(eId, nId), wId), sId);
    }

    protected int getBiggestAdjacentAgentId(Thing t, String supervisor) {
        if (t == null) return 0;
        return getBiggestAdjacentAgentId(new Point(t.x, t.y), supervisor);
    }

    public Point getCCRotatedPoint(Point p) {
        return new Point(p.y, -p.x);
    }


}
