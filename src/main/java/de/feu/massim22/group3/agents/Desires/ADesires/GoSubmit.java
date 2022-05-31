package de.feu.massim22.group3.agents.Desires.ADesires;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import de.feu.massim22.group3.agents.BdiAgent;
import de.feu.massim22.group3.agents.Desires.SubDesires.SubDesire;
import de.feu.massim22.group3.agents.DesireUtilities;
import de.feu.massim22.group3.agents.DirectionUtil;
import de.feu.massim22.group3.agents.Reachable.ReachableGoalZone;
import de.feu.massim22.group3.utils.logging.AgentLogger;
import eis.iilang.Action;
import eis.iilang.Identifier;
import massim.protocol.data.TaskInfo;
import massim.protocol.data.Thing;
import java.awt.Point;

public class GoSubmit extends SubDesire {

	public GoSubmit(BdiAgent agent) {
		super("Submit", agent);
        groupOrder = true;
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
        AgentLogger.info(Thread.currentThread().getName() + " GoSubmit.isExecutable() Start");	
		if(!agent.desireProcessing.analysisDone) {
			agent.desireProcessing.analyseAttachedThings();
			agent.desireProcessing.analysisDone = true;
		}
		
		if (agent.desireProcessing.goodPositionBlocks.size() > 0 
				&& agent.desireProcessing.badPositionBlocks.size() == 0 
				&& agent.desireProcessing.badBlocks.size() == 0
				&& agent.desireProcessing.missingBlocks.size() == 0) {
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
        AgentLogger.info(Thread.currentThread().getName() + " GoSubmit.getNextAction() Start");     
		Point agentPos = agent.belief.getPosition();
		List<Point> pointsGoalZone = agent.belief.getGoalZones();
		Action nextAction = null;

		for (Point p : pointsGoalZone) {
			/*AgentLogger.info(Thread.currentThread().getName() + " getNextAction() agentPos: " + agentPos
					+ " , Point GoalZone: " + p);*/
			if (agentPos.x == p.x && agentPos.y == p.y) {
				// Agent steht schon in einer GoalZone
				nextAction = new Action("submit", new Identifier(agent.desireProcessing.task.name));
			} 
		}
			if(nextAction == null) {
				// Agent muss noch in die GoalZone laufen
				ReachableGoalZone nearestGoalZone = agent.desireProcessing.getNearestGoalZone(agent.belief.getReachableGoalZones());
				String direction = DirectionUtil.firstIntToString(nearestGoalZone.direction());
				nextAction = new Action("move", new Identifier(direction));
			}
		return nextAction;
	}
	
    @Override
    public boolean isDone() {
        return true;
    }
    
    
}
