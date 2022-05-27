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

public class GoRoleZone extends SubDesire {
    
	public GoRoleZone(BdiAgent agent) {
        super("GoRoleZone", agent);
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
		Point agentPos = agent.belief.getPosition();
		List<ReachableRoleZone> reachableRoleZones = agent.belief.getReachableRoleZones();

		// es existiert eine roleZone ( die der Agent erreichen kann)und er ist nicht schon drin
		if (reachableRoleZones.size() > 0) {
			for (ReachableRoleZone rgz : reachableRoleZones) {
				AgentLogger.info(Thread.currentThread().getName() + " isExecutable() agentPos: " + agentPos
						+ " , Point GoalZone: " + rgz.position());
				if (agentPos.x == rgz.position().x && agentPos.y == rgz.position().y) {
					return false;
				}
			}
			return true;

		} else {
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
        // roleZone mit der kürzesten Entfernung zum Agenten
        ReachableRoleZone nearestRoleZone = agent.desireProcessing.getNearestRoleZone(agent.belief.getReachableRoleZones());
        String direction = DirectionUtil.firstIntToString(nearestRoleZone.direction());
        return new Action("move", new Identifier(direction));
    }
    
    @Override
    public boolean isDone() {
        return true;
    }
    
   
}