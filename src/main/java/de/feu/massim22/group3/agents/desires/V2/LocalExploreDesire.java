package de.feu.massim22.group3.agents.desires.V2;

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
        AgentLogger.info(Thread.currentThread().getName() + "LocalExploreDesire.getNextAction() - Agent: " + agent.getName());

        if (agent.getIntention() != null && agent.getIntention().getName().equals("LocalExploreDesire")) {
            if (agent.exploreCount > 40) {
                agent.exploreDirection = (agent.exploreDirection + 1) % 4;
                agent.exploreDirection2 = (agent.exploreDirection2 + 1) % 4;
                agent.exploreCount = 0;
            } else
                agent.exploreCount++;
        } else {
            agent.exploreCount = 0;
   
            if (!agent.belief.getRoleName().equals("default")) 
            // go towards the direction of the empty goal zone to do multi block tasks
                if (agent.belief.getPosition().y < 28)
                    agent.exploreDirection = DirectionUtil.stringToInt(DirectionUtil.getDirection(agent.belief.getPosition(), new Point(9, 1)));
                else
                    agent.exploreDirection = DirectionUtil.stringToInt(DirectionUtil.getDirection(agent.belief.getPosition(), new Point(28, 54)));
            agent.exploreDirection2 = (agent.exploreDirection2 + 5) % 4;
        }
         
         return agent.desireProcessing.getActionForMove(agent, DirectionUtil.intToString(agent.exploreDirection), DirectionUtil.intToString(agent.exploreDirection2), getName());
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