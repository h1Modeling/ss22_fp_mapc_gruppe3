package de.feu.massim22.group3.agents.Desires.ADesires;

import java.awt.Point;
import java.util.List;

import de.feu.massim22.group3.agents.BdiAgent;
import de.feu.massim22.group3.agents.Desires.SubDesires.SubDesire;
import de.feu.massim22.group3.agents.DesireUtilities;
import de.feu.massim22.group3.agents.DirectionUtil;
import de.feu.massim22.group3.agents.Reachable.ReachableGoalZone;
import de.feu.massim22.group3.utils.logging.AgentLogger;
import eis.iilang.Action;
import eis.iilang.Identifier;

public class GoGoalZone extends ADesire {
    
	public GoGoalZone(BdiAgent agent) {
        super("GoGoalZone", agent);
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
		//List<Point> pointsGoalZone = agent.belief.getGoalZones();
		List<ReachableGoalZone> reachableGoalZones = agent.belief.getReachableGoalZones();

		// es existiert eine goalZone ( die der Agent erreichen kann)und er ist nicht schon drin
		if (reachableGoalZones.size() > 0) {
			for (ReachableGoalZone rgz : reachableGoalZones) {
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
        // goalZone mit der kürzesten Entfernung zum Agenten
        ReachableGoalZone nearestGoalZone = agent.desireProcessing.getNearestGoalZone(agent.belief.getReachableGoalZones());
        String direction = DirectionUtil.firstIntToString(nearestGoalZone.direction());
        return new Action("move", new Identifier(direction));
    }
}
