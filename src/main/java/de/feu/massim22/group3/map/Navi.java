package de.feu.massim22.group3.map;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.lwjgl.BufferUtils;

import de.feu.massim22.group3.MailService;
import de.feu.massim22.group3.TaskName;
import de.feu.massim22.group3.utils.logging.AgentLogger;
import eis.iilang.Function;
import eis.iilang.Identifier;
import eis.iilang.Numeral;
import eis.iilang.Parameter;
import eis.iilang.TruthValue;
import eis.iilang.Percept;

import java.awt.Point;
import java.nio.FloatBuffer;

import massim.protocol.data.Thing;

public class Navi implements INaviAgentV1, INaviAgentV2 {
    private static Navi instance;
    private String name = "Navi";
    private MailService mailService;
    private Map<String, GameMap> maps = new HashMap<>();
    private Map<String, String> agentSupervisor = new HashMap<>();
    private Map<String, Integer> agentStep = new HashMap<>();
    private Map<String, Long> openGlHandler = new HashMap<>();
    
    private Navi() {
        PathFinder.init();
        // TODO At end of application PathFinder must be closed to free resources
    }

    @SuppressWarnings("unchecked")
    public static synchronized <T extends INavi> T get() {
        if (Navi.instance == null) {
            instance = new Navi();
        }
        return (T)(Navi.instance);
    }

    public void setMailService(MailService mailService) {
        this.mailService = mailService;
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
   
    // Melinda
    @Override
    public void registerSupervisor(String name, String supervisor) {
        agentSupervisor.put(name, supervisor);
    }
    
    @Override
    public Point getPosition(String name, String supervisor) {
        return maps.get(supervisor).getAgentPosition(name);
    }
    //Melinda Ende

    @Override
    public void updateMapAndPathfind(String supervisor, String agent, int agentIndex, Point position, int vision, Set<Thing> things, List<Point> goalPoints, List<Point> rolePoints, int step) {

        updateMap(supervisor, agent, agentIndex, position, vision, things, goalPoints, rolePoints, step);

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
            GameMap map = maps.get(supervisor);
            startCalculation(supervisor, map);
        }
    }

    @Override
    public void updateMap(String supervisor, String agent, int agentIndex, Point position, int vision, Set<Thing> things, List<Point> goalPoints, List<Point> rolePoints, int step) {
        AgentLogger.info("updateMap() Start - Agent: " + agent + " , Supervisor: " + supervisor); 
        if (!maps.containsKey(supervisor)) {
            throw new IllegalArgumentException("Agent " + supervisor + " is not registered yet");
        }
        GameMap map = maps.get(supervisor);

        // Set agent Position
        map.setAgentPosition(agent, position);
        AgentLogger.info("updateMap() Start - AgentPosition: " + agent + " , " + map.getInternalAgentPosition(agent));

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
        AgentLogger.info("updateMap() End"); 
    }

    // Creates array with CellType.FREE in vision and CellType.UNKNOWN outside of vision
    public CellType[][] getBlankCellArray(int vision) {
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

    private synchronized void startCalculation(String supervisor, GameMap map) {
        AgentLogger.info("startCalculation() Start - Supervisor: " + supervisor); 
        FloatBuffer mapBuffer = map.getMapBuffer();
        FloatBuffer agentBuffer = map.getEmptyBuffer();
        List<String> agents = getAgentsFromSupervisor(supervisor);
        int agentSize = agents.size();
        int step = agentStep.get(supervisor);

        Point mapSize = map.getMapSize();
        int channelSize = 2;
        int z = 1 + agentSize;

        AgentLogger.info("startCalculation() - agents: " + agents);
        AgentLogger.info("startCalculation() - mapSize: " + mapSize);
        
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
        int maxNumberGoals = 32;
        int dataY = 3;
        List<InterestingPoint> interestingPoints = map.getInterestingPoints(maxNumberGoals);
        int numberGoals = interestingPoints.size();
        int textureSize = Math.max(agentSize, numberGoals);
        Point dataSize = new Point(textureSize, dataY);
        FloatBuffer dataTextureBuffer = BufferUtils.createFloatBuffer(textureSize * dataY * channelSize);
        for (int y = 0; y < 3; y++) {
            for (int x = 0; x < textureSize; x++) {
                // Agent position
                if (y == 0 && x < agentSize) {
                    String agent = agents.get(x);
                    AgentLogger.info("startCalculation() - Agent for Position: " + agent);
                    Point agentPos = map.getInternalAgentPosition(agent);
                    dataTextureBuffer.put(agentPos.x);
                    dataTextureBuffer.put(agentPos.y);
                    AgentLogger.info("startCalculation() - AgentPosition: " + agent + " , " + agentPos);
                } 
                // Agent Form
                else if (y == 1 && x < agentSize) {
                    // TODO Agent Form in PathFinding
                    dataTextureBuffer.put(0);
                    dataTextureBuffer.put(0);
                } 
                // Interesting Points Position (Path-Finding Goals)
                else if (y == 2 && x < numberGoals) {
                    InterestingPoint ip = interestingPoints.get(x);
                    Point p = ip.point();
                    dataTextureBuffer.put(p.x);
                    dataTextureBuffer.put(p.y);
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
        AgentLogger.info("startCalculation() - numberGoals: " + numberGoals);        
        if (numberGoals > 0) {
            // Start Path Finding
            AgentLogger.info("Before finder.start()");   
            PathFindingResult[][] result = finder.start(mapTextureBuffer, dataTextureBuffer, interestingPoints, mapSize, dataSize, agentSize, numberGoals, mapDiscovered, supervisor, step);
            AgentLogger.info("After finder.start()");             
            // Send Result to Agents
            if (mailService != null) {
                AgentLogger.info("############## ");
                AgentLogger.info("startCalculation() - Agenten: " + agents); 
                AgentLogger.info("############## ");
                for (int i = 0; i < agents.size(); i++) {
                    AgentLogger.info("startCalculation() - Loop Agent: " +  agents.get(i)); 
                    AgentLogger.info("PathFindingResult Index 1: " + result[i][1].distance());
                    AgentLogger.info("PathFindingResult Index 1: " + result[i][1].direction());
                    AgentLogger.info("InterestingPoint Index 1: " + interestingPoints.get(i));
                    String agent = agents.get(i);
                    PathFindingResult[] agentResultData = result[i];
                    Point mapTopLeft = map.getTopLeft();
                    sendPathFindingResultToAgent(agent, agentResultData, interestingPoints, mapTopLeft);
                    AgentLogger.info("############## ");
                }
            }
        }
        
        AgentLogger.info("startCalculation() - End"); 
    }

    private Percept sendPathFindingResultToAgent(String agent, PathFindingResult[] agentResultData, List<InterestingPoint> interestingPoints, Point mapTopLeft) {
        AgentLogger.info ("sendPathFindingResultToAgent() Start");
        AgentLogger.info ("sendPathFindingResultToAgent() - interestingPoints.size: " + interestingPoints.size());
        List<Parameter> data = new ArrayList<>();
        // Generate Percept
        for (int j = 0; j < interestingPoints.size(); j++) {
            AgentLogger.info("------------------------- ");
            PathFindingResult resultData = agentResultData[j];
            AgentLogger.info ("sendPathFindingResultToAgent() - resultData.distance: " + resultData.distance());
            AgentLogger.info ("sendPathFindingResultToAgent() - resultData.direction: " + resultData.direction());
            AgentLogger.info ("sendPathFindingResultToAgent() - InterestingPoint: " + interestingPoints.get(j));
            // Result was found
            if (resultData.distance() > 0) {
                InterestingPoint ip = interestingPoints.get(j);
                Parameter distance = new Numeral(resultData.distance());
                Parameter direction = new Numeral(resultData.direction());
                boolean iZ = ip.cellType().equals(CellType.UNKNOWN);
                Parameter isZone = new TruthValue(iZ);
                String det = iZ ? ip.zoneType().name() : ip.cellType().name();
                Parameter detail = new Identifier(det);
                Parameter pointX = new Numeral(ip.point().x + mapTopLeft.x);
                Parameter pointY = new Numeral(ip.point().y + mapTopLeft.y);
                // Generate Data for Point
                Parameter f = new Function("pointResult", detail, isZone, pointX, pointY, distance, direction);
                data.add(f);
            }
        }                
        Percept message = new Percept(TaskName.PATHFINDER_RESULT.name(), data);
        AgentLogger.info ("sendPathFindingResultToAgent() - Result: " + message + " , " + data);
        // Send Data to Agent
        mailService.sendMessage(message, agent, name);
        return message;
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
    
    @Override
    public void updateSupervisor(String supervisor) {
    	startCalculation(supervisor, maps.get(supervisor));
    }
}
