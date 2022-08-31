package de.feu.massim22.group3.agents.desires;

import java.util.ArrayList;
import java.util.List;
import eis.iilang.Action;
import de.feu.massim22.group3.agents.belief.Belief;
import de.feu.massim22.group3.agents.belief.reachable.ReachableTeammate;
import de.feu.massim22.group3.map.INaviAgentV1;
import de.feu.massim22.group3.map.Navi;
import de.feu.massim22.group3.utils.DirectionUtil;
import de.feu.massim22.group3.utils.logging.AgentLogger;
import massim.protocol.data.Thing;

import java.awt.Point;

/**
 * The abstract Class <code>BeliefDesire</code> defines a basic framework for implementing a desire which gets it's information
 * from an agents belief.
 * 
 * @author Heinz Stadler
 * @author Melinda Betz (minor contribution)
 */
public abstract class BeliefDesire implements IDesire {
    protected Belief belief;
    protected List<IDesire> precondition = new ArrayList<>();
    private Action outputAction;
    private int moveIteration = 0;

    /**
     * Instantiates a new BeliefDesire.
     * 
     * @param belief the belief of the agent
     */
    public BeliefDesire(Belief belief) {
        this.belief = belief;
    }
    

    /**
     * Fills the action cache with the calculated action.
     * 
     * @param action the calculated action
     */
    @Override
    public void setOutputAction(Action action) {
        this.outputAction = action;
    }

    /**
     * Gets the calculated action from the action cache.
     * 
     * @return the calculated action
     */
    @Override
    public Action getOutputAction() {
        return this.outputAction;
    }

    /**
     * Gets the next ActionInfo from the desires subdesires.
     * 
     * @return the next ActionInfo from the desires subdesires or null if all subdesires are already fulfilled.
     */
    protected ActionInfo fulfillPreconditions() {
        for (IDesire d : precondition) {
            if (!d.isFulfilled().value()) {
                AgentLogger.info("Next action for agent " + belief.getAgentShortName() + " from " + d.getName());
                return d.getNextActionInfo();
            }
        }
        return null;
    }

    /**
     * {@inheritDoc}
     */
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

    /**
     * {@inheritDoc}
     */
    @Override
    public String getName() {
        return getClass().getSimpleName();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ActionInfo getNextActionInfo() {
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void update(String supervisor) {
        moveIteration = 0;
        for (IDesire d: precondition) {
            d.update(supervisor);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getPriority() {
        return 0;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isGroupDesire() {
        return false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public BooleanInfo isUnfulfillable() {
        for (IDesire d : precondition) {
            if (d.isUnfulfillable().value()) {
                return d.isUnfulfillable();
            }
        }
        return new BooleanInfo(false, "");
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
        
        // TODO AGENT is stuck
        return ActionInfo.SKIP("Agent is Stuck in getIteratedActionForMove");
    }

    private boolean roleAllowsTwoCellMove() {
        var role = belief.getRole();
        if (role == null) return false;
        var attachedCount = belief.getOwnAttachedPoints().size();
        return role.maxSpeed(attachedCount) >= 2;
    }

    private boolean mapAllowsTwoCellMove(String dir) {
        List<Point> attached = belief.getOwnAttachedPoints();
        Point dirPoint1 = DirectionUtil.getCellInDirection(dir.substring(0, 1));
        Point dirPoint2 = DirectionUtil.getCellInDirection(dir.substring(1, 2));
        // Test attached
        for (Point p : attached) {
            Point testPoint1 = new Point(p.x + dirPoint1.x, p.y + dirPoint1.y);
            Point testPoint2 = new Point(p.x + dirPoint1.x + dirPoint2.x, p.y + dirPoint1.y + dirPoint2.y);
            Thing t1 = belief.getThingAt(testPoint1);
            Thing t2 = belief.getThingAt(testPoint2);
            if (!isFree(t1) && !testPoint1.equals(new Point(0, 0))) return false;
            if (!isFree(t2) && !testPoint2.equals(new Point(0, 0))) return false;
        }
        // Test agent
        Point testPoint1 = new Point(dirPoint1.x, dirPoint1.y);
        Point testPoint2 = new Point(dirPoint1.x + dirPoint2.x, dirPoint1.y + dirPoint2.y);
        Thing t1 = belief.getThingAt(testPoint1);
        Thing t2 = belief.getThingAt(testPoint2);
        if (!isFree(t1) && !testPoint1.equals(new Point(0, 0))) return false;
        if (!isFree(t2) && !testPoint2.equals(new Point(0, 0))) return false;
        return true;
    }

    /**
     * Gets an ActionInfo to perform a move in a provided direction.
     * The ActionInfo can contain a move, rotate, skip or clear depending on the surroundings of the agent.
     *  
     * @param directions one or multiple directions encoded into a String. Each character of the String defines a single move
     * @param desire the name of the desire which requests the ActionInfo
     * @return the calculated ActionInfo
     */
    protected ActionInfo getActionForMove(String directions, String desire) {
        String dir = directions.substring(0, 1);
        Point dirPoint = DirectionUtil.getCellInDirection(dir);

        // 2 Cell Move (only if direct move is possible)
        if (directions.length() > 1 && roleAllowsTwoCellMove() && mapAllowsTwoCellMove(directions)) {
            String dir2 = directions.substring(1, 2);
            return ActionInfo.MOVE(dir, dir2, desire);
        }

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
                return ActionInfo.SKIP("Can't move because of attached");
            }
        }
        // Test Agent
        Thing t = belief.getThingAt(dirPoint);
        // Clear Obstacle
        if (t != null && t.type.equals(Thing.TYPE_OBSTACLE)) {
            return ActionInfo.CLEAR(dirPoint, desire);
        // Move
        } else if (isFree(t)|| attached.contains(dirPoint)) {
            return ActionInfo.MOVE(dir, desire);
        // Try to move around Agent or Block
        } else if (t != null && (t.type.equals(Thing.TYPE_ENTITY) || t.type.equals(Thing.TYPE_BLOCK))) {
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
                // Move in opposite direction of attached block
                Point p = attached.get(0);
                Point newDir = new Point(-p.x, -p.y);
                String newDirString = getDirectionFromPoint(newDir);
                return getIteratedActionForMove(newDirString, desire);
            }
            return ActionInfo.SKIP("Agent is stuck in getActionForMove");
        }
    }

    /**
     * Tests if the agent can move in the provided direction only with a single move action.
     *  
     * @param direction the direction which should be tested
     * @return true if the agent can move in the provided direction
     */
    protected boolean straightMovePossible(String direction) {
        String dir = direction.substring(0, 1);
        Point dirPoint = DirectionUtil.getCellInDirection(dir);

        List<Point> attached = belief.getOwnAttachedPoints();

        for (Point p : attached) {
            Point testPoint = new Point(p.x + dirPoint.x, p.y + dirPoint.y);
            Thing t = belief.getThingAt(testPoint);
            if (!isFree(t) && !testPoint.equals(new Point(0, 0))) {
                return false;
            }
        }
        // Test Agent
        Thing t = belief.getThingAt(dirPoint);
        return t == null;
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

    /**
     * Gets an ActionInfo to perform a move to a provided Point.
     * The ActionInfo can contain a move, rotate, skip or clear depending on the surroundings of the agent.
     * 
     * @param absolutePoint the position to which the agent should move in the agents coordinate system
     * @param desire the name of the desire which requests the ActionInfo
     * @return the calculated ActionInfo
     */
    protected ActionInfo getActionForMove(Point absolutePoint, String desire) {
        Point position = belief.getPosition();
        Point relativePoint = new Point(absolutePoint.x - position.x, absolutePoint.y - position.y);
        String dir = getDirectionToRelativePoint(relativePoint);
        return getActionForMove(dir, desire);
    }

    /**
     * Gets an ActionInfo to perform a clock wise rotation.
     * The ActionInfo can contain a clear, rotate, or skip depending on the surroundings of the agent.
     * 
     * @param desire the name of the desire which requests the ActionInfo
     * @return the calculated ActionInfo
     */
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

    /**
     * Gets an ActionInfo to perform a counter clock wise rotation.
     * The ActionInfo can contain a clear, rotate, or skip depending on the surroundings of the agent.
     * 
     * @param desire the name of the desire which requests the ActionInfo
     * @return the calculated ActionInfo
     */
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

    /**
     * Gets the direction to a provided point.
     * 
     * @param p the Point to which the direction should be calculated
     * @return the direction to the Point
     */
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

    /**
     * Gets the direction from a Point to another Point.
     * 
     * @param from the Point from which the direction should be calculated 
     * @param to the Point to which the direction should be calculated
     * @return the calculated direction
     */
    protected String getDirectionFromAndToRelativePoint(Point from, Point to) {
        if (to == null || from.equals(to)) {
            return "";
        }
        if (to.x == from.x) {
            return to.y < from.y ? "n" : "s";
        }
        if (to.y == from.x) {
            return to.x < from.x ? "w" : "e";
        }
        // Avoid obstacles if possible
        Thing n = belief.getThingAt(new Point(from.x, from.y - 1));
        Thing s = belief.getThingAt(new Point(from.x, from.y + 1));
        Thing e = belief.getThingAt(new Point(from.x + 1, from.y));
        Thing w = belief.getThingAt(new Point(from.x - 1, from.y));
        if (to.x < from.x && isFree(w)) {
            return "w";
        }
        if (to.x > from.x && isFree(e)) {
            return "e";
        }
        if (to.y < from.y && isFree(n)) {
            return "n";
        }
        if (to.y > from.y && isFree(s)) {
            return "s";
        }
        // Go through wall
        if (to.x < from.x) {
            return "w";
        }
        if (to.x > from.x) {
            return "e";
        }
        if (to.y < from.y) {
            return "n";
        }
        return "s";
    }

    /**
     * Tests if the provided Thing is traversable.
     * 
     * @param t the Thing to test
     * @return true if the provided thing is traversable or if the thing is the agent itself
     */
    protected boolean isFree(Thing t) {
        return t == null || t.type.equals(Thing.TYPE_DISPENSER) || (t.x == 0 && t.y == 0);
    }

    /**
     * Test if the provided point in vision is traversable.
     * 
     * @param p the Point in vision
     * @return true if the provided position is traversable or if the position is at the agents position
     */
    protected boolean isFreeInVision(Point p) {
        for (Thing t: belief.getThings()) {
            if (t.x == p.x && t.y == p.y && !isFree(t)) {
                return false;
            }
        }
        return true;
    }

    /**
     * Tests if the provided Thing is clearable, is a block or an obstacle.
     * 
     * @param t the thing to test
     * @return true if the provided thing is a block or an obstacle
     */
    protected boolean isClearable(Thing t) {
        return t != null && (t.type.equals(Thing.TYPE_BLOCK) || t.type.equals(Thing.TYPE_OBSTACLE));
    }

    /**
     * Rotates a provided Point clock wise around the origin.
     * 
     * @param p the Point to rotate
     * @return the rotated Point
     */
    protected Point getCRotatedPoint(Point p) {
        return new Point(-p.y, p.x);
    }

    /**
     * Rotates a provided direction clock wise.
     * 
     * @param dir the direction to rotate
     * @return the rotated direction
     */
    protected String getCRotatedDirection(String dir) {
        switch (dir) {
            case "n": return "e";
            case "e": return "s";
            case "s": return "w";
            default: return "n";
        }
    }

    /**
     * Rotates a provided direction counter clock wise.
     * 
     * @param dir the direction to rotate
     * @return the rotated direction
     */
    protected String getCCRotatedDirection(String dir) {
        switch (dir) {
            case "n": return "w";
            case "e": return "n";
            case "s": return "e";
            default: return "s";
        }
    }

    /**
     * Translates a provided Point into a direction.
     * 
     * @param p the Point to translate
     * @return the direction at the point
     */
    protected String getDirectionFromPoint(Point p) {
        if (p.x == 0 || Math.abs(p.y) > Math.abs(p.x)) {
            return p.y < 0 ? "n" : "s";
        }
        return p.x < 0 ? "w" : "e";
    }

    /**
     * Translates a direction into a Point.
     * 
     * @param dir the direction to translate
     * @return the point at the direction
     */
    protected Point getPointFromDirection(String dir) {
        switch (dir) {
            case "n": return new Point(0, -1);
            case "e": return new Point(1, 0);
            case "s": return new Point(0, 1);
            default: return new Point(-1, 0);
        }
    }

    /**
     * Gets the Manhattan distance from the origin to the provided point.
     * 
     * @param p the Point to which the distance should be calculated
     * @return the Manhattan distance to the point
     */
    protected int getDistance(Point p) {
        return Math.abs(p.x) + Math.abs(p.y);
    }

    /**
     * Gets the Manhattan distance from the origin to the provided Thing.
     * 
     * @param t the Thing to which the distance should be calculated
     * @return the Manhattan distance to the thing
     */
    protected int getDistance(Thing t) {
        return Math.abs(t.x) + Math.abs(t.y);
    }

    /**
     * Gets the Manhattan distance between two Points.
     * 
     * @param a the start Point
     * @param b the end Point
     * @return the Manhattan distance between the points.
     */
    protected int getDistance(Point a, Point b) {
        return Math.abs(a.x - b.x) + Math.abs(a.y - b.y);
    }

    /**
     * Gets the distance from the agent to another agent.
     * 
     * @param agent the name of the agent to which the distance should be calculated
     * @return the distance to the agent
     */
    protected int getDistanceToAgent(String agent) {
        List<ReachableTeammate> mates = belief.getReachableTeammates();
        for (ReachableTeammate m : mates) {
            if (m.name().equals(agent)) {
                return m.distance();
            }
        }
        return 0;
    }

    /**
     * Gets the position of an agent.
     * 
     * @param agent the name of the agent
     * @return the position of the agent
     */
    protected Point getAgentPosition(String agent) {
        List<ReachableTeammate> mates = belief.getReachableTeammates();
        for (ReachableTeammate m : mates) {
            if (m.name().equals(agent)) {
                return m.position();
            }
        }
        return null;
    }

    /**
     * Gets the biggest agent index of all agents which are adjacent to a certain position.
     * 
     * @param p the position to test
     * @param supervisor the name of the supervisor of the agent group
     * @return the biggest agent index of all agents which are adjacent to the provided position
     */
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

    /**
     * Gets the biggest agent index of all agents which are adjacent to a certain Thing.
     * 
     * @param t the thing to test
     * @param supervisor the name of the supervisor of the agent group
     * @return the biggest agent index of all agents which are adjacent to the provided thing
     */
    protected int getBiggestAdjacentAgentId(Thing t, String supervisor) {
        if (t == null) return 0;
        return getBiggestAdjacentAgentId(new Point(t.x, t.y), supervisor);
    }

    /**
     * Counter clockwise rotates a Point around the origin.
     *  
     * @param p the Point to rotate
     * @return the rotated point
     */
    public Point getCCRotatedPoint(Point p) {
        return new Point(p.y, -p.x);
    }
}
