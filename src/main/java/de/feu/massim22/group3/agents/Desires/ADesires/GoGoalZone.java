package de.feu.massim22.group3.agents.Desires.ADesires;

import java.awt.Point;
import java.util.List;
import de.feu.massim22.group3.agents.BdiAgentV2;
import de.feu.massim22.group3.agents.DirectionUtil;
import de.feu.massim22.group3.agents.Reachable.ReachableGoalZone;
import eis.iilang.Action;
import eis.iilang.Identifier;

public class GoGoalZone extends ADesire {
    
	public GoGoalZone(BdiAgentV2 agent) {
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
        boolean result = true;
        List<ReachableGoalZone> reachableGoalZones = agent.belief.getReachableGoalZones();

        // es existiert eine goalZone ( die der Agent erreichen kann) und er ist nicht schon drin
        if (reachableGoalZones.size() > 0) {
            Point pointAgent = new Point(0, 0);

            for (Point goalZone : agent.belief.getGoalZones()) {
                if (goalZone.equals(pointAgent)) {
                    // Agent steht schon in einer GoalZone
                    result =  false;
                }
            }
        } else {
            result =  false;
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
        // goalZone mit der kürzesten Entfernung zum Agenten
        ReachableGoalZone nearestGoalZone = agent.desireProcessing.getNearestGoalZone(agent.belief.getReachableGoalZones());
        String direction = DirectionUtil.firstIntToString(nearestGoalZone.direction());
        
        return agent.desireProcessing.getPossibleActionForMove(agent, direction);  
    }
}
