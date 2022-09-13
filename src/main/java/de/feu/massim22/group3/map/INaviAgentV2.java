package de.feu.massim22.group3.map;

import java.awt.Point;
import java.nio.FloatBuffer;
import java.util.*;

import massim.protocol.data.*;

/**
 * The Interface <code>INaviAgentV2</code> defines methods for special interaction between <code>BdiAgentV2</code> and the <code>Navi</code>.
 *
 * @see INavi
 * @see INaviAgentV1
 * @see INaviTest
 * @see de.feu.massim22.group3.agents.BdiAgentV2
 * @author Heinz Stadler
 * @author Melinda Betz (minor contribution)
 */
public interface INaviAgentV2 extends INavi {

    /**
     * Saves data from an agent received in the current step into the <code>GameMap</code>.
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
     */
    void updateMap(String supervisor, String agent, int agentIndex, Point position, int vision, Set<Thing> things,
            List<Point> goalPoints, List<Point> rolePoints, int step, String team, int maxSteps, int score,
            Set<NormInfo> normsInfo, Set<TaskInfo> taskInfo, List<Point> attachedPoints);

    /**
     * Starts path finding for the the group of the supervisor
     * @param supervisor the name of the supervisor
     * @return a List of pathfinding calculation results
     */
    List<CalcResult> updateSupervisor(String supervisor);

    /**
     * Connects a supervisor to an agent
     * @param name the name of the agent
     * @param supervisor the new supervisor of the agent
     */
    void registerSupervisor(String name, String supervisor);

    /**
     * Get information about discovered dispenser, role zones and goal zones
     * @param supervisor the supervisor of the agent group
     * @param maxNumberGoals the maximal number of interesting points 
     * @return a List of <code>InterestingPoints</code> containing information about dispenser, role zones
     * and goal zones
     */
    List<InterestingPoint> getInterestingPoints(String supervisor, int maxNumberGoals);
    
    /** Gets the position of the point in the top left corner of the <code>GameMap</code>.
     * 
     * @param supervisor the name of the supervisor
     * @return the position of the point in the top left corner of the <code>GameMap</code>
     */
    Point getTopLeft(String supervisor);
    
    /**
     * Gets the position of the agent in the internal coordinate system of the <code>GameMap</code>.
     * 
     * @param supervisor the name of the supervisor of the group the agent is part of
     * @param agent the name of the agent
     * @return the position of the agent in the internal coordinate system of the <code>GameMap</code>
     */
    Point getInternalAgentPosition(String supervisor, String agent);
    
    /**
     * Gets a <code>FloatBuffer</code> which encodes the Cells of the <code>GameMap</code>.
     * Obstacles or undiscovered cells are encoded as 1f, other types are encoded as 0f.
     * 
     * @param supervisor the supervisor
     * @return the <code>FloatBuffer</code> which encodes the Cells of the <code>GameMap</code>
     */
    FloatBuffer getMapBuffer(String supervisor);

    /**
     * Get the maps of all supervisors
     * @return the maps
     */
    Map<String, GameMap> getMaps();
}