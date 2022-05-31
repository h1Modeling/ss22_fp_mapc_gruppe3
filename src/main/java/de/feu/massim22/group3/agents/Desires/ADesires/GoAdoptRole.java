package de.feu.massim22.group3.agents.Desires.ADesires;

import java.awt.Point;
import java.util.List;

import de.feu.massim22.group3.agents.BdiAgent;
import de.feu.massim22.group3.agents.Desires.SubDesires.SubDesire;
import de.feu.massim22.group3.agents.Reachable.ReachableRoleZone;
import de.feu.massim22.group3.agents.DesireUtilities;
import eis.iilang.Action;
import eis.iilang.Identifier;

//wird nicht benötigt Logik siehe goRoleZone
public class GoAdoptRole extends ADesire {
	
	public GoAdoptRole(BdiAgent agent) {
		super("GoAdoptRole", agent);
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
		return new Action("adopt", new Identifier("roleTask"));
	}
}
