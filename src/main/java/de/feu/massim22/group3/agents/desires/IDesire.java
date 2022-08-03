package de.feu.massim22.group3.agents.desires;

import de.feu.massim22.group3.agents.intention.IIntention;

/**
 * The Interface <code>IDesire</code> provides methods for interacting with agent desires.
 * 
 * @author Heinz Stadler
 */
public interface IDesire extends IIntention {
    /**
     * Tests if the desire is already fulfilled.
     * 
     * @return true if the desire is fulfilled
     */
    BooleanInfo isFulfilled();

    /**
     * Tests if the desire can be executed.
     * 
     * @return true if the desire can be executed
     */
    BooleanInfo isExecutable();

    /**
     * Tests if the desire is unfulfillable and should be removed.
     * 
     * @return true if the desire is unfulfillable
     */
    BooleanInfo isUnfulfillable();

    /**
     * Updates the desire and should be called every step.
     * 
     * @param supervisor the name of the supervisor of the agent
     */
    void update(String supervisor);

    /**
     * Gets the priority of the desire.
     * 
     * @return the priority of the desire
     */
    int getPriority();

    /**
     * Tests if the desire is a group desire.
     * 
     * @return true if the desire is a group desire
     */
    boolean isGroupDesire();
}
