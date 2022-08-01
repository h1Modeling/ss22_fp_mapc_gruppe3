package de.feu.massim22.group3.agents.desires;

import de.feu.massim22.group3.agents.belief.Belief;
import de.feu.massim22.group3.agents.belief.reachable.ReachableGoalZone;
import de.feu.massim22.group3.utils.DirectionUtil;
import de.feu.massim22.group3.utils.logging.AgentLogger;

import java.awt.Point;

public class GoToGoalZoneDesire extends BeliefDesire {

    public GoToGoalZoneDesire(Belief belief) {
        super(belief);
        AgentLogger.info(Thread.currentThread().getName() + " runSupervisorDecisions - Start GoToGoalZoneDesire, Step: " + belief.getStep());
    }

    @Override
    public BooleanInfo isFulfilled() {
        AgentLogger.info(Thread.currentThread().getName() + " runSupervisorDecisions - GoToGoalZoneDesire.isFulfilled, Step: " + belief. getStep());
        boolean result = belief.getGoalZones().contains(new Point(0, 0));
        String info = result ? "" : "not on goal zone";
        return new BooleanInfo(result, info);
    }

    @Override
    public ActionInfo getNextActionInfo() {
        AgentLogger.info(Thread.currentThread().getName() + " runSupervisorDecisions - GoToGoalZoneDesire.getNextActionInfo, Step: " + belief.getStep());
        ReachableGoalZone zone = belief.getNearestGoalZone();
        AgentLogger.info(Thread.currentThread().getName() + "GoToGoalZoneDesire - AgentPosition: " + belief.getPosition());
        AgentLogger.info(Thread.currentThread().getName() + "GoToGoalZoneDesire - reachableGoalZones: " + belief.getReachableGoalZones());
        AgentLogger.info(Thread.currentThread().getName() + "GoToGoalZoneDesire - GoalZones: " + belief.getGoalZones());
        Point p = belief.getNearestRelativeManhattenGoalZone();
        int manhattenDistance = p == null ? 1000 : Math.abs(p.x) + Math.abs(p.y);
        // Data from Pathfinding
        if (zone != null && zone.distance() < 4 * manhattenDistance) {
            String direction = DirectionUtil.intToString(zone.direction());
            if (direction.length() > 0) {
            	 AgentLogger.info(Thread.currentThread().getName() + "GoToGoalZoneDesire - nextActionDirectionPathfinding: " + direction);
                return getActionForMove(direction.substring(0, 1), getName());
            }
        }
        // Manhatten
        String dir = getDirectionToRelativePoint(p);
        AgentLogger.info(Thread.currentThread().getName() + "GoToGoalZoneDesire - nextActionDirection: " + dir);
        return getActionForMove(dir, getName());
    }
    
    @Override
    public BooleanInfo isExecutable() {
        AgentLogger.info(Thread.currentThread().getName() + " runSupervisorDecisions - GoToGoalZoneDesire.isExecutable, Step: " + belief.getStep());
        boolean result = belief.getReachableGoalZones().size() > 0 || belief.getGoalZones().size() > 0;
        String info = result ? "" : "no reachable goal zones";
        return new BooleanInfo(result, info);
    }
}
