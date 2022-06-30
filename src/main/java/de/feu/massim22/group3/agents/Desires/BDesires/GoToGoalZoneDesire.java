package de.feu.massim22.group3.agents.Desires.BDesires;

import de.feu.massim22.group3.agents.Belief;
import de.feu.massim22.group3.agents.DirectionUtil;
import de.feu.massim22.group3.agents.Reachable.ReachableGoalZone;

import java.awt.Point;

public class GoToGoalZoneDesire extends BeliefDesire {

    public GoToGoalZoneDesire(Belief belief) {
        super(belief);
    }

    @Override
    public BooleanInfo isFulfilled() {
        boolean result = belief.getGoalZones().contains(new Point(0, 0));
        String info = result ? "" : "not on goal zone";
        return new BooleanInfo(result, info);
    }

    @Override
    public ActionInfo getNextActionInfo() {
        ReachableGoalZone zone = belief.getNearestGoalZone();
        Point p = belief.getNearestRelativeManhattenGoalZone();
        int manhattenDistance = p == null ? 1000 : Math.abs(p.x) + Math.abs(p.y);
        // Data from Pathfinding
        if (zone != null && zone.distance() < 4 * manhattenDistance) {
            String direction = DirectionUtil.intToString(zone.direction());
            if (direction.length() > 0) {
                return getActionForMove(direction.substring(0, 1), getName());
            }
        }
        // Manhatten
        String dir = getDirectionToRelativePoint(p);
        return getActionForMove(dir, getName());
    }
    
    @Override
    public BooleanInfo isExecutable() {
        boolean result = belief.getReachableGoalZones().size() > 0 || belief.getGoalZones().size() > 0;
        String info = result ? "" : "no reachable goal zones";
        return new BooleanInfo(result, info);
    }
}
