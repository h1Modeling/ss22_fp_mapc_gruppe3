package de.feu.massim22.group3.agents.desires;

import de.feu.massim22.group3.agents.belief.Belief;
import de.feu.massim22.group3.agents.belief.reachable.ReachableRoleZone;
import de.feu.massim22.group3.utils.DirectionUtil;

import java.awt.Point;

/**
 * The Class <code>GoToRoleZoneDesire</code> models the desire to be positioned inside of a role zone.
 * 
 * @author Heinz Stadler
 */
public class GoToRoleZoneDesire extends BeliefDesire {

    /**
     * Instantiates a new GoToRoleZoneDesire.
     * 
     * @param belief the belief of the agent
     */
    public GoToRoleZoneDesire(Belief belief) {
        super(belief);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public BooleanInfo isFulfilled() {
        for (Point p : belief.getRoleZones()) {
            if (p.x == 0 && p.y == 0) {
                return new BooleanInfo(true, "");
            }
        }
        return new BooleanInfo(false, "Not on role zone");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ActionInfo getNextActionInfo() {
        ReachableRoleZone zone = belief.getNearestRoleZone();
        String direction = DirectionUtil.intToString(zone.direction());
        return getActionForMove(direction.substring(0, 1), getName());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public BooleanInfo isExecutable() {
        return new BooleanInfo(belief.getReachableRoleZones().size() > 0, "Known role zones: " + belief.getReachableRoleZones().size());
    }
}
