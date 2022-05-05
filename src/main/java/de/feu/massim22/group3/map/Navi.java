package de.feu.massim22.group3.map;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.awt.Point;

import massim.protocol.data.Thing;

public class Navi {
    private static Navi instance;
    private Map<String, GameMap> maps = new HashMap<>();
    
    private Navi() {

    }

    public static synchronized Navi get() {
        if (Navi.instance == null) {
            instance = new Navi();
        }
        return Navi.instance;
    }

    public void registerAgent(String name) {
        if (maps.containsKey(name)) {
            throw new IllegalArgumentException("Agent is already registerd");
        }
        maps.put(name, new GameMap(20, 20));
    }

    public void updateAgent(String name, Point position, Set<Thing> things, List<Point> goalPoints, List<Point> rolePoints) {
        if (maps.containsKey(name)) {
            throw new IllegalArgumentException("Agent is not registered yet");
        }
        GameMap map = maps.get(name);
    }

    // TODO 
    public void startCalculation() {
        PathFinder finder = new PathFinder();
        new Thread(finder).start();
    }
}
