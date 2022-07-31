package de.feu.massim22.group3.agents.supervisor;

import eis.iilang.Percept;

/**
 * The Interface <code>Supervisable</code> defines methods for communication between an agent and its supervisor and should
 * be implemented by any agent which has a supervisor.
 * 
 * @author Heinz Stadler
 */
public interface Supervisable {
    /**
     * Sends the provided Percept by the mail service.
     * 
     * @param message the percept message to send
     * @param receiver the receiver of the message
     * @param sender the sender of the message
     */
    void forwardMessage(Percept message, String receiver, String sender);

    /**
     * Gets the name of the agent.
     * 
     * @return the name of the agent
     */
    String getName();

    /**
     * Updates the simulation step of the supervisor.
     */
    void initSupervisorStep();
}
