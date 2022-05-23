package de.feu.massim22.group3.agents.Desires.ADesires;

import de.feu.massim22.group3.agents.BdiAgentV2;
import de.feu.massim22.group3.agents.DesireUtilities;
import eis.iilang.Action;
import eis.iilang.Identifier;
import massim.protocol.data.Thing;

//TODO Klassenlogik
public class GoSubmit extends ADesire {

	public GoSubmit(BdiAgentV2 agent, DesireUtilities desireProcessing) {
		super("Submit", agent, desireProcessing);
	}

	/**
	 * The method proves if a certain Desire is possible.
	 *
	 * @param desire - the desire that has to be proven
	 * 
	 * @return boolean - the desire is possible or not
	 */
	@Override
	public boolean isExecutable() {
		return false;
	}

	/**
	 * The method returns the nextAction that is needed.
	 * 
	 * @return Action - the action that is needed
	 * 
	 **/
	@Override
	public Action getNextAction() {
		 return new Action("submit", new Identifier("task1"));
	}
}
