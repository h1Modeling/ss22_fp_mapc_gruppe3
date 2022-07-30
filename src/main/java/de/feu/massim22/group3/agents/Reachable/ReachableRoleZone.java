package de.feu.massim22.group3.agents.Reachable;

import de.feu.massim22.group3.agents.DirectionUtil;

import java.awt.Point;

/**
 * The Record <code>ReachableRoleZone</code> stores path finding information from an agent to a cell in a role zone.
 *
 * @param position the position of the cell
 * @param distance the distance from the agent to the cell
 * @param direction the number in which the direction to the cell is encoded
 * 
 * @author Heinz Stadler
 */
public record ReachableRoleZone(Point position, int distance, int direction) {

    /**
     * Gets the information stored in the record as a String.
     */
    public String toString() {
        String dir = DirectionUtil.intToString(direction);
        return "Reachable Rolezone at (" + position.x + "/" + position.y + ") with distance " + distance
                + " in direction " + dir;
    }
}
