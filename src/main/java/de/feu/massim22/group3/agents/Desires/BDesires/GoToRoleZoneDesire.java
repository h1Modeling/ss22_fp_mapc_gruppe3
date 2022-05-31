package de.feu.massim22.group3.agents.Desires.BDesires;

import de.feu.massim22.group3.agents.Belief;
import de.feu.massim22.group3.agents.DirectionUtil;
import de.feu.massim22.group3.agents.Reachable.ReachableRoleZone;
import eis.iilang.Action;
import eis.iilang.Identifier;

import java.awt.Point;

public class GoToRoleZoneDesire extends BeliefDesire {

    public GoToRoleZoneDesire(Belief belief) {
        super(belief);
    }

    @Override
    public boolean isFullfilled() {
        for (Point p : belief.getRoleZones()) {
            if (p.x == 0 && p.y == 0) {
                return true;
            }
        }
        return false;
    }

    @Override
    public Action getNextAction() {
        ReachableRoleZone zone = belief.getNearestRoleZone();
        String direction = DirectionUtil.intToString(zone.direction());
        return new Action("move", new Identifier(direction.substring(0, 1)));
    }

    @Override
    public boolean isExecutable() {
        return belief.getReachableRoleZones().size() > 0;
    }
}
