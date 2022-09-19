package de.feu.massim22.group3.agents.desires;

import java.awt.Point;

import de.feu.massim22.group3.agents.belief.Belief;
import de.feu.massim22.group3.utils.DirectionUtil;
import de.feu.massim22.group3.agents.belief.reachable.ReachableDispenser;
import de.feu.massim22.group3.map.CellType;
import de.feu.massim22.group3.utils.Convert;
import de.feu.massim22.group3.utils.logging.AgentLogger;
import massim.protocol.data.Thing;


/**
 * The Class <code>GuardDispenserDesire</code> was designed to test a strategy where a
 * dispenser is blocked by an agent so that the enemy agents cannot get blocks from it anymore.
 * 
 * @author Phil Heger
 *
 */
public class GuardDispenserDesire extends BeliefDesire {

    private String blockDetail;
    private CellType dispenserCellType;

    
    /**
     * Instantiates a new GuardDispenserDesire.
     * 
     * @param belief the belief of the agent
     * @param blockDetail block type of the dispenser type that the agent is supposed to block
     * @param supervisor the supervisor of the agent group
     */
    public GuardDispenserDesire(Belief belief, String blockDetail, String supervisor) {
        super(belief);
        AgentLogger.info(Thread.currentThread().getName() + " runSupervisorDecisions - Start GuardDispenserDesire");
        this.blockDetail = blockDetail;
        dispenserCellType = Convert.dispenserToCellType(blockDetail);
        String[] neededActions = {"clear"};
        precondition.add(new ActionDesire(belief, neededActions));
    }

    /**
     * {@inheritDoc}
     */
    public ActionInfo getNextActionInfo() {
        ActionInfo a = fulfillPreconditions();
        if (a != null) {
            return a;
        }

        // If on dispenser
        Point p = belief.getNearestRelativeManhattanDispenser(blockDetail);
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

    /**
     * {@inheritDoc}
     */
    @Override
    public BooleanInfo isFulfilled() {
        return new BooleanInfo(false, "");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getName() {
        return "Guard Dispenser " + blockDetail; 
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isGroupDesire() {
        return true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getPriority() {
        return 1100;
    }

    /**
     * {@inheritDoc}
     */
    public String getBlockDetail() {
        return blockDetail;
    }
}
