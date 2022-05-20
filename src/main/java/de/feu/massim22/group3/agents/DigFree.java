package de.feu.massim22.group3.agents;

import java.awt.Point;
import java.util.List;

import de.feu.massim22.group3.agents.Belief.*;
import eis.iilang.Action;
import eis.iilang.Identifier;
import massim.protocol.data.Thing;

public class DigFree extends ADesire {
	
	DigFree(BdiAgentV2 agent, DesireUtilities desireProcessing){
		super("DigFree", agent, desireProcessing);
	}
	
	/**
	 * The method proves if a certain Desire is possible.
	 *
	 * @param desire -  the desire that has to be proven 
	 * 
	 * @return boolean - the desire is possible or not
	 */
	@Override 
	public boolean isExecutable() {
		boolean result = false;
		boolean north = false;
		boolean east = false;
		boolean south = false;
		boolean west = false;
		
        for (Thing thing : agent.belief.getThings()) {
            if (thing.type.equals(Thing.TYPE_OBSTACLE)) {
                if (thing.x == 0 && thing.y == -1) {
                    north = true;
                } else
                if (thing.x == 1 && thing.y == 0) {
                    east = true;
                } else
                if (thing.x == 0 && thing.y == 1) {
                    south = true;
                } else
                if (thing.x == -1 && thing.y == 0) {
                    west = true;
                }
            }
        }
		
		if (north && east && south && west) {
			result = true;
		}
		
		return result;
	}

	/**
	 * The method returns the nextAction that is needed  .
	 * 
	 * @return Action - the action that is needed
	 * 
	 **/
	
	//mit Block in Richtung der nächsten GoalZone, ohne Block in Richtung dispenser (?)
    @Override
    public Action getNextAction() {
        String direction;

        if (agent.belief.getAttachedThings().size() > 0) {
            List<ReachableGoalZone> zoneList = agent.belief.getReachableGoalZones();
            ReachableGoalZone nearestZone = desireProcessing.getNearestGoalZone(zoneList);
            direction = DirectionUtil.intToString(nearestZone.direction());
        } else {
            List<ReachableDispenser> zoneList = agent.belief.getReachableDispensers();
            ReachableDispenser nearestZone = desireProcessing.getNearestDispenser(zoneList);
            direction = DirectionUtil.intToString(nearestZone.direction());
        }

        Point p = DirectionUtil.getCellInDirection(direction);  
        return new Action("clear", new Identifier(String.valueOf(p.x)), new Identifier(String.valueOf(p.y)));
    }
	

}
