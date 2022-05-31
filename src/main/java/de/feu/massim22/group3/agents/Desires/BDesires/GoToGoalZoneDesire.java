package de.feu.massim22.group3.agents.Desires.BDesires;

import de.feu.massim22.group3.agents.Belief;
import de.feu.massim22.group3.agents.DirectionUtil;
import de.feu.massim22.group3.agents.Reachable.ReachableGoalZone;
import eis.iilang.Action;
import eis.iilang.Identifier;

import java.awt.Point;

public class GoToGoalZoneDesire extends BeliefDesire {

    public GoToGoalZoneDesire(Belief belief) {
        super(belief);
    }

    @Override
    public boolean isFullfilled() {
        return belief.getRoleZones().contains(new Point(0, 0));
    }

    @Override
    public Action getNextAction() {
        ReachableGoalZone zone = belief.getNearestGoalZone();
        String direction = DirectionUtil.intToString(zone.direction());
        return new Action("move", new Identifier(direction.substring(0, 1)));
    }
    
    @Override
    public boolean isExecutable() {
        return belief.getReachableGoalZones().size() > 0;
    }
}
