package de.feu.massim22.group3.map;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.SwingUtilities;

import org.lwjgl.BufferUtils;

import de.feu.massim22.group3.MailService;
import de.feu.massim22.group3.TaskName;
import de.feu.massim22.group3.agents.CalcResult;
import de.feu.massim22.group3.utils.Convert;
import de.feu.massim22.group3.utils.debugger.DebugStepListener;
import de.feu.massim22.group3.utils.debugger.GraphicalDebugger;
import de.feu.massim22.group3.utils.debugger.IGraphicalDebugger;
import de.feu.massim22.group3.utils.debugger.GraphicalDebugger.AgentDebugData;
import de.feu.massim22.group3.utils.debugger.GraphicalDebugger.GroupDebugData;
import eis.iilang.Function;
import eis.iilang.Identifier;
import eis.iilang.Numeral;
import eis.iilang.Parameter;
import eis.iilang.TruthValue;
import eis.iilang.Percept;

import java.awt.Point;
import java.nio.FloatBuffer;

import massim.protocol.data.NormInfo;
import massim.protocol.data.TaskInfo;
import massim.protocol.data.Thing;

public class Navi implements INaviAgentV1, INaviAgentV2  {
    private static Navi instance;
    private String name = "Navi";
    private MailService mailService;
    private Map<String, GameMap> maps = new HashMap<>();
    private Map<String, String> agentSupervisor = new HashMap<>();
    private Map<String, Integer> agentStep = new HashMap<>();
    private Map<String, Long> openGlHandler = new HashMap<>();
    private IGraphicalDebugger debugger = new GraphicalDebugger();
    
    private Navi() {
        PathFinder.init();

        // Open Debugger
        SwingUtilities.invokeLater((Runnable)debugger);

        // TODO At end of application PathFinder must be closed to free resources
    }

    public static synchronized <T extends INavi> T get() {
        if (Navi.instance == null) {
            instance = new Navi();
        }
        return (T) (Navi.instance);
    }

    public void setDebugStepListener(DebugStepListener listener) {
        debugger.setDebugStepListener(listener);
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
 
    /*@Override
    public GameMap getMap(String supervisor) {
        return maps.get(supervisor);
    }*/
    
    @Override
	public List<InterestingPoint> getInterestingPoints(String supervisor, int maxNumberGoals) {
		GameMap map = maps.get(supervisor);
		return map.getInterestingPoints(maxNumberGoals);
	}
	
    @Override
	public Point getTopLeft(String supervisor) {
		return maps.get(supervisor).getTopLeft();
    }
    
    @Override
    public Point getInternalAgentPosition(String supervisor, String agent) {
    	return maps.get(supervisor).getInternalAgentPosition(agent);
    }
    
    @Override
    public FloatBuffer getMapBuffer(String supervisor) {
    	return maps.get(supervisor).getMapBuffer();
    }
    
    //Melinda Ende

    public void updateAgentDebugData(String agent, String supervisor, String role, int energy, String lastAction, String lastActionSuccess) {
        AgentDebugData data = new AgentDebugData(agent, supervisor, role, energy, lastAction, lastActionSuccess);
        debugger.setAgentData(data);
    }
    
    @Override
    public void updateMapAndPathfind(String supervisor, String agent, int agentIndex, Point position, int vision,
            Set<Thing> things, List<Point> goalPoints, List<Point> rolePoints, int step, String team, int maxSteps,
            int score, Set<NormInfo> normsInfo, Set<TaskInfo> taskInfo) {

        updateMap(supervisor, agent, agentIndex, position, vision, things, goalPoints, rolePoints, step, team, maxSteps,
                score, normsInfo, taskInfo);

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
    public void updateMap(String supervisor, String agent, int agentIndex, Point position, int vision, Set<Thing> things, List<Point> goalPoints, List<Point> rolePoints, int step, String team, int maxSteps, int score, Set<NormInfo> normsInfo, 
            Set<TaskInfo> taskInfo) {
    /*public void updateAgent(String supervisor, String agent, int agentIndex, Point position, int vision, Set<Thing> things,
        List<Point> goalPoints, List<Point> rolePoints, int step, String team, int maxSteps, int score, Set<NormInfo> normsInfo, 
        Set<TaskInfo> taskInfo) {*/

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
            CellType type;
            if (t.type.equals(Thing.TYPE_ENTITY)) {
                type = t.details.equals(team) ? CellType.TEAMMATE : CellType.ENEMY;
            } else {
                type = Convert.thingToCellType(t);
            }
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

        // Update Debugger simulation data
        debugger.setSimInfo(step, maxSteps, score);

        // Update Debugger Norms
        debugger.setNorms(normsInfo, step);

        // Update Debugger Tasks
        debugger.setTasks(taskInfo, step);
    }

    // Creates array with CellType.FREE in vision and CellType.UNKNOWN outside of vision
    @Override
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

    private synchronized List<CalcResult> startCalculation(String supervisor, GameMap map) {
        List<CalcResult> calcResults = new ArrayList<>();
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
        
        if (numberGoals > 0) {
            // Start Path Finding
            PathFindingResult[][] result = finder.start(mapTextureBuffer, dataTextureBuffer, interestingPoints, mapSize, dataSize, agentSize, numberGoals, mapDiscovered, supervisor, step);

            // Update Debugger
            CellType[][] cells = map.getDebugCells();
            Point topLeft = map.getTopLeft();
            Map<Point, String> agentPosition = map.getDebugAgentPosition();
            List<Point> roleZones = map.getRoleCache();
            List<Point> goalZones = map.getGoalCache();

            // Update debugger
            GroupDebugData debugData = new GroupDebugData(supervisor, cells, topLeft, interestingPoints, result, agentPosition, roleZones, goalZones, agents);
            debugger.setGroupData(debugData);
            
            // Send Result to Agents
            if (mailService != null) {
                for (int i = 0; i < agents.size(); i++) {
                    String agent = agents.get(i);
                    PathFindingResult[] agentResultData = result[i];
                    Point mapTopLeft = map.getTopLeft();
                    calcResult.add(sendPathFindingResultToAgent(agent, agentResultData, interestingPoints, mapTopLeft));
                }
            }
        }
        return calcResult;
    }

    private Percept sendPathFindingResultToAgent(String agent, PathFindingResult[] agentResultData, List<InterestingPoint> interestingPoints, Point mapTopLeft) {
        List<Parameter> data = new ArrayList<>();
        // Generate Percept
        for (int j = 0; j < interestingPoints.size(); j++) {
            PathFindingResult resultData = agentResultData[j];
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
        // Send Data to Agent
        mailService.sendMessage(message, agent, name);       
        return message;
    }
    
    @Override
    public List<CalcResult> updateSupervisor(String supervisor) {
        return startCalculation(supervisor, maps.get(supervisor));
    }
}
