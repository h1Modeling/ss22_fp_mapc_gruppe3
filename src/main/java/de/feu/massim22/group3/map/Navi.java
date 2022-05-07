package de.feu.massim22.group3.map;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.lwjgl.BufferUtils;

import java.awt.Point;
import java.nio.FloatBuffer;

import massim.protocol.data.Thing;

public class Navi {
    private static Navi instance;
    private Map<String, GameMap> maps = new HashMap<>();
    private Map<String, String> agentSupervisor = new HashMap<>();
    private Map<String, Integer> agentStep = new HashMap<>();
    private Map<String, Long> openGlHandler = new HashMap<>();
    
    private Navi() {
        PathFinder.init();
        // TODO At end of application PathFinder must be closed to free resources
    }

    public static synchronized Navi get() {
        if (Navi.instance == null) {
            instance = new Navi();
        }
        return Navi.instance;
    }

    public void registerAgent(String name) {
        if (maps.containsKey(name)) {
            throw new IllegalArgumentException("Agent is already registered");
        }
        maps.put(name, new GameMap(20, 20));
        agentSupervisor.put(name, name);
        agentStep.put(name, -1);
        openGlHandler.put(name, PathFinder.createOpenGlContext());
    }

    public void updateAgent(String supervisor, String agent, int agentIndex, Point position, int vision, Set<Thing> things, List<Point> goalPoints, List<Point> rolePoints, int step) {
        if (!maps.containsKey(supervisor)) {
            throw new IllegalArgumentException("Agent " + supervisor + " is not registered yet");
        }
        GameMap map = maps.get(supervisor);

        // Set agent Position
        map.setAgentPosition(agent, position);

        // create temporary vision array
        CellType[][] thingVision = getBlankCellArray(vision);
        int size = 2 * vision + 1;
        ZoneType[][] zoneVision = new ZoneType[size][size];
        for (int y = 0; y < size; y++) {
            for (int x = 0; x < size; x++) {
                zoneVision[y][x] = ZoneType.NONE;
            }
        }

        // Fill cells with information from agent
        // Position
        thingVision[vision][vision] = CellType.TEAMMATE;
        // Things
        for (Thing t : things) {
            CellType type = thingToCellType(t);
            thingVision[t.y + vision][t.x + vision] = type;
        }
        // Role Zones
        for (Point p : rolePoints) {
            zoneVision[p.y + vision][p.x + vision] = ZoneType.ROLEZONE;
        }
        // Goal Zones
        for (Point p : goalPoints) {
            zoneVision[p.y + vision][p.x + vision] = ZoneType.GOALZONE;
        }

        // Copy infos into map
        for (int y = 0; y < size; y++) {
            for (int x = 0; x < size; x++) {
                CellType cellType = thingVision[y][x];
                ZoneType zoneType = zoneVision[y][x];
                if (cellType != CellType.UNKNOWN) {
                    map.addReport(x + position.x - vision, y + position.y - vision, cellType, zoneType, agentIndex, step);
                }
            }
        }

        // Update internal step
        agentStep.put(agent, step);

        // Test if all agents in group have already sent step information
        boolean allSent = true;
        for (String agentKey : agentStep.keySet()) {
            // Get supervisor
            String aSupervisor = agentSupervisor.get(agentKey);
            if (aSupervisor.equals(supervisor)) {
                // Test if agent is in same step
                Integer aStep = agentStep.get(agentKey);
                if (aStep < step) {
                    allSent = false;
                    break;
                }
            }
        }

        // Start calculation
        if (allSent) {
            startCalculation(supervisor, map);
        }
    }

    // Creates array with CellType.FREE in vision and CellType.UNKNOWN outside of vision
    CellType[][] getBlankCellArray(int vision) {
        int size = 2 * vision + 1;
        CellType[][] cells = new CellType[size][size];
        // Initialize with unknown cells
        for (int y = 0; y < size; y++) {
            for (int x = 0; x < size; x++) {
                cells[y][x] = CellType.UNKNOWN;
            }
        }
        // Add free cells in vision
        for (int y = 0; y < size; y++) {
            // Top Half
            if (y <= vision) {
                for (int i = 0; i < 2 * y + 1; i++) {
                    int x = vision - y + i;
                    cells[y][x] = CellType.FREE;
                }
            }
            // Bottom Half
            if (y > vision) {
                for (int x = y - vision; x < size - y + vision; x++) {
                    cells[y][x] = CellType.FREE;
                }
            }
        }
        return cells;
    }


    private List<String> getAgentsFromSupervisor(String supervisor) {
        List<String> result = new ArrayList<>();
        for (String agentKey : agentStep.keySet()) {
            // Get supervisor
            String aSupervisor = agentSupervisor.get(agentKey);
            if (aSupervisor.equals(supervisor)) {
                result.add(agentKey);
            }
        }
        return result;
    }

    private void startCalculation(String supervisor, GameMap map) {
        FloatBuffer mapBuffer = map.getMapBuffer();
        FloatBuffer agentBuffer = map.getEmptyBuffer();
        List<String> agents = getAgentsFromSupervisor(supervisor);
        int agentSize = agents.size();
        int step = agentStep.get(supervisor);

        Point mapSize = map.getMapSize();
        int channelSize = 2;
        int z = 1 + agentSize;
        
        // Buffer for 3D-RG-Texture which contains the map at z=0 and for every agent a clear map for the agent path finding result
        FloatBuffer mapTextureBuffer = BufferUtils.createFloatBuffer(mapSize.x * mapSize.y * channelSize * z);
        mapTextureBuffer.put(mapBuffer);
        for (int i = 0; i < agentSize; i++) {
            mapTextureBuffer.put(agentBuffer);
            agentBuffer.rewind();
        }
        mapTextureBuffer.flip();

        // Buffer for 2D-RG-Texture which holds three data regions (r = x-value; g = y-value)
        // y = 0: Position of agents
        // y = 1: Form of agent (attached blocks)
        // y = 2: Goal Position
        int numberGoals = 32;
        int textureSize = Math.max(agentSize, numberGoals);
        Point dataSize = new Point(textureSize, textureSize);
        FloatBuffer dataTextureBuffer = BufferUtils.createFloatBuffer(textureSize * textureSize * channelSize);
        for (int y = 0; y < textureSize; y++) {
            for (int x = 0; x < textureSize; x++) {
                // Agent position
                if (y == 0 && x < agentSize) {
                    String agent = agents.get(x);
                    Point agentPos = map.getInternalAgentPosition(agent);
                    dataTextureBuffer.put(agentPos.x);
                    dataTextureBuffer.put(agentPos.y);
                } 
                // Agent Form
                else if (y == 1 && x < agentSize) {
                    // TODO Agent Form in PathFinding
                    dataTextureBuffer.put(0);
                    dataTextureBuffer.put(0);
                } 
                // Goal Position
                else if (y == 2 && x < numberGoals) {
                    // TODO add real goals
                    dataTextureBuffer.put(3);
                    dataTextureBuffer.put(2);
                } 
                // Fill Rest
                else {
                    dataTextureBuffer.put(0);
                    dataTextureBuffer.put(0);
                }
            }
        }
        dataTextureBuffer.flip();

        long handler = openGlHandler.get(supervisor);
        PathFinder finder = new PathFinder(handler);
        boolean mapDiscovered = map.mapDiscovered();
        finder.start(mapTextureBuffer, dataTextureBuffer, mapSize, dataSize, agentSize, numberGoals, mapDiscovered, supervisor, step);
    }

    private CellType thingToCellType(Thing t) {
        switch (t.type) {
            case Thing.TYPE_BLOCK: return blockToCellType(t.details);
            case Thing.TYPE_DISPENSER: return dispenserToCellType(t.details);
            case Thing.TYPE_ENTITY: return CellType.ENTITY;
            case Thing.TYPE_OBSTACLE: return CellType.OBSTACLE;
            case Thing.TYPE_MARKER: return CellType.FREE;
            default: return CellType.UNKNOWN;
        } 
    }

    private CellType blockToCellType(String blockDetail) {
        switch (blockDetail) {
            case "b0": return CellType.BLOCK_0;
            case "b1": return CellType.BLOCK_1;
            case "b2": return CellType.BLOCK_2;
            case "b3": return CellType.BLOCK_3;
            case "b4": return CellType.BLOCK_4;
            default: return CellType.UNKNOWN;
        }
    }

    private CellType dispenserToCellType(String blockDetail) {
        switch (blockDetail) {
            case "b0": return CellType.DISPENSER_0;
            case "b1": return CellType.DISPENSER_1;
            case "b2": return CellType.DISPENSER_2;
            case "b3": return CellType.DISPENSER_3;
            case "b4": return CellType.DISPENSER_4;
            default: return CellType.UNKNOWN;
        }
    }
}
