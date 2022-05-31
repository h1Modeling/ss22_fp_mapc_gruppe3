package de.feu.massim22.group3.agents.Desires.ADesires;

import java.awt.Point;
import java.util.List;

import de.feu.massim22.group3.agents.BdiAgent;
import de.feu.massim22.group3.agents.Desires.SubDesires.SubDesire;

import de.feu.massim22.group3.agents.DirectionUtil;

import de.feu.massim22.group3.agents.Reachable.ReachableRoleZone;

import eis.iilang.Action;
import eis.iilang.Identifier;


public class GoAdoptRole extends SubDesire {
    boolean inZone = false;
	String role = null;
	Point agentPos = agent.belief.getPosition();
	List<ReachableRoleZone> reachableRoleZones = agent.belief.getReachableRoleZones();
    
	public GoAdoptRole(BdiAgent agent) {
        super("GoAdoptRole", agent);
    }
	
	 public GoAdoptRole(BdiAgent agent, String role) {
	        super("GoAdoptRole", agent);
	        this.role = role;
	        groupOrder = true;
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
		boolean result = true;
		// es existiert eine roleZone ( die der Agent erreichen kann)und er ist nicht schon drin
		if (reachableRoleZones.size() > 0) {
			for (ReachableRoleZone rrz : reachableRoleZones) {
				if (agentPos.x == rrz.position().x && agentPos.y == rrz.position().y) {
				    inZone = true;
				    
					if(role != null) {
						result = true;
					} else {
						result = false;
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
	@Override
	public Action getNextAction() {
		Action nextAction = null;

		// roleZone mit der kürzesten Entfernung zum Agenten
		ReachableRoleZone nearestRoleZone = agent.desireProcessing
				.getNearestRoleZone(agent.belief.getReachableRoleZones());
		String direction = DirectionUtil.firstIntToString(nearestRoleZone.direction());

		if (role != null) {
			// bestimmte Rolle ist vorgegeben
		    if (inZone) {
		        nextAction = new Action("adopt", new Identifier(role));
		    } else {
		     // einfach zur roleZone gehen
	            nextAction = new Action("move", new Identifier(direction));
		    }
		} else {
			// einfach zur roleZone gehen
			nextAction = new Action("move", new Identifier(direction));
		}
		return nextAction;
	}
    
    @Override
    public boolean isDone() {
        return true;
    }
    
   
}