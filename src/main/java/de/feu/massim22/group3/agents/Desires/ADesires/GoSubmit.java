package de.feu.massim22.group3.agents.Desires.ADesires;

import de.feu.massim22.group3.agents.BdiAgent;
import de.feu.massim22.group3.agents.DirectionUtil;
import de.feu.massim22.group3.agents.Reachable.ReachableGoalZone;
import de.feu.massim22.group3.utils.logging.AgentLogger;
import eis.iilang.Action;
import eis.iilang.Identifier;
import java.awt.Point;

public class GoSubmit extends ADesire {

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
        AgentLogger.info(Thread.currentThread().getName() + " GoSubmit.getNextAction() Start - Agent: " + agent.belief.getPosition() + " ReachableGoalZone: " + agent.belief.getReachableGoalZones());
        AgentLogger.info(Thread.currentThread().getName() + " GoSubmit.getNextAction() Start - Agent: " + agent.belief.getPosition() + " GoalZone: " + agent.belief.getGoalZones());
        
        Action nextAction = null;
        Point pointAgent = new Point(0, 0);
        
        for (Point goalZone : agent.belief.getGoalZones()) {
            if (goalZone.equals(pointAgent)) {
                // Agent steht schon in einer GoalZone
                nextAction = new Action("submit", new Identifier(agent.desireProcessing.task.name));
                break;
            }
        }
        
        if (nextAction == null) {
            // Agent muss noch in die GoalZone laufen
            ReachableGoalZone nearestGoalZone = agent.desireProcessing
                    .getNearestGoalZone(agent.belief.getReachableGoalZones());
            String direction = DirectionUtil.firstIntToString(nearestGoalZone.direction());
            nextAction = agent.desireProcessing.getPossibleActionForMove(agent, direction);  
        }
        
        return nextAction;
    }
}
