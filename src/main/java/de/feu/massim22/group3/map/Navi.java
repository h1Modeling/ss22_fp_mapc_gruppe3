package de.feu.massim22.group3.map;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import javax.swing.SwingUtilities;

import org.lwjgl.BufferUtils;

import de.feu.massim22.group3.EventName;
import de.feu.massim22.group3.MailService;
import de.feu.massim22.group3.utils.Convert;
import de.feu.massim22.group3.utils.debugger.DebugStepListener;
import de.feu.massim22.group3.utils.debugger.GraphicalDebugger;
import de.feu.massim22.group3.utils.debugger.IGraphicalDebugger;
import de.feu.massim22.group3.utils.debugger.GraphicalDebugger.AgentDebugData;
import de.feu.massim22.group3.utils.debugger.GraphicalDebugger.GroupDebugData;
import de.feu.massim22.group3.utils.logging.AgentLogger;
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
    private Map<String, String> agentSupervisor = new HashMap<>(); // Agent Key, Supervisor Value
    private Map<String, Integer> agentStep = new HashMap<>();
    private Map<String, List<AgentGreet>> supervisorGreetData = new HashMap<>();
    private Map<String, Long> openGlHandler = new HashMap<>();
    private IGraphicalDebugger debugger = new GraphicalDebugger();
    private Map<String, Map<String, MergeReply>> mergeKeys = new HashMap<>();
    private boolean busy = false;
    
    private Navi() {
        PathFinder.init();

        // Open Debugger
        SwingUtilities.invokeLater((Runnable)debugger);

        // TODO At end of application PathFinder must be closed to free resources
    }

    @SuppressWarnings("unchecked")
    public static synchronized <T extends INavi> T get() {
        if (Navi.instance == null) {
            instance = new Navi();
        }
        return (T) (Navi.instance);
    }

    @Override
    public boolean isWaitingOrBusy() {
        return mergeKeys.size() > 0 || busy;
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

    public void updateAgentDebugData(String agent, String supervisor, String role, int energy, String lastAction, String lastActionSuccess) {
        AgentDebugData data = new AgentDebugData(agent, supervisor, role, energy, lastAction, lastActionSuccess);
        debugger.setAgentData(data);
    }
    
    @Override
    public synchronized void updateMapAndPathfind(String supervisor, String agent, int agentIndex, Point position, int vision,
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
            filterKnownTeamMatesFromGreetData(supervisor);
            // Check if all Supervisors have sent
            if (haveAllAgentsSentData(step)) {
                makeMergeSuggestions();
            }
            GameMap map = maps.get(supervisor);
            startCalculation(supervisor, map);
        }
    }

    @Override
    public synchronized void updateMap(String supervisor, String agent, int agentIndex, Point position, int vision, Set<Thing> things, List<Point> goalPoints, List<Point> rolePoints, int step, String team, int maxSteps, int score, Set<NormInfo> normsInfo, 
            Set<TaskInfo> taskInfo) {

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
                boolean isTeamMate = t.details.equals(team);
                boolean isSelf = t.x == 0 && t.y == 0;
                type = isTeamMate ? CellType.TEAMMATE : CellType.ENEMY;
                // Save TeamMember discovery in supervisor data 
                if (isTeamMate && !isSelf) {
                    List<AgentGreet> data = supervisorGreetData.getOrDefault(supervisor, new ArrayList<>());
                    data.add(new AgentGreet(agent, supervisor, position, new Point(t.x, t.y)));
                    supervisorGreetData.put(supervisor, data);
                }
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

    private void makeMergeSuggestions() {
        for (List<AgentGreet> greets : supervisorGreetData.values()) {
            for (AgentGreet g : greets) {
                Point inverseOffset = new Point(-g.offset.x, -g.offset.y);
                List<AgentGreet> result = offsetInAgentGreetCount(inverseOffset);
                // Only make suggestion if count is exactly 1 to be safe
                if (result.size() == 1) {
                    String agent = g.name();
                    String supervisor = agentSupervisor.get(agent);
                    String foreignAgent = result.get(0).name();
                    String foreignSupervisor = agentSupervisor.get(foreignAgent);
                    String mergeKey = getMergeKey(supervisor, foreignSupervisor);
                    Parameter agentName = new Identifier(agent);
                    Parameter foreignSupervisorPara = new Identifier(foreignSupervisor);
                    Parameter offsetX = new Numeral(g.offset.x);
                    Parameter offsetY = new Numeral(g.offset.y);
                    Parameter mergeKeyPara = new Identifier(mergeKey);
                    Percept message = new Percept(EventName.MERGE_SUGGESTION.name(), agentName, foreignSupervisorPara, mergeKeyPara, offsetX, offsetY);
                    mailService.sendMessage(message, supervisor, name);

                    Map<String, MergeReply> entries = mergeKeys.getOrDefault(mergeKey, new HashMap<>());
                    if (!entries.containsKey(supervisor)) {
                        entries.put(supervisor, new MergeReply(g.offset));
                    }
                    mergeKeys.put(mergeKey, entries);
                }
            }
        }

        // Clear for next step
        supervisorGreetData.clear();
    }

    @Override
    public synchronized void rejectMerge(String mergeKey, String name) {
        AgentLogger.info(name + " rejected merge with key " + mergeKey);
        mergeKeys.remove(mergeKey);
    }

    @Override
    public synchronized void acceptMerge(String mergeKey, String name) {
        Map<String, MergeReply> map = mergeKeys.get(mergeKey);
        // Merge was rejected by other party
        if (map == null) { 
            return;
        }
        AgentLogger.info(name + " accepts merge with key " + mergeKey);
        MergeReply reply = map.get(name);
        reply.setReply();
        map.put(name, reply);
        
        // Check if all have agreed
        for (MergeReply r : map.values()) {
            if (!r.getReply() || map.size() < 2) {
                return;
            }
        }
        AgentLogger.info("Groupmerge with key " + mergeKey + " is starting");

        // Merge to Group
        String agents[] = map.keySet().toArray(new String[map.size()]);
        String a1 = agents[0];
        String a2 = agents[1];
        
        int id1 = Integer.parseInt(a1.substring(1));
        int id2 = Integer.parseInt(a2.substring(1));
        
        GameMap m1 = maps.get(a1);
        GameMap m2 = maps.get(a2);
        
        // Minimum Agent Id will be new Supervisor
        String newSupervisor = id1 < id2 ? a1 : a2;
        String oldSupervisor = id1 < id2 ? a2 : a1;
        GameMap newMap = id1 < id2 ? m1 : m2;
        GameMap oldMap = id1 < id2 ? m2 : m1; 

        Point offset = map.get(newSupervisor).getOffset();
        
        // Merge Map
        newMap.mergeIntoMap(oldMap, new Point(0,0), offset);

        maps.put(newSupervisor, newMap);
        
        // Update Agents in Navi
        for (Entry<String, String> entry : agentSupervisor.entrySet()) {
            if (entry.getValue().equals(oldSupervisor)) {
                // Update in Navi
                entry.setValue(newSupervisor);
                // Update Agent
                String agent = entry.getKey();
                Parameter supervisorPara = new Identifier(newSupervisor);
                
                Parameter posOffsetX = new Numeral(offset.x);
                Parameter posOffsetY = new Numeral(offset.y);
                Percept agentMessage = new Percept(EventName.UPDATE_GROUP.name(), supervisorPara, posOffsetX, posOffsetY);
                mailService.sendMessage(agentMessage, agent, name);
                
                // Update Supervisor
                Parameter agentPara = new Identifier(agent);
                Percept supervisorMessage = new Percept(EventName.ADD_GROUP_MEMBERS.name(), agentPara);
                mailService.sendMessage(supervisorMessage, newSupervisor, name);
            }
        }
        
        // Update Debugger
        debugger.removeSupervisor(oldSupervisor, newSupervisor);
        mergeKeys.remove(mergeKey);
    }

    private String getMergeKey(String supervisor1, String supervisor2) {
        int a = Integer.parseInt(supervisor1.substring(1));
        int b = Integer.parseInt(supervisor2.substring(1));
        int min = Math.min(a, b);
        int max = Math.max(a, b);
        return "Merge" + min + "-" + max;
    }

    private List<AgentGreet> offsetInAgentGreetCount(Point offset) {
        List<AgentGreet> result = new ArrayList<>();

        for (List<AgentGreet> greets : supervisorGreetData.values()) {
            for (AgentGreet g: greets) {
                if (g.offset().equals(offset)) {
                    result.add(g);
                }
            }
        }
        return result;
    }

    private boolean haveAllAgentsSentData(int step) {
        for (Entry<String, Integer> e : agentStep.entrySet()) {
            if (e.getValue() != step) {
                return false;
            }
        }
        return true;
    }

    private void filterKnownTeamMatesFromGreetData(String supervisor) {
        List<AgentGreet> data = supervisorGreetData.get(supervisor);
        if (data != null) {
            int i = 0;
            GameMap map = maps.get(supervisor);
            // Remove known agents
            while (i < data.size()) {
                AgentGreet greet = data.get(i);
                Point position = greet.agentPosition;
                Point offset = greet.offset;
                Point p = new Point(position.x + offset.x, position.y + offset.y);
                // Remove agent from list because he is a known teammate
                if (map.isAgentInGroupAtPosition(p)) {
                    data.remove(i);
                } else {
                    i++;
                }
            } 
        }
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

    private synchronized void startCalculation(String supervisor, GameMap map) {
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
                    sendPathFindingResultToAgent(agent, agentResultData, interestingPoints, mapTopLeft);
                }
            }
        }
    }

    private void sendPathFindingResultToAgent(String agent, PathFindingResult[] agentResultData, List<InterestingPoint> interestingPoints, Point mapTopLeft) {
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
        Percept message = new Percept(EventName.PATHFINDER_RESULT.name(), data);
        // Send Data to Agent
        mailService.sendMessage(message, agent, name);
    }

    private record AgentGreet(String name, String supervisor, Point agentPosition, Point offset) {}

    private class MergeReply {
        private boolean reply = false;
        private Point offset;

        MergeReply(Point offset) {
            this.offset = offset;
        }

        boolean getReply() {
            return reply;
        }

        void setReply() {
            this.reply = true;
        }

        Point getOffset() {
            return offset;
        }
    }
    
    @Override
    public synchronized void updateSupervisor(String supervisor) {
        startCalculation(supervisor, maps.get(supervisor));
    }
}
