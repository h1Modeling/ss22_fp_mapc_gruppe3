package de.feu.massim22.group3.communication;

import de.feu.massim22.group3.agents.Agent;
import eis.iilang.Action;

/**
 * The Interface <code>EisSender</code> provides a method to send an action to the simulation server.
 * 
 * @author Heinz Stadler
 */
public interface EisSender {
    /**
     * Sends an action to the simulation server.
     * 
     * @param agent the name of the agent the action comes from
     * @param action the action of the agent
     */
    void send(Agent agent, Action action);
}
