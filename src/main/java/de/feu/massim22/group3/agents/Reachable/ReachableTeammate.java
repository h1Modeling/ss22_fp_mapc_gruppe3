package de.feu.massim22.group3.agents.Reachable;

import de.feu.massim22.group3.agents.DirectionUtil;

import java.awt.Point;

/**
 * The Record <code>ReachableTeammate</code> stores path finding information from an agent to another agent.
 *
 * @param position the position of the goal agent
 * @param name the name of the goal agent
 * @param distance the distance from the agent to the goal agent
 * @param direction the number in which the direction to the gaol agent is encoded
 * 
 * @author Heinz Stadler
 */
public record ReachableTeammate(Point position, String name, int distance, int direction) {

    /**
     * Gets the information stored in the record as a String.
     */
    public String toString() {
        String dir = DirectionUtil.intToString(direction);
        return "Reachable Agent " + name + " at (" + position.x + "/" + position.y + ") with distance " + distance
                + " in direction " + dir;
    }
}