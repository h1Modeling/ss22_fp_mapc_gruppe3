package de.feu.massim22.group3.agents;

import java.awt.Point;
import java.util.List;

import de.feu.massim22.group3.agents.Belief.ReachableGoalZone;
import eis.iilang.Action;
import eis.iilang.Identifier;

public class GoGoalZone extends Desire {
    GoGoalZone(BdiAgentV2 agent) {
        super("GoGoalZone", agent);
    }
    
    /**
     * The method proves if a certain Desire is possible.
     *
     * @param desire -  the desire that has to be proven 
     * 
     * @return boolean - the desire is possible or not
     */
    @Override
    public boolean isExecutable(Desire desire) {
    	//es existiert eine goalZone ( die der Agent erreichen kann)
    	if(agent.belief.getReachableGoalZones().size() > 0)
    		return true;
    	else {
    		return false;
    	}
    }
    
    /**
     * The method returns the nextAction that is needed.
     * 
     * @return Action - the action that is needed
     * 
     **/
    @Override
	public Action getNextAction() {
		// goalZone mit der kürzesten Entfernung zum Agenten
		ReachableGoalZone nearestGoalZone = getNearestGoalZone(agent.belief.getReachableGoalZones());
		// Richtung zu goalZone To Do : Hindernissprüfung
		DirectionUtil.getDirection(agent.belief.getPosition(), nearestGoalZone.position());
		String direction = DirectionUtil.intToString(nearestGoalZone.direction());
		Point p = DirectionUtil.getCellInDirection(direction);
		return new Action("move", new Identifier(String.valueOf(p.x)), new Identifier(String.valueOf(p.y)));
	}
    
    ReachableGoalZone getNearestGoalZone(List<ReachableGoalZone> inZoneList) {
        int distance = 1000;
        ReachableGoalZone result = null;
        
        for (ReachableGoalZone zone : (List<ReachableGoalZone>) inZoneList) {
            if (zone.distance() < distance) {
                distance = zone.distance();
                result = zone;
            }
        }
        return result;
    }
}
