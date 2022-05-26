package de.feu.massim22.group3.agents.Desires.ADesires;

import de.feu.massim22.group3.agents.BdiAgent;
import de.feu.massim22.group3.agents.Desires.SubDesires.SubDesire;
import de.feu.massim22.group3.agents.DesireUtilities;
import eis.iilang.*;
import massim.protocol.data.Thing;


public class DodgeClear extends ADesire {
	
	public DodgeClear(BdiAgent agent){
		super("DodgeClear", agent);
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

		for (Thing thing : agent.belief.getThings()) {
			if (thing.type.equals(Thing.TYPE_MARKER)) {
				if (thing.x == 0 && thing.y == 0) {
					if (thing.details.equals("cp")) {
						result = true;
					}
					if (thing.details.equals("ci")) {
						result = true;
					}
					if (thing.details.equals("clear")) {
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
	
	//Norden TODO: richtige Richtung
    @Override
	public Action getNextAction() {
		Identifier newDirection = new Identifier("n");
		
		return new Action("move", newDirection );
	}
}
