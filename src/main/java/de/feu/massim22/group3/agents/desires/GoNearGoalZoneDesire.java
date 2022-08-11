package de.feu.massim22.group3.agents.desires;

import de.feu.massim22.group3.agents.belief.Belief;
import de.feu.massim22.group3.agents.belief.reachable.ReachableGoalZone;
import de.feu.massim22.group3.utils.DirectionUtil;

import java.awt.Point;
/**
 * The Class <code>GoNearGoalZoneDesire</code> models the desire to be positioned near a goal zone.
 * 
 * @author Heinz Stadler
 */
public class GoNearGoalZoneDesire extends BeliefDesire {

    /**
     * Instantiates a new GoNearGoalZoneDesire.
     * 
     * @param belief the belief of the agent
     */
    public GoNearGoalZoneDesire(Belief belief) {
        super(belief);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public BooleanInfo isFulfilled() {
        boolean result = belief.getGoalZones().size() > 0;
        String info = result ? "" : "not near goal zone";
        return new BooleanInfo(result, info);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ActionInfo getNextActionInfo() {
        ReachableGoalZone zone = belief.getNearestGoalZone();
        Point p = belief.getNearestRelativeManhattanGoalZone();
        int manhattanDistance = p == null ? 1000 : Math.abs(p.x) + Math.abs(p.y);
        // Data from Pathfinding
        if (zone != null && zone.distance() < 4 * manhattanDistance) {
            String direction = DirectionUtil.intToString(zone.direction());
            if (direction.length() > 0) {
                return getActionForMove(direction.substring(0, 1), getName());
            }
        }
        // Manhattan
        String dir = getDirectionToRelativePoint(p);
        return getActionForMove(dir, getName());
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public BooleanInfo isExecutable() {
        boolean result = belief.getReachableGoalZones().size() > 0 || belief.getGoalZones().size() > 0;
        String info = result ? "" : "no reachable goal zones";
        return new BooleanInfo(result, info);
    }
}
