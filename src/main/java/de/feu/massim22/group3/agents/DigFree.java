package de.feu.massim22.group3.agents;

import java.util.Iterator;

import eis.iilang.Action;
import eis.iilang.Identifier;
import massim.protocol.data.Thing;

public class DigFree extends Desire {
	
	DigFree(BdiAgent agent){
		super("DigFree",agent);
	}
	
	/**
	 * The method proves if a certain Desire is possible.
	 *
	 * @param desire -  the desire that has to be proven 
	 * 
	 * @return boolean - the desire is possible or not
	 */
	public boolean isDesirePossible(Desire desire) {
		boolean result = false;
		boolean north = false;
		boolean east = false;
		boolean south = false;
		boolean west = false;
		
        for (Thing thing : agent.belief.getThings()) {
            if (thing.type.equals(Thing.TYPE_OBSTACLE)) {
                if (thing.x == 0 && thing.y == -1) {
                    north = true;
                }
                if (thing.x == 1 && thing.y == 0) {
                    east = true;
                }
                if (thing.x == 0 && thing.y == 1) {
                    south = true;
                }
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
	
	//Norden To Do: richtige Richtung
	public Action getNextAction() {
		Identifier x = new Identifier("0");
		Identifier y = new Identifier("-1");
		
		return new Action("clear", x, y );
	}
	
}
