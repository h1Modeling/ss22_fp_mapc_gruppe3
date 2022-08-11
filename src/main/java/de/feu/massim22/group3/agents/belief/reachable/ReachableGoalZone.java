package de.feu.massim22.group3.agents.belief.reachable;

import java.awt.Point;

import de.feu.massim22.group3.utils.DirectionUtil;

/**
 * The Record <code>ReachableGoalZone</code> stores path finding information from an agent to a cell in a goal zone.
 *
 * @param position the position of the cell
 * @param distance the distance from the agent to the cell
 * @param direction the number in which the direction to the cell is encoded
 * 
 * @author Heinz Stadler
 */
public record ReachableGoalZone(Point position, int distance, int direction) {

    /**
     * Gets the information stored in the record as a String.
     */
    public String toString() {
        String dir = DirectionUtil.intToString(direction);
        return "Reachable Goalzone at (" + position.x + "/" + position.y + ") with distance " + distance
                + " in direction " + dir;
    }
}
