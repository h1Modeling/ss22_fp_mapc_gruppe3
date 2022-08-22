package de.feu.massim22.group3.agents.desires;

/**
 * The Class <code>GroupDesireTypes</code> provides constants which define group desire types.
 * 
 * @author Heinz Stadler
 */
public abstract class GroupDesireTypes {
    /** No group desire is hold by the agent. */
    public static final String NONE = "none";

    /** The agent is tasked to explore the map size. */
    public static final String EXPLORE = "explore";

    /** The agent is tasked to forward a block to a team mate. */
    public static final String TASK = "task";

    /** The agent is tasked to guard a goal zone or dispenser. */
    public static final String GUARD = "guard";

    /** The agent is tasked to get a block for a multi block task. */
    public static final String GET_BLOCK = "block";

    /** The agent is tasked to deliver and attach a block to a team mate. */
    public static final String DELIVER_ATTACH = "deliver_attach";

    /** The agent is tasked to receive and attach a block from a team mate. */
    public static final String RECEIVE_ATTACH = "receive_attach";
}
