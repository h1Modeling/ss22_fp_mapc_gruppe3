package de.feu.massim22.group3.agents.Desires.BDesires;

import de.feu.massim22.group3.agents.Belief;
import de.feu.massim22.group3.agents.DirectionUtil;
import de.feu.massim22.group3.agents.Reachable.ReachableRoleZone;

import java.awt.Point;

public class GoToRoleZoneDesire extends BeliefDesire {

    public GoToRoleZoneDesire(Belief belief) {
        super(belief);
    }

    @Override
    public BooleanInfo isFulfilled() {
        for (Point p : belief.getRoleZones()) {
            if (p.x == 0 && p.y == 0) {
                return new BooleanInfo(true, "");
            }
        }
        return new BooleanInfo(false, "Not on role zone");
    }

    @Override
    public ActionInfo getNextActionInfo() {
        ReachableRoleZone zone = belief.getNearestRoleZone();
        String direction = DirectionUtil.intToString(zone.direction());
        return getActionForMove(direction.substring(0, 1), getName());
    }

    @Override
    public BooleanInfo isExecutable() {
        return new BooleanInfo(belief.getReachableRoleZones().size() > 0, "Known role zones: " + belief.getReachableRoleZones().size());
    }
}
