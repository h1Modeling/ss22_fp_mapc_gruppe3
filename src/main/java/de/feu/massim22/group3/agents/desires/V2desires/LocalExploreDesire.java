package de.feu.massim22.group3.agents.desires.V2desires;

import de.feu.massim22.group3.agents.*;
import de.feu.massim22.group3.agents.belief.Belief;
import de.feu.massim22.group3.agents.desires.*;
import de.feu.massim22.group3.utils.DirectionUtil;
import de.feu.massim22.group3.utils.logging.AgentLogger;
import eis.iilang.Identifier;

/**
 * The class <code>LocalExploreDesire</code> models the desire to do a local explore.
 * 
 * @author Melinda Betz
 */
public class LocalExploreDesire extends BeliefDesire {

    private BdiAgentV2 agent;
    private String supervisor;

    /**
     * Instantiates a new LocalExploreDesire.
     * 
     * @param belief the belief of the agent
     * @param supervisor the supervisor of the group
     * @param agent the agent who wants to go to a goal zone
     * 
     */
    public LocalExploreDesire(Belief belief, String supervisor, BdiAgentV2 agent) {
        super(belief);
        AgentLogger.info(Thread.currentThread().getName() + " runAgentDecisions - Start LocalExploreDesire");
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

        /*if (agent.blockAttached && agent.getBelief().getGoalZones().contains(Point.zero())) {
            return ActionInfo.SKIP("0001 with block in goalzone; where should I go?");
        } else {*/
            agent.exploreDirection = DirectionUtil
                    .stringToInt(agent.desireProcessing.walkCircles(agent, 10).toString());
            agent.exploreDirection2 = (agent.exploreDirection2 + 5) % 4;
        //}

        return agent.desireProcessing.getActionForMove(agent, DirectionUtil.intToString(agent.exploreDirection),
                DirectionUtil.intToString(agent.exploreDirection2), getName());
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