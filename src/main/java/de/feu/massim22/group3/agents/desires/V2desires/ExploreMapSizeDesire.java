package de.feu.massim22.group3.agents.desires.V2desires;

import de.feu.massim22.group3.agents.*;
import de.feu.massim22.group3.agents.V2utils.AgentCooperations;
import de.feu.massim22.group3.agents.V2utils.Point;
import de.feu.massim22.group3.agents.belief.Belief;
import de.feu.massim22.group3.agents.desires.*;
import de.feu.massim22.group3.utils.DirectionUtil;
import de.feu.massim22.group3.utils.logging.AgentLogger;
import eis.iilang.Identifier;

/**
 * The class <code>ExploreMapSizeDesire</code> models the desire to explore the map size.
 * 
 * @author Melinda Betz
 */
public class ExploreMapSizeDesire extends BeliefDesire {

    private BdiAgentV2 agent;
    private String supervisor;

    /**
     * Instantiates a new ExploreMapSizeDesire.
     * 
     * @param belief the belief of the agent
     * @param supervisor the supervisor of the group
     * @param agent the agent who wants to go to a goal zone
     * 
     */
    public ExploreMapSizeDesire(Belief belief, String supervisor, BdiAgentV2 agent) {
        super(belief);
        AgentLogger.info(Thread.currentThread().getName() + " runAgentDecisions - Start ExploreMapSizeDesire");
        this.agent = agent;
        this.supervisor = supervisor;
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
        if (AgentCooperations.exists(agent.desireProcessing.stepUtilities.exploreHorizontalMapSize, agent)) {
            return new BooleanInfo(true, "");
        }
        
        return new BooleanInfo(false, "");
    }

    /**
     * Gets the next action that has to be done .
     * 
     * @return the next action
     */
    @Override
    public ActionInfo getNextActionInfo() {
        AgentLogger.info(
                Thread.currentThread().getName() + "ExploreMapSizeDesire.getNextAction() - Agent: " + agent.getName());
        
        

        if (AgentCooperations.exists(agent.desireProcessing.stepUtilities.exploreHorizontalMapSize, agent, 2) 
            || AgentCooperations.exists(agent.desireProcessing.stepUtilities.exploreVerticalMapSize, agent, 2)) {
            return ActionInfo.SKIP("2000 waiting for explore map size finished");
        }
        
        if (AgentCooperations.exists(agent.desireProcessing.stepUtilities.exploreHorizontalMapSize, agent, 1)) {
            return agent.desireProcessing.getActionForMove(agent, "e", "e");
        }
        
        if (AgentCooperations.exists(agent.desireProcessing.stepUtilities.exploreHorizontalMapSize, agent, 1)) {
            return agent.desireProcessing.getActionForMove(agent, "n", "n");
        }

        return ActionInfo.SKIP("0010 should not happen");
    }

    /**
     * Updates the supervisor .
     * 
     * @param the new supervisor
     */
    @Override
    public void update(String supervisor) {
        this.supervisor = supervisor;
    }

    /**
     * Gets the priority.
     * 
     * @return the priority
     */
    @Override
    public int getPriority() {
        return 100;
    }
}
