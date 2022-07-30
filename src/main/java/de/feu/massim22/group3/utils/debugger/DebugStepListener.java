package de.feu.massim22.group3.utils.debugger;

import eis.iilang.Action;

/**
 * The Interface <code>DebugStepListener</code> defines methods for interaction between the <code>GraphicalDebugger</code>
 * and a class which forwards information to the simulation server. 
 *
 * @see GraphicalDebugger
 * @author Heinz Stadler
 */
public interface DebugStepListener {
    /**
     * Sends the cached agent actions to the server.
     */
    void debugStep();

    /**
     * Sets if each Action should be sent with a delay to slow down the simulation.
     * 
     * @param value true if each action should be sent with a delay
     */
    void setDelay(boolean value);

    /**
     * Sets manually an Action for a specific agent.
     * 
     * @param agent the name of the agent
     * @param a the action of the agent
     */
    void setAction(String agent, Action a);
}
