package de.feu.massim22.group3.agents.Reachable;

import de.feu.massim22.group3.agents.DirectionUtil;
import de.feu.massim22.group3.map.CellType;

import java.awt.Point;

/**
 * The Record <code>ReachableDispenser</code> stores path finding information from an agent to a cell adjacent to a dispenser.
 *
 * @param position the position of the cell
 * @param type the cell type of the dispenser
 * @param distance the distance from the agent to the cell
 * @param direction the number in which the direction to the cell is encoded
 * @param data the side of the cell relative to the dispenser
 * 
 * @author Heinz Stadler
 */
public record ReachableDispenser(Point position, CellType type, int distance, int direction, String data) {

    /**
     * Gets the information stored in the record as a String.
     */
    public String toString() {
        String dir = DirectionUtil.intToString(direction);
        return "Reachable " + type.name() + " at (" + position.x + "/" + position.y + ") with distance " + distance
                + " in direction " + dir + ", Side: " + data;
    }
}