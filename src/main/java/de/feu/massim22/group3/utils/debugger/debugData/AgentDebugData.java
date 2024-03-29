package de.feu.massim22.group3.utils.debugger.debugData;

/**
 * The Record <code>AgentDebugData</code> provides a data structure to store information about an agent.
 * 
 * @param name the name of the agent
 * @param supervisor the supervisor of the agent
 * @param role the current role of the agent
 * @param energy the current energy of the agent
 * @param lastAction the last performed action of the agent
 * @param lastActionSuccess the action request result of the last action
 * @param lastActionDesire the desire which generated the last action
 * @param groupDesireType the current group desire of the agent
 * @param groupDesirePartner the name of the team mate which performs a group desire with the agent
 * @param groupDesireBlock the block on which the group desire is based on
 * @param attachedThings a String containing information about the attached things to the agent
 *
 * @author Heinz Stadler
 */
public record AgentDebugData (
    String name,
    String supervisor,
    String role,
    int energy,
    String lastAction,
    String lastActionSuccess,
    String lastActionDesire,
    String groupDesireType,
    String groupDesirePartner,
    String groupDesireBlock,
    String attachedThings
) {}
