package de.feu.massim22.group3.agents.desires.V2desires;

import de.feu.massim22.group3.agents.*;
import de.feu.massim22.group3.agents.V2utils.Point;
import de.feu.massim22.group3.agents.belief.Belief;
import de.feu.massim22.group3.agents.belief.reachable.ReachableGoalZone;
import de.feu.massim22.group3.agents.desires.*;
import de.feu.massim22.group3.utils.DirectionUtil;
import de.feu.massim22.group3.utils.logging.AgentLogger;

/**
 * The class <code>LocalExploreDesire</code> models the desire to do a local explore.
 * 
 * @author Melinda Betz
 */
public class LocalExploreDesire extends BeliefDesire {

    private BdiAgentV2 agent;

    /**
     * Instantiates a new LocalExploreDesire.
     * 
     * @param belief - the belief of the agent
     * @param supervisor - the supervisor of the group
     * @param agent - the agent who wants to go to a goal zone
     * 
     */
    public LocalExploreDesire(String supervisor, BdiAgentV2 agent) {
        super(agent.getBelief());
        AgentLogger.info(Thread.currentThread().getName() + " runAgentDecisions - Start LocalExploreDesire");
        this.agent = agent;
    }

    /**
     * Checks if the desire is fulfilled.
     * 
     * @return if it is fulfilled or not
     */
    @Override
    public BooleanInfo isFulfilled() {
        return new BooleanInfo(false, "");
    }
    
    /**
     * Checks if the desire is executable .
     * 
     * @return if it is executable or not
     */
    @Override
    public BooleanInfo isExecutable() {
        return new BooleanInfo(true, "");
    }

    /**
     * Gets the next action that has to be done .
     * 
     * @return the next action
     */
    @Override
    public ActionInfo getNextActionInfo() {
        AgentLogger.info(
                Thread.currentThread().getName() + "LocalExploreDesire.getNextAction() - Agent: " + agent.getName());
        
        if (!agent.blockAttached || belief.getReachableGoalZones().size() == 0) {
            agent.exploreDirection = DirectionUtil
                    .stringToInt(agent.desireProcessing.walkCircles(agent, 10).getValue());
            agent.exploreDirection2 = (agent.exploreDirection2 + 5) % 4;
        } else {
            // Data from Pathfinding
            ReachableGoalZone zone = belief.getReachableGoalZones().get(0);
            agent.exploreDirection = zone.direction();  
            agent.exploreDirection2 = agent.exploreDirection;
        }

        return agent.desireProcessing.getActionForMove(agent, DirectionUtil.intToString(agent.exploreDirection),
                DirectionUtil.intToString(agent.exploreDirection2), getName());
    }
}