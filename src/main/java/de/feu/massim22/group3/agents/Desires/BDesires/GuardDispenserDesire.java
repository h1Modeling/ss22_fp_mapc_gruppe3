package de.feu.massim22.group3.agents.Desires.BDesires;

import java.awt.Point;

import de.feu.massim22.group3.agents.Belief;
import de.feu.massim22.group3.agents.DirectionUtil;
import de.feu.massim22.group3.agents.Reachable.ReachableDispenser;
import de.feu.massim22.group3.map.CellType;
import de.feu.massim22.group3.utils.Convert;
import de.feu.massim22.group3.utils.logging.AgentLogger;
import massim.protocol.data.Thing;


public class GuardDispenserDesire extends BeliefDesire {

    private String blockDetail;
    CellType dispenserCellType;

    public GuardDispenserDesire(Belief belief, String blockDetail, String supervisor) {
        super(belief);
        AgentLogger.info(Thread.currentThread().getName() + " runSupervisorDecisions - Start GuardDispenserDesire");
        this.blockDetail = blockDetail;
        dispenserCellType = Convert.dispenserToCellType(blockDetail);
        String[] neededActions = {"clear"};
        precondition.add(new ActionDesire(belief, neededActions));
    }
    
    public ActionInfo getNextActionInfo() {
        ActionInfo a = fullfillPreconditions();
        if (a != null) {
            return a;
        }

        // If 

        // If on dispenser
        Point p = belief.getNearestRelativeManhattenDispenser(blockDetail);
        if (p != null && p.equals(new Point(0, 0))){
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
        
        
        // Move to dispenser with path finder
        ReachableDispenser rd = belief.getNearestDispenser(dispenserCellType);
        if (rd != null) {
            String dir = DirectionUtil.intToString(rd.direction());
            if (dir.length() > 0) {
                return getActionForMove(dir.substring(0, 1), getName());
            }
        }
        return ActionInfo.SKIP("Agent is stuck in getNextActionInfo of GuardDispenserDesire");
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
        return "Guard Dispenser " + blockDetail; 
    }

    @Override
    public boolean isGroupDesire() {
        return true;
    }

    @Override
    public int getPriority() {
        return 1100;
    }

    public String getBlockDetail() {
        return blockDetail;
    }
}
