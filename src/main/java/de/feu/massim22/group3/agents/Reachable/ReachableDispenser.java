package de.feu.massim22.group3.agents.Reachable;

import de.feu.massim22.group3.agents.DirectionUtil;
import de.feu.massim22.group3.map.CellType;

import java.awt.Point;

public record ReachableDispenser(Point position, CellType type, int distance, int direction, String data) {
    public String toString() {
        String dir = DirectionUtil.intToString(direction);
        return "Reachable " + type.name() + " at (" + position.x + "/" + position.y + ") with distance " + distance
                + " in direction " + dir + ", Side: " + data;
    }
}