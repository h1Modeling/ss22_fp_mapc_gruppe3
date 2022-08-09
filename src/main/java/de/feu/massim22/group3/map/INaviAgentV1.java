package de.feu.massim22.group3.map;

import java.awt.Point;
import java.util.List;
import java.util.Set;

import de.feu.massim22.group3.utils.debugger.debugData.DesireDebugData;
import massim.protocol.data.*;

/**
 * The Interface <code>INaviAgentV1</code> defines methods for special interaction between <code>BdiAgentV1</code> and the <code>Navi</code>.
 *
 * @see INavi
 * @see INaviAgentV2
 * @see INaviTest
 * @see de.feu.massim22.group3.agents.BdiAgentV1
 * @author Heinz Stadler
 */
public interface INaviAgentV1 extends INavi {

    /**
     * Saves data from an agent received in the current step into the <code>GameMap</code> and starts path finding.
     * 
     * @param supervisor the name of the supervisor of the group the agent is part of
     * @param agent the name of the agent
     * @param agentIndex the index of the agent
     * @param position the current position of the agent
     * @param vision the size of the vision of the agent
     * @param things the things which are currently in vision
     * @param goalPoints the goal points which are currently in vision
     * @param rolePoints the role points which are currently in vision
     * @param step the current step of the simulation
     * @param team the name of the team the agent is part of
     * @param maxSteps the number of steps the match has
     * @param score the current score of the team
     * @param normsInfo the current active norms
     * @param taskInfo the current active tasks
     * @param attachedPoints the attached points of the agent
     * @return a two dimensional Array of type <code>PathFindingResult</code> which contains information
     * about distance and direction of certain interesting points.
     */
    PathFindingResult[][] updateMapAndPathfind(String supervisor, String agent, int agentIndex, Point position, int vision, Set<Thing> things,
            List<Point> goalPoints, List<Point> rolePoints, int step, String team, int maxSteps, int score,
            Set<NormInfo> normsInfo, Set<TaskInfo> taskInfo, List<Point> attachedPoints);
    
    /**
     * Sends current agent data to the <code>GraphicalDebugger</code>.
     * 
     * @param agent the name of the agent
     * @param supervisor the name of the supervisor of the group the agent is part of
     * @param role the name of the current role of the agent
     * @param energy the current energy of the agent
     * @param lastAction the name of the last action performed by the agent
     * @param lastActionSuccess an indicator if the last action was successful
     * @param lastActionIntention the name of the intention the last action came from
     * @param groupDesireType the name of the group desire the agent has in its desires
     * @param groupDesirePartner the name of the team mate which performs a group desire with the agent
     * @param groupDesireBlock the block on which the group desire is based on
     * @param attachedThingsDebugString a String containing information about the attached things of the agent
     * @see GraphicalDebugger
     */
    void updateAgentDebugData(String agent, String supervisor, String role, int energy, String lastAction, String lastActionSuccess, String lastActionIntention, String groupDesireType, String groupDesirePartner, String groupDesireBlock, String attachedThingsDebugString);
    
    /**
     * Sends information about the agents desires to the <code>GraphicalDebugger</code>.
     * 
     * @param data a List of DesireDebugData
     * @param agent the name of the agent
     * @see GraphicalDebugger
     */
    void updateDesireDebugData(List<DesireDebugData> data, String agent);

    /**
     * Accept a merge request.
     * 
     * @param mergeKey the key of the merge request
     * @param name the name of the agent which accepts the merge request
     */
    void acceptMerge(String mergeKey, String name);

    /**
     * Reject a merge request.
     * 
     * @param mergeKey the key of the merge request
     * @param name the name of the agent which accepts the merge request
     */
    void rejectMerge(String mergeKey, String name);

    /**
     * Gets the direction to the nearest undiscovered point in the <code>GameMap</code>.
     * 
     * @param supervisor the name of the supervisor of the group the agent is part of
     * @param agent the name of the agent
     * @return the direction to the nearest undiscovered point - can be "n", "e", "s", "w"
     */
    String getDirectionToNearestUndiscoveredPoint(String supervisor, String agent);

    /**
     * Gets the state of the <code>Navi</code>
     * @return true if the <code>Navi</code> is waiting for response from an agent or is doing some
     * calculations
     */
    boolean isWaitingOrBusy();

    /**
     * Gets the agent index of an agent at a certain point.
     * @param supervisor the name of the supervisor of the group the agent is part of
     * @param p the point
     * @return the index of the agent at the provided point or 0 if no agent is found
     */
    int getAgentIdAtPoint(String supervisor, Point p);

    /**
     * Gets a <code>List</code> of Points in goal zones which have enough free
     * space around to comfortably assemble blocks. 
     * 
     * @param supervisor the name of the supervisor of the group the agent is part of
     * @return the <code>List</code> of Points
     */
    List<Point> getMeetingPoints(String supervisor);
}
