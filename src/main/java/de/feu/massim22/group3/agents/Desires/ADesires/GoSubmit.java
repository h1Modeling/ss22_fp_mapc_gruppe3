package de.feu.massim22.group3.agents.Desires.ADesires;

import java.util.List;
import java.util.Set;

import de.feu.massim22.group3.agents.BdiAgentV2;
import de.feu.massim22.group3.agents.DesireUtilities;
import de.feu.massim22.group3.agents.DirectionUtil;
import de.feu.massim22.group3.agents.Reachable.ReachableGoalZone;
import de.feu.massim22.group3.utils.logging.AgentLogger;
import eis.iilang.Action;
import eis.iilang.Identifier;
import massim.protocol.data.TaskInfo;
import massim.protocol.data.Thing;
import java.awt.Point;

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
		return true;
	}

	/**
	 * The method returns the nextAction that is needed.
	 * 
	 * @return Action - the action that is needed
	 * 
	 **/
	@Override
	public Action getNextAction() {
		Point agentPos = agent.belief.getPosition();
		List<Point> pointsGoalZone = agent.belief.getGoalZones();
		Action nextAction = null;

		for (Point p : pointsGoalZone) {
			AgentLogger.info(Thread.currentThread().getName() + " getNextAction() agentPos: " + agentPos
					+ " , Point GoalZone: " + p);
			if (agentPos.x == p.x && agentPos.y == p.y) {
				// Agent steht schon in einer GoalZone
				nextAction = new Action("submit", new Identifier(desireProcessing.task));
			} 
		}
			if(nextAction == null) {
				// Agent muss noch in die GoalZone laufen
				ReachableGoalZone nearestGoalZone = desireProcessing.getNearestGoalZone(agent.belief.getReachableGoalZones());
				String direction = DirectionUtil.intToString(nearestGoalZone.direction());
				nextAction = new Action("move", new Identifier(direction));
			}
		return nextAction;
	}
}
