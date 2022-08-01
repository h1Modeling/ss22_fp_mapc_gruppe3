package de.feu.massim22.group3.agents.belief.reachable;

import java.awt.Point;

import de.feu.massim22.group3.utils.DirectionUtil;

public record ReachableTeammate(Point position, String name, int distance, int direction) {
    public String toString() {
        String dir = DirectionUtil.intToString(direction);
        return "Reachable Agent " + name + " at (" + position.x + "/" + position.y + ") with distance " + distance
                + " in direction " + dir;
    }
}