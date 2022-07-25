package de.feu.massim22.group3.agents.Desires.BDesires;

import java.awt.Point;

import de.feu.massim22.group3.agents.Belief;
import de.feu.massim22.group3.agents.DirectionUtil;
import de.feu.massim22.group3.agents.Reachable.ReachableDispenser;
import de.feu.massim22.group3.agents.Reachable.ReachableGoalZone;
import de.feu.massim22.group3.map.CellType;
import de.feu.massim22.group3.utils.Convert;
import de.feu.massim22.group3.utils.logging.AgentLogger;
import massim.protocol.data.Thing;


public class GuardGoalZoneDesire extends BeliefDesire {


    public GuardGoalZoneDesire(Belief belief, String supervisor) {
        super(belief);
        AgentLogger.info(Thread.currentThread().getName() + " runSupervisorDecisions - Start GuardGoalZoneDesire");
        String[] neededActions = {"clear"};
        precondition.add(new ActionDesire(belief, neededActions));
    }
    
    public ActionInfo getNextActionInfo() {
        ActionInfo a = fullfillPreconditions();
        if (a != null) {
            return a;
        }


        // If in goal zone
        Point p = belief.getNearestRelativeManhattenGoalZone();
        
        if (p != null && p.equals(new Point(0, 0))){
            // If on dispenser and enemy close --> clear on enemy
//            Thing closestBlock = getClosestBlock();
//            if (closestBlock != null && belief.getEnergy() > 10) {
//                if (getDistance(closestBlock) <= belief.getRole().clearMaxDistance()) {
//                    int x = closestBlock.x;
//                    int y = closestBlock.y;
//                    AgentLogger.info(belief.getAgentShortName(), "Clear on " + x + " " + y);
//                    return ActionInfo.CLEAR(new Point(x, y), "Clear on block");
//                }
//            }
            
            // If on dispenser and enemy close --> clear on enemy
            Thing closestEnemy = getClosestEnemy();
            if (closestEnemy != null && belief.getEnergy() > 10) {
                if (getDistance(closestEnemy) <= belief.getRole().clearMaxDistance()) {
                    return ActionInfo.CLEAR(new Point(closestEnemy.x, closestEnemy.y), "Clear on enemy");
                }
            }
            return ActionInfo.SKIP("Agent is on Dispenser and no enemy close --> skip");
        }
        // Move with Manhatten when close enough
        else if (p != null && getDistance(p) < 1) {
            String dir = getDirectionToRelativePoint(p);
            return getActionForMove(dir, getName());
        }
        
        
        // Move to goal zone with path finder
        ReachableGoalZone rgz = belief.getNearestGoalZone();
        if (rgz != null) {
            String dir = DirectionUtil.intToString(rgz.direction());
            if (dir.length() > 0) {
                return getActionForMove(dir.substring(0, 1), getName());
            }
        }
        return ActionInfo.SKIP("Agent is stuck in getNextActionInfo of GuardDispenserDesire");
    }

    private Thing getClosestBlock(){
        int dist = 100;
        Thing closestBlock = null;
        for (Thing t : belief.getThings()) {
            if (t.type.equals("block") && getDistance(t) < dist){
                dist = getDistance(t);
                closestBlock = t;
            }
        }
        return closestBlock;
    }

    private Thing getClosestEnemy(){
        int dist = 100;
        Thing closestEnemy = null;
        for (Thing t : belief.getThings()) {
            if (t.type.equals("entity") && ! t.details.equals(belief.getTeam()) && getDistance(t) < dist){
                dist = getDistance(t);
                closestEnemy = t;
            }
        }
        return closestEnemy;
    }

    @Override
    public BooleanInfo isFulfilled() {
        return new BooleanInfo(false, "");
    }

    @Override
    public String getName() {
        return "Guard Goal Zone"; 
    }

    @Override
    public boolean isGroupDesire() {
        return true;
    }

    @Override
    public int getPriority() {
        return 1100;
    }
}
