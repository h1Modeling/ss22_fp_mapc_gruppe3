package de.feu.massim22.group3.agents;

import eis.iilang.Action;
import eis.iilang.Identifier;

//ToDo Klassenlogik
public class GetBlock extends ADesire {

	GetBlock(BdiAgentV2 agent, DesireUtilities desireProcessing) {
		super("GetBlock", agent, desireProcessing);
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
		return new Action("attach", new Identifier("n"));
	}
}
