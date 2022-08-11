package de.feu.massim22.group3.map;

/**
 * The Interface <code>INaviTest</code> defines methods for testing the <code>Navi</code>.
 *
 * @see INavi
 * @see INaviAgentV1
 * @see INaviAgentV2
 * @author Heinz Stadler
 */
public interface INaviTest extends INavi {
    /**
     * Resets the <code>Navi</code> to the initial state.
     */
    void clear();

    /**
     * Sets the debug mode
     * @param debug if true the <code>GraphicalDebugger</code> will be started at program launch
     */
    void setDebug(boolean debug);
}
