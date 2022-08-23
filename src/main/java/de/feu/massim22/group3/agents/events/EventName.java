package de.feu.massim22.group3.agents.events;

/**
 * The Enumeration <code>EventName</code> indicates the event received from a message in <code>BdiAgentV1</code>
 *
 * @see de.feu.massim22.group3.agents.BdiAgentV1
 * @author Heinz Stadler
 */
public enum EventName {
    /** A new step has started. */
    UPDATE,
    /** A message to the supervisor is received. */
    TO_SUPERVISOR,
    /** A message from the supervisor is received. */
    FROM_SUPERVISOR,
    /** Path finding information are received. */
    PATHFINDER_RESULT,
    /** A group merge suggestion is received. */
    MERGE_SUGGESTION,
    /** An agent group update is received. */
    UPDATE_GROUP,
    /** A group member update is received. */
    ADD_GROUP_MEMBERS,
    /** A possible connection message is received. */
    REPORT_POSSIBLE_CONNECTION,
    /** The supervisor instructs to deliver a block. */
    SUPERVISOR_PERCEPT_DELIVER_BLOCK,
    /** The supervisor order to deliver a block is canceled. */
    SUPERVISOR_PERCEPT_DELIVER_BLOCK_DONE,
    /** The supervisor instructs to receive a block. */
    SUPERVISOR_PERCEPT_RECEIVE_BLOCK,
    /** The supervisor instructs to deliver a block to an agent which delivers a two block task. */
    SUPERVISOR_PERCEPT_DELIVER_TWO_BLOCK,
    /** The supervisor instructs to receive a block from an agent and then deliver a two block task. */
    SUPERVISOR_PERCEPT_RECEIVE_TWO_BLOCK,
    /** The supervisor instructs to get a block. */
    SUPERVISOR_PERCEPT_GET_BLOCK,
    /** The last supervisor order should be canceled. */
    SUPERVISOR_PERCEPT_CANCELED,
    /** The last supervisor order should be canceled because it's done or canceled by another agent. */
    SUPERVISOR_PERCEPT_DONE_OR_CANCELED,
    /** The supervisor sent a reply to an attach request. */
    ATTACH_REPLY,
    MEASURE_MOVE,

    MEASURE_MEET,

    /** The supervisor instructs to guard a dispenser. */
    SUPERVISOR_PERCEPT_GUARD_DISPENSER,
    /** The supervisor instructs to guard a goal zone. */
    SUPERVISOR_PERCEPT_GUARD_GOAL_ZONE,
    /** The supervisor instructs to delete all current group desires. */
    SUPERVISOR_PERCEPT_DELETE_GROUP_DESIRES,
    /** The supervisor instructs to explore the map size */
    SUPERVISOR_PERCEPT_EXPLORE_MAP_SIZE,
    /** The map size is discovered and sent to all agents */
    MAP_SIZE_DISCOVERED,
    MEASURE_DONE,

    SIZE_SEND,
}
