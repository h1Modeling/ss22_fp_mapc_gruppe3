package de.feu.massim22.group3.map;

import java.awt.Point;
import java.util.List;
import java.util.Set;

import massim.protocol.data.Thing;

public interface INaviAgentV2 extends INavi {
    void updateMap(String supervisor, String agent, int agentIndex, Point position, int vision, Set<Thing> things, List<Point> goalPoints, List<Point> rolePoints, int step);
    void updateSupervisor(String supervisor);
}