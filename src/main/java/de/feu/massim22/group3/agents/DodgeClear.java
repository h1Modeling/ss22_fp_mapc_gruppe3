package de.feu.massim22.group3.agents;

import java.util.Iterator;
import eis.iilang.*;
import massim.protocol.data.Thing;


public class DodgeClear extends Desire {
	
	DodgeClear(BdiAgent agent){
		super("DodgeClear",agent);
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
		Iterator i = agent.belief.getThings().iterator();

		while (i.hasNext()) {
			if (((Thing) i.next()).type.equals(Thing.TYPE_MARKER)) {
				if (((Thing) i.next()).x == 0 && ((Thing) i.next()).y == 0) {
					if (((Thing) i.next()).details.equals("cp")) {
						result = true;
					}
					if (((Thing) i.next()).details.equals("ci")) {
						result = true;
					}
					if (((Thing) i.next()).details.equals("clear")) {
						result = true;
					}
				}
			}
		}
		return result;
	}

	/**
	 * The method returns the nextAction that is needed.
	 * 
	 * @return Action - the action that is needed
	 * 
	 **/
	
	//Norden To Do: richtige Richtung
	public Action getNextAction() {
		Identifier newDirection = new Identifier("n");
		
		return new Action("move", newDirection );
	}
}
