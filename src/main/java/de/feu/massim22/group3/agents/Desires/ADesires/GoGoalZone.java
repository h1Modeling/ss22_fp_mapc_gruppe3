package de.feu.massim22.group3.agents.Desires.ADesires;

import de.feu.massim22.group3.agents.BdiAgentV2;
import de.feu.massim22.group3.agents.DesireUtilities;
import de.feu.massim22.group3.agents.DirectionUtil;
import de.feu.massim22.group3.agents.Reachable.ReachableGoalZone;
import eis.iilang.Action;
import eis.iilang.Identifier;

public class GoGoalZone extends ADesire {
    
	public GoGoalZone(BdiAgentV2 agent, DesireUtilities desireProcessing) {
        super("GoGoalZone", agent, desireProcessing);
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
        //es existiert eine goalZone ( die der Agent erreichen kann)
        if (agent.belief.getReachableGoalZones().size() > 0)
            return true;
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
        // goalZone mit der kürzesten Entfernung zum Agenten
        ReachableGoalZone nearestGoalZone = desireProcessing.getNearestGoalZone(agent.belief.getReachableGoalZones());
        String direction = DirectionUtil.intToString(nearestGoalZone.direction());
        return new Action("move", new Identifier(direction));
    }
}
