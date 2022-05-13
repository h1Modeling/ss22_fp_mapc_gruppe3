package de.feu.massim22.group3.map;

import java.awt.Point;
import java.util.List;
import java.util.Set;

import massim.protocol.data.Thing;

public interface INaviAgentV1 extends INavi {
    void updateMapAndPathfind(String supervisor, String agent, int agentIndex, Point position, int vision, Set<Thing> things, List<Point> goalPoints, List<Point> rolePoints, int step);
}
