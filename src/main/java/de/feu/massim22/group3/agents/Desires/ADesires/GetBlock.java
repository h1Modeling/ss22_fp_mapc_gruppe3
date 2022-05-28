package de.feu.massim22.group3.agents.Desires.ADesires;

import de.feu.massim22.group3.agents.BdiAgent;
import de.feu.massim22.group3.agents.Desires.SubDesires.SubDesire;
import de.feu.massim22.group3.agents.DesireUtilities;
import eis.iilang.Action;
import eis.iilang.Identifier;

//wird momentan nicht benötigt. Logik wird in GoDispenser abgebildet.
public class GetBlock extends ADesire {

	public GetBlock(BdiAgent agent) {
		super("GetBlock", agent);
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

		if(!agent.desireProcessing.analysisDone) {
			agent.desireProcessing.analyseAttachedThings();
			agent.desireProcessing.analysisDone = true;
		}
		
		if (agent.desireProcessing.goodPositionBlocks.size() > 0 
				&& agent.desireProcessing.badBlocks.size() == 0
				&& agent.desireProcessing.missingBlocks.size() > 0) {
			return true;
		}
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
		return new Action("attach", new Identifier("n"));
	}
}
