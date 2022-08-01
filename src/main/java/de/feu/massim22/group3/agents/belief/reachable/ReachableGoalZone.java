package de.feu.massim22.group3.agents.belief.reachable;

import de.feu.massim22.group3.agents.DirectionUtil;

import java.awt.Point;

public record ReachableGoalZone(Point position, int distance, int direction) {
    public String toString() {
        String dir = DirectionUtil.intToString(direction);
        return "Reachable Goalzone at (" + position.x + "/" + position.y + ") with distance " + distance
                + " in direction " + dir;
    }
}
