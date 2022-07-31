package de.feu.massim22.group3.agents.intention;

import de.feu.massim22.group3.agents.desires.ActionInfo;
import eis.iilang.Action;

/**
 * The Interface <code>IIntention</code> provides methods for accessing the current intention of an agent.
 * 
 * @author Heinz Stadler
 * @author Melinda Betz
 */
public interface IIntention {
    /**
     * Gets information about the next action of the agent.
     * 
     * @return the next action info
     */
    ActionInfo getNextActionInfo();

    /**
     * Gets the name of the intention.
     * 
     * @return the name of the intention
     */
    String getName();
    
    /**
     * Get the next Action from cache.
     * 
     * @return the next action
     */
    Action getOutputAction();

    /**
     * Sets the next action cache.
     * 
     * @param action the next action
     */
    void setOutputAction(Action action);
}
