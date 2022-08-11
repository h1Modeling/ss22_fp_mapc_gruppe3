package de.feu.massim22.group3.agents.events;

/**
 * The Enumeration <code>SupervisorEventName</code> indicates the event received from a message in <code>Supervisor</code>
 *
 * @see de.feu.massim22.group3.agents.supervisor.Supervisor
 * @author Heinz Stadler
 */
public enum SupervisorEventName {
    /** An agent report is received. */
    REPORT,
    /** An agent sent an attach request. */
    ATTACH_REQUEST,
}