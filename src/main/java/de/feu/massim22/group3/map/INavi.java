package de.feu.massim22.group3.map;

import de.feu.massim22.group3.communication.MailService;
import de.feu.massim22.group3.utils.debugger.DebugStepListener;

import java.awt.Point;

/**
 * The Interface <code>INavi</code> defines methods for basic interaction between agents and the <code>Navi</code>.
 * The Interface can be extended to define special interaction between different implementations of agents and the <code>Navi</code>. 
 *
 * @see INaviAgentV1
 * @see INaviAgentV2
 * @see INaviTest
 * @author Heinz Stadler
 * @author Melinda Betz (minor contribution)
 */
public interface INavi extends Disposable {
    /**
     * Sets the <code>MailService</code> for communication to the agents
     * @param mailService the <code>MailService</code>
     */
    void setMailService(MailService mailService);
    
    /**
     * Registers an agent
     * @param name the name of the agent
     * @param team the name of the team of the agent
     */
    void registerAgent(String name, String team);
    
    /**
     * Gets a two-dimensional array fo type <code>CellType</code> which contains Cells of type
     * CellType.FREE or CellType.UNKNOWN depending if the cell is in vision or not.
     * 
     * @param vision the distance of the vision
     * @return the two-dimensional array fo type <code>CellType</code>
     */
    CellType[][] getBlankCellArray(int vision);

    /**
     * Sets a <code>DebugStepListener</code> to control the simulation steps by the <code>GraphicalDebugger</code>
     * @param listener the <code>DebugStepListener</code>
     * @param manualMode if true step changes are controlled by manual interaction with the <code>GraphicalDebugger</code>
     */
    void setDebugStepListener(DebugStepListener listener, boolean manualMode);

    /**
     * Frees resources managed by the Class
     */
    void dispose();

    /**
     * Calculates if a block at the provided position is attached to a known agent.
     * 
     * @param supervisor the supervisor of the agent group
     * @param p the position of the block in the coordinate system of the agent
     * @return true if there is a block at the provided position which is attached to a known agent
     */
    boolean isBlockAttached(String supervisor, Point p);

    /**
     * Gets the position of an agent
     * @param name the name of the agent
     * @param supervisor the name of the supervisor of the group the agent is part of
     * @return the position of the agent or null if the agent is not found
     */
    Point getPosition(String name, String supervisor);

    /**
     * Resets the information of an agent in the <code>Navi</code>.
     * This method should be called for all agents before each match. 
     * @param name the name of the agent
     * @param team the name of the team the agent belongs to
     */
    void resetAgent(String name, String team);

    /**
     * Tests if already agents try to discover the horizontal map size.
     * @return true if agents are already trying to discover the horizontal map size
     */
    boolean isHorizontalMapSizeInDiscover();

    /**
     * Tests if already agents try to discover the vertical map size.
     * @return true if agents are already trying to discover the vertical map size
     */
    boolean isVerticalMapSizeInDiscover();

    /**
     * Sets if the horizontal map size is currently in discovery.
     * @param value true if the horizontal map size is currently in discovery
     * @return true if the horizontal map size wasn't already in discovery 
     */
    boolean setHorizontalMapSizeInDiscover(boolean value);


    /**
     * Sets if the vertical map size is currently in discovery.
     * @param value true if the vertical map size is currently in discovery
     * @return true if the vertical map size wasn't already in discovery 
     */
    boolean setVerticalMapSizeInDiscover(boolean value);

    /**
     * Sets the horizontal size of the map.
     * @param value the horizontal map size
     */
    void setHorizontalMapSize(int value);

    /**
     * Sets the vertical size of the map.
     * @param value the vertical map size
     */
    void setVerticalMapSize(int value);
}