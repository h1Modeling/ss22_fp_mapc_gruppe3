package de.feu.massim22.group3.agents.Desires.BDesires;

import de.feu.massim22.group3.agents.Belief;
import de.feu.massim22.group3.agents.DirectionUtil;
import de.feu.massim22.group3.agents.BdiAgentV2;
import de.feu.massim22.group3.agents.Reachable.ReachableGoalZone;
import de.feu.massim22.group3.utils.logging.AgentLogger;

import java.awt.Point;

public class GoGoalZoneDesire extends BeliefDesire {
    BdiAgentV2 agent;

    public GoGoalZoneDesire(Belief belief, BdiAgentV2 agent) {
        super(belief);
        AgentLogger.info(Thread.currentThread().getName() + " runSupervisorDecisions - Start GoGoalZoneDesire, Step: " + belief.getStep());
        this.agent = agent;
    }

    @Override
    public BooleanInfo isFulfilled() {
        AgentLogger.info(Thread.currentThread().getName() + " runSupervisorDecisions - GoGoalZoneDesire.isFulfilled, Step: " + belief. getStep());
        boolean result = belief.getGoalZones().contains(new Point(0, 0));
        String info = result ? "" : "not on goal zone";
        return new BooleanInfo(result, info);
    }
    
    @Override
    public BooleanInfo isExecutable() {
        AgentLogger.info(Thread.currentThread().getName() + " runSupervisorDecisions - GoGoalZoneDesire.isExecutable, Step: " + belief.getStep());
        boolean result = belief.getReachableGoalZones().size() > 0 || belief.getGoalZones().size() > 0;
        String info = result ? "" : "no reachable goal zones";
        return new BooleanInfo(result, info);
    }
    
    @Override
    public ActionInfo getNextActionInfo() {
        AgentLogger.info(Thread.currentThread().getName() + " runSupervisorDecisions - GoGoalZoneDesire.getNextActionInfo, Step: " + belief.getStep());
        AgentLogger.info(Thread.currentThread().getName() + "GoGoalZoneDesire - AgentPosition: " + belief.getPosition());
        AgentLogger.info(Thread.currentThread().getName() + "GoGoalZoneDesire - reachableGoalZones: " + belief.getReachableGoalZones());
        AgentLogger.info(Thread.currentThread().getName() + "GoGoalZoneDesire - GoalZones: " + belief.getGoalZones());
        
        Point p = belief.getNearestRelativeManhattenGoalZone();
        int manhattenDistance = p == null ? 1000 : Math.abs(p.x) + Math.abs(p.y);
        
        if (manhattenDistance < 1000) {
            // Manhatten
            String direction = getDirectionToRelativePoint(p);
            String dirAlt = "";
            
            for (int i = 1; i < belief.getGoalZones().size(); i++) {
                dirAlt = getDirectionToRelativePoint(belief.getGoalZones().get(i));
                
                if (!dirAlt.equals(direction)) {
                    break;
                }
            }
            
            AgentLogger.info(Thread.currentThread().getName() + "GoGoalZoneDesire - nextActionDirectionManhatten: " + direction);
            direction = proofDirection(direction);
            this.agent.desireProcessing.lastWishDirection = direction;
            AgentLogger
                    .info(Thread.currentThread().getName() + "GoGoalZoneDesire - after proofDirection: " + direction);
            
            if (dirAlt.equals(""))
                return getActionForMove(direction, getName());
            else
                return getActionForMoveWithAlternate(direction, dirAlt, getName());
        }
        
        // Data from Pathfinding
        ReachableGoalZone zone = belief.getNearestGoalZone();
        String direction = DirectionUtil.intToString(zone.direction());

        if (direction.length() > 0) {
            AgentLogger.info(Thread.currentThread().getName() + "GoGoalZoneDesire - nextActionDirectionPathfinding: "
                    + direction);
            direction = proofDirection(direction);
            this.agent.desireProcessing.lastWishDirection = direction;
            AgentLogger
                    .info(Thread.currentThread().getName() + "GoGoalZoneDesire - after proofDirection: " + direction);
            return getActionForMove(direction.substring(0, 1), getName());
        } 
        
        return ActionInfo.SKIP(getName());
    }
    
    private String proofDirection(String inDirection) {
        String outDirection = inDirection;
 
        if (this.agent.desireProcessing.lastWishDirection != null) {
            if (belief.getLastAction().equals("move") && belief.getLastActionResult().equals("success")) {
                if (!belief.getLastActionParams().get(0).equals(this.agent.desireProcessing.lastWishDirection)) {
                    if (belief.getLastActionParams().get(0).equals(DirectionUtil.oppositeDirection(inDirection))) {
                        outDirection = this.agent.desireProcessing.lastWishDirection;
                    }
                }
            }
        }
        
        return outDirection;
    }
}
