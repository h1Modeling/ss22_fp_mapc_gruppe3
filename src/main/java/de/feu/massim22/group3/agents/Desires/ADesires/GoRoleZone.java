package de.feu.massim22.group3.agents.Desires.ADesires;

import java.awt.Point;
import java.util.List;

import de.feu.massim22.group3.agents.BdiAgent;
import de.feu.massim22.group3.agents.Desires.SubDesires.SubDesire;
import de.feu.massim22.group3.agents.DesireUtilities;
import de.feu.massim22.group3.agents.DirectionUtil;
import de.feu.massim22.group3.agents.Reachable.ReachableGoalZone;
import de.feu.massim22.group3.agents.Reachable.ReachableRoleZone;
import de.feu.massim22.group3.utils.logging.AgentLogger;
import eis.iilang.Action;
import eis.iilang.Identifier;
import massim.protocol.data.Thing;

public class GoRoleZone extends SubDesire {
	 String role = null;
	Point agentPos = agent.belief.getPosition();
	List<ReachableRoleZone> reachableRoleZones = agent.belief.getReachableRoleZones();
    
	public GoRoleZone(BdiAgent agent) {
        super("GoRoleZone", agent);
    }
	
	 public GoRoleZone(BdiAgent agent, String role) {
	        super("GoRoleZone", agent);
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
		boolean result = false;
		// es existiert eine roleZone ( die der Agent erreichen kann)und er ist nicht schon drin
		if (reachableRoleZones.size() > 0) {
			for (ReachableRoleZone rgz : reachableRoleZones) {
				/*AgentLogger.info(Thread.currentThread().getName() + " isExecutable() agentPos: " + agentPos
						+ " , Point GoalZone: " + rgz.position());*/
				if (agentPos.x == rgz.position().x && agentPos.y == rgz.position().y) {
					if(role != null) {
						result = true;
					} else {
						result = false;
					}
				} else {
					result = true;
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
			// bestimmte Rolle wird benötigt
			switch (role) {
			case "worker":
				nextAction = new Action("adopt", new Identifier("worker"));
			case "constructor":
				nextAction = new Action("adopt", new Identifier("constructor"));
			case "explorer":
				nextAction = new Action("adopt", new Identifier("explorer"));
			case "digger":
				nextAction = new Action("adopt", new Identifier("digger"));
			case "default":
				nextAction = new Action("adopt", new Identifier("default"));
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