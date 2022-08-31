package de.feu.massim22.group3.agents.desires.V2desires;

import de.feu.massim22.group3.agents.*;
import de.feu.massim22.group3.agents.V2utils.*;
import de.feu.massim22.group3.agents.belief.Belief;
import de.feu.massim22.group3.agents.desires.*;
import de.feu.massim22.group3.utils.logging.AgentLogger;

/**
 * The class <code>ExploreMapSizeDesire</code> models the desire to explore the map size.
 * 
 * @author Melinda Betz
 */
public class ExploreMapSizeDesire extends BeliefDesire {

    private BdiAgentV2 agent;

    /**
     * Instantiates a new ExploreMapSizeDesire.
     * 
     * @param belief the belief of the agent
     * @param supervisor the supervisor of the group
     * @param agent the agent who wants to go to a goal zone
     * 
     */
    public ExploreMapSizeDesire(Belief belief, BdiAgentV2 agent) {
        super(belief);
        AgentLogger.info(Thread.currentThread().getName() + " runAgentDecisions - Start ExploreMapSizeDesire");
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
        AgentLogger.info(
                Thread.currentThread().getName() + " ExploreMapSizeDesire.isExecutable() - Agent: " + agent.getName());
        if (AgentCooperations.exists(StepUtilities.exploreHorizontalMapSize, agent)
                || AgentCooperations.exists(StepUtilities.exploreVerticalMapSize, agent)) {
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
                Thread.currentThread().getName() + " ExploreMapSizeDesire.getNextAction() - Agent: " + agent.getName());
        AgentLogger.info(Thread.currentThread().getName() + " ExploreMapSizeDesire.getNextAction() - horizontal: " 
                + (AgentCooperations.get(StepUtilities.exploreHorizontalMapSize, agent) != null ? 
                        AgentCooperations.get(StepUtilities.exploreHorizontalMapSize, agent).toString() : ""));
        AgentLogger.info(Thread.currentThread().getName() + " ExploreMapSizeDesire.getNextAction() - vertical: " 
        + (AgentCooperations.get(StepUtilities.exploreVerticalMapSize, agent) != null ? 
                AgentCooperations.get(StepUtilities.exploreVerticalMapSize, agent).toString() : ""));
        
        if (AgentCooperations.exists(StepUtilities.exploreHorizontalMapSize, agent, 2) 
            || AgentCooperations.exists(StepUtilities.exploreVerticalMapSize, agent, 2)) {
            return ActionInfo.SKIP("2000 waiting for explore map size finished");
        }
        
        if (AgentCooperations.exists(StepUtilities.exploreHorizontalMapSize, agent, 1)) {
            return agent.desireProcessing.getActionForMove(agent, "e", "e", getName());
        }
        
        if (AgentCooperations.exists(StepUtilities.exploreVerticalMapSize, agent, 1)) {
            return agent.desireProcessing.getActionForMove(agent, "s", "s", getName());
        }

        return ActionInfo.SKIP("0010 should not happen");
    }

    /**
     * Updates the supervisor .
     * 
     * @param the new supervisor
     */
   /* @Override
    public void update(String supervisor) {
        this.supervisor = supervisor;
    }*/

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
