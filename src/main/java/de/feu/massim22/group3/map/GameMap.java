package de.feu.massim22.group3.map;

import java.awt.Point;
import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Map.Entry;

import org.lwjgl.BufferUtils;

import de.feu.massim22.group3.utils.logging.AgentLogger;


public class GameMap {
    
    private Point initialSize;
    //private Point size = new Point(64, 92); // null;
    private Point size = new Point(32, 32); // null;
    // First dimension are rows, second dimension are columns
    private MapCell[][] cells;
    private Point topLeft; // top left indices can be negative
    private int mapExtensionSize = 20;
    private Map<String, Point> agentPosition = new HashMap<>(); 
    private Map<String, Integer> agentAttached = new HashMap<>();
    private Map<String, Integer> agentAttachedDistance = new HashMap<>(); 
    // These hold information relative to the internal array - only use for pathfinding
    private List<Point> goalCache = new ArrayList<>();
    private List<Point> roleCache = new ArrayList<>();
    private List<InterestingPoint> dispenserCache = new ArrayList<>();
    private List<Point> meetingPoints = null;
    
    public GameMap(int x, int y) {
        initialSize = size == null ? new Point(x, y) : size;
        topLeft = new Point((int)(-initialSize.x / 2), (int)(-initialSize.y / 2));
        cells = new MapCell[initialSize.y][initialSize.x];
        
        // Initialize with empty cells
        for (int i = 0; i < initialSize.y; i++) {
            for (int j = 0; j < initialSize.x; j++) {
                cells[i][j] = new MapCell();
            }
        }
    }

    List<Point> getGoalCache() {
        return goalCache;
    }

    List<Point> getRoleCache() {
        return roleCache;
    }

    public boolean mapDiscovered() {
        return size != null;
    }
    
    //Melinda
    public Point getAgentPosition(String name) {
        return agentPosition.get(name);
    }

    public Point getInternalAgentPosition(String agent) {
        Point agentPos = agentPosition.get(agent);
        AgentLogger.info(Thread.currentThread().getName() + " getInternalAgentPosition() - agent: " + agent + " , agentPos: " + agentPos + " , cellIndex: " + getInternalCellIndex(agentPos.x, agentPos.y));
        return agentPos == null ? new Point(0, 0) : getInternalCellIndex(agentPos.x, agentPos.y);
    }

    public int getAgentIdAtPoint(Point p) {
        for (Entry<String, Point> e : agentPosition.entrySet()) {
            if (e.getValue().equals(p)) {
                String agent = e.getKey();
                // TODO change substring to teamname length
                return Integer.parseInt(agent.substring(1));
            }
        }
        return 0;
    }

    public boolean isBlockAttached(Point p) {
        for (Entry<String, Point> entry : agentPosition.entrySet()) {
            Point n = new Point(p.x, p.y - 1);
            Point e = new Point(p.x + 1, p.y);
            Point s = new Point(p.x, p.y + 1);
            Point w = new Point(p.x - 1, p.y);
            String agent = entry.getKey();
            int attached = agentAttached.get(agent);
            if (entry.getValue().equals(n)) {
                boolean isAttached = attachedAtRelativePoint(new Point(0, 1), attached);
                if (isAttached) {
                    return true;
                }
            }
            if (entry.getValue().equals(s)) {
                boolean isAttached = attachedAtRelativePoint(new Point(0, -1), attached);
                if (isAttached) {
                    return true;
                }
            }
            if (entry.getValue().equals(e)) {
                boolean isAttached = attachedAtRelativePoint(new Point(-1, 0), attached);
                if (isAttached) {
                    return true;
                }
            }
            if (entry.getValue().equals(w)) {
                boolean isAttached = attachedAtRelativePoint(new Point(1, 0), attached);
                if (isAttached) {
                    return true;
                }
            }
        }
        return false;
    }

    boolean attachedAtRelativePoint(Point p, int value) {
        return ((value >> ((p.y + 2) * 5 + p.x + 2)) & 0x1) == 0x1;
    }

    public Point getInternalCellIndex(int x, int y) {
        int cellX = getCellX(x);
        int cellY = getCellY(y);
        return new Point(cellX, cellY);
    }

    CellType[][] getDebugCells() {
        int ySize = cells.length;
        int xSize = cells[0].length;
        CellType[][] types = new CellType[ySize][xSize];
        for (int y = 0; y < ySize; y++) {
            for (int x = 0; x < xSize; x++) {
                types[y][x] = cells[y][x].getCellType();
            }
        }
        return types;
    }
    
    public void addReport(int x, int y, CellType cellType, ZoneType zoneType, int agentId, int step) {		
        // Check if Array is big enough
        checkBounds(x, y);
        int cellX = getCellX(x);
        int cellY = getCellY(y);
        MapCellReport report = new MapCellReport(cellType, zoneType, agentId, step);
        if (cellY >= 0 && cellY < cells.length && cellX >= 0 && cellX < cells[0].length) {
            CellType current = cells[cellY][cellX].getCellType();
            // Dispenser don't change during sim and can be overwritten by blocks
            if (current != CellType.DISPENSER_0 && current != CellType.DISPENSER_1 && current != CellType.DISPENSER_2 && current != CellType.DISPENSER_3 && current != CellType.DISPENSER_4) {
                cells[cellY][cellX].addReport(report);
            }
        }
    }

    public void setAgentPosition(String name, Point position) {
        agentPosition.put(name, position);
    }

    public List<Point> getMeetingPoints() {
        if (meetingPoints == null) {
            calcMeetingPoints();
        }
        return meetingPoints;
    }

    public void calcMeetingPoints() {
        meetingPoints = new ArrayList<>();
        for (int y = 2; y < cells.length - 2; y++) {
            for (int x = 2; x < cells[0].length - 2; x++) {
                if (meetingPoints.size() > 9) {
                    return;
                }
                MapCell c = cells[y][x];
                if (c.getZoneType() == ZoneType.GOALZONE) {
                    boolean found = true;
                    for (int oy = -2; oy <= 2; oy++) {
                        for (int ox = -2; ox <= 2; ox++) {
                            MapCell cell = cells[y+oy][x+ox];
                            if (cell.getCellType() == CellType.OBSTACLE) {
                                found = false;
                            }
                        }
                    }
                    if (found) {
                        meetingPoints.add(new Point(x + topLeft.x, y + topLeft.y));
                    }
                }
            }
        }
    }

    public void setAgentAttached(String name, List<Point> attachedThings) {
        int result = 0; //4096; // Agent Pos
        int dist = 0;
        for (Point p : attachedThings) {
            if (p.y == -2) {
                if (p.x == -2) result += 1;
                if (p.x == -1) result += 2;
                if (p.x == 0) result += 4;
                if (p.x == 1) result += 8;
                if (p.x == 2) result += 16;
            } else if (p.y == -1) {
                if (p.x == -2) result += 32;
                if (p.x == -1) result += 64;
                if (p.x == 0) result += 128;
                if (p.x == 1) result += 256;
                if (p.x == 2) result += 512;
            } else if (p.y == 0) {
                if (p.x == -2) result += 1024;
                if (p.x == -1) result += 2048;
                // Agent Pos 
                if (p.x == 1) result += 8192;
                if (p.x == 2) result += 16384;
            } else if (p.y == 1) {
                if (p.x == -2) result += 32768;
                if (p.x == -1) result += 65536;
                if (p.x == 0) result += 131072;
                if (p.x == 1) result += 262144;
                if (p.x == 2) result += 524288;
            } else if (p.y == 2) {
                if (p.x == -2) result += 1048576;
                if (p.x == -1) result += 2097152;
                if (p.x == 0) result += 4194304;
                if (p.x == 1) result += 8388608;
                if (p.x == 2) result += 16777216;
            }
            // Calculate biggest distance
            dist = Math.max(dist, Math.max(Math.abs(p.x), Math.abs(p.y)));
        }
        agentAttached.put(name, result);
        agentAttachedDistance.put(name, dist);
    }

    public int getAgentAttached(String agent) {
        Integer result = agentAttached.get(agent);
        return result == null ? 0 : result;
    }

    public int getAgentAttachedDistance(String agent) {
        Integer result = agentAttachedDistance.get(agent);
        return result == null ? 0 : result;
    }
    
    public Point getTopLeft() {
        return topLeft;
    }

    Map<Point, String> getDebugAgentPosition() {
        Map<Point, String> result = new HashMap<>();
        for (Entry<String, Point> e : agentPosition.entrySet()) {
            Point agentPoint = e.getValue();
            Point internalPoint = getInternalCellIndex(agentPoint.x, agentPoint.y);
            result.put(internalPoint, e.getKey());
        }
        return result;
    }
    
    public Point getBottomRight() {
        Point p = getTopLeft();
        Point s = size == null ? initialSize : size;
        return new Point(p.x + s.x, p.y + s.y);
    }
    
    public Point getOrigin() {
        return new Point(-topLeft.x, -topLeft.y);
    }
    
    public void setFinalSize(int x, int y) {
        size = new Point(x, y);
        updateMapToFinalSize();
    }
    
    private void updateMapToFinalSize() {
        MapCell[][] newCells = new MapCell[size.y][size.x];
        // Initialize with empty cells
        for (int i = 0; i < size.y; i++) {
            for (int j = 0; j < size.x; j++) {
                newCells[i][j] = new MapCell();
            }
        }

        // Merge existing cells
        for (int i = 0; i < initialSize.y; i++) {
            for (int j = 0; j < initialSize.x; j++) {
                newCells[i % size.y][j % size.x].mergeIntoCell(cells[i][j]);
            }
        }
        cells = newCells;
    }
    
    // Points are relative to the origin of their map
    public Point mergeIntoMap(GameMap foreignMap, Point foreignPoint, Point thisPoint) {
        int offsetX = thisPoint.x - foreignPoint.x;
        int offsetY = thisPoint.y - foreignPoint.y;
        
        // MapSize not yet discovered
        if (size == null) {
            
            // Get Translated Top Left of foreign map
            Point foreignTopLeft = foreignMap.getTopLeft();
            foreignTopLeft.translate(offsetX, offsetY);
            Point foreignBottomRight = foreignMap.getBottomRight();
            Point bottomRight = getBottomRight();

            Point newTopLeft = new Point(Math.min(foreignTopLeft.x, topLeft.x), Math.min(foreignTopLeft.y, topLeft.y));
            int sizeX = Math.max(foreignBottomRight.x, bottomRight.x) - newTopLeft.x;
            int sizeY = Math.max(foreignBottomRight.y, bottomRight.y) - newTopLeft.y;
            
            MapCell[][] merge = new MapCell[sizeY][sizeX];
            
            // Initialize with empty cells
            for (int i = 0; i < sizeY; i++) {
                for (int j = 0; j < sizeX; j++) {
                    merge[i][j] = new MapCell();
                }
            }
            
            // Copy current Map
            int offsetXCells = topLeft.x - newTopLeft.x;
            int offsetYCells = topLeft.y - newTopLeft.y;
            for (int y = 0; y < initialSize.y; y++) {
                for (int x = 0; x < initialSize.x; x++) {
                    merge[y + offsetYCells][x + offsetXCells] = cells[y][x];
                }
            }
            
            // Copy foreign Map
            offsetXCells = foreignTopLeft.x - newTopLeft.x;
            offsetYCells = foreignTopLeft.y - newTopLeft.y;
            for (int y = 0; y < foreignMap.initialSize.y; y++) {
                for (int x = 0; x < foreignMap.initialSize.x; x++) {
                    MapCell f = foreignMap.cells[y][x];
                    merge[y + offsetYCells][x + offsetXCells].mergeIntoCell(f);
                }
            }
            
            // update cells
            cells = merge;
            initialSize = new Point(sizeX, sizeY);
            topLeft = newTopLeft;
        }

        // Map size already discovered
        else {
            int originOffsetX = foreignMap.getTopLeft().x - topLeft.x;
            int originOffsetY = foreignMap.getTopLeft().y - topLeft.y;
            for (int y = 0; y < size.y; y++) {
                for (int x = 0; x < size.x; x++) {
                    MapCell foreignCell = foreignMap.cells[y][x];
                    MapCell myCell = cells[(y + offsetY + originOffsetY + 2 * size.y) % size.y][(x + offsetX + originOffsetX + 2 * size.x) % size.x];
                    myCell.mergeIntoCell(foreignCell);
                }
            }
        }

        // Offset for agents of foreign map
        return new Point(offsetX, offsetY);
    }
    
    public CellType getCellType(int x, int y) {
        MapCell cell = getCell(x, y);
        return cell.getCellType();
    }
    
    public FloatBuffer getMapBuffer() {
        Point curSize = getMapSize();
        FloatBuffer data = BufferUtils.createFloatBuffer(curSize.x * curSize.y * 2);
        
        roleCache.clear();
        goalCache.clear();
        dispenserCache.clear();
        calcMeetingPoints();
        
        for (int y = 0; y < curSize.y; y++) {
            for (int x = 0; x < curSize.x; x++) {
                MapCell c = cells[y][x];
                CellType type = c.getCellType();
                
                float v = type.equals(CellType.OBSTACLE) || type.equals(CellType.UNKNOWN) ? 1f : 0f;
                // r-channel
                data.put(v);
                // g-channel not used in map
                data.put(0.0f);
                
                // update Cache
                Point p = new Point(x, y);
                if (c.getCellType() != CellType.OBSTACLE) {
                    if (c.getZoneType() == ZoneType.ROLEZONE) {
                        roleCache.add(p);
                    } else if (c.getZoneType() == ZoneType.GOALZONE) {
                        goalCache.add(p);
                    }
                }
                if (c.getCellType().name().contains("DISPENSER")) {
                   // AgentLogger.info(Thread.currentThread().getName() + " getMapBuffer: " + x + " , " + y + " , " + c.getCellType().name() + " , ");

                    Point size = getMapSize();
                    // Get neighbour cells
                    // North
                    Point north = new Point(p.x, p.y - 1);
                    if (north.y >= 0 && cells[north.y][north.x].getCellType() == CellType.FREE) {
                        InterestingPoint ip = new InterestingPoint(north, ZoneType.NONE, c.getCellType(), "n");
                        dispenserCache.add(ip);
                    }
                    // East
                    Point east = new Point(p.x + 1, p.y);
                    if (east.x < size.x && cells[east.y][east.x].getCellType() == CellType.FREE) {
                        InterestingPoint ip = new InterestingPoint(east, ZoneType.NONE, c.getCellType(), "e");
                        dispenserCache.add(ip);
                    }
                    // South
                    Point south = new Point(p.x, p.y + 1);
                    if (south.y < size.y && cells[south.y][south.x].getCellType() == CellType.FREE) {
                        InterestingPoint ip = new InterestingPoint(south, ZoneType.NONE, c.getCellType(), "s");
                        dispenserCache.add(ip);
                    }
                    // West
                    Point west = new Point(p.x - 1, p.y);
                    if (west.x >= 0 && cells[west.y][west.x].getCellType() == CellType.FREE) {
                        InterestingPoint ip = new InterestingPoint(west, ZoneType.NONE, c.getCellType(), "w");
                        dispenserCache.add(ip);
                    }                   
                    // Dispenser
                    InterestingPoint ip = new InterestingPoint(p, ZoneType.NONE, c.getCellType(), "x");
                    dispenserCache.add(ip);
                }
            }
        }

        data.flip();
        return data;
    }

    String getDirectionToNearestUndiscoveredPoint(String agent) {
        Point agentPos = getInternalAgentPosition(agent);
        int step = 1;
        while (step < Math.max(cells.length, cells[0].length)) {
            int top = Math.max(0, -step + agentPos.y);
            int left = Math.max(0, -step + agentPos.x);
            int bottom = Math.min(cells.length - 1, step + agentPos.y);
            int right = Math.min(cells[0].length - 1, step + agentPos.x);

            List<String> possibleDirs = new ArrayList<>();
            List<String> possibleDirsFree = new ArrayList<>();

            if (agentPos.x < 0 || agentPos.y < 0 || agentPos.x >= cells[0].length || agentPos.y >= cells.length) {
                break;
            }

            if (cells[top][agentPos.x].getCellType() == CellType.UNKNOWN && agentPos.y > 5) {
                possibleDirs.add("n");
                if (cells[agentPos.y - 1][agentPos.x].getCellType() != CellType.OBSTACLE) {
                    possibleDirsFree.add("n");
                }
            }
            if (cells[bottom][agentPos.x].getCellType() == CellType.UNKNOWN && agentPos.y < cells.length - 6) {
                possibleDirs.add("s");
                if (cells[agentPos.y + 1][agentPos.x].getCellType() != CellType.OBSTACLE) {
                    possibleDirsFree.add("s");
                }
            }
            if (cells[agentPos.y][left].getCellType() == CellType.UNKNOWN && agentPos.x > 5) {
                possibleDirs.add("w");
                if (cells[agentPos.y][agentPos.x - 1].getCellType() != CellType.OBSTACLE) {
                    possibleDirsFree.add("w");
                }
            }
            
            if (cells[agentPos.y][right].getCellType() == CellType.UNKNOWN && agentPos.x < cells[0].length - 6) {
                possibleDirs.add("e");
                if (cells[agentPos.y][agentPos.x + 1].getCellType() != CellType.OBSTACLE) {
                    possibleDirsFree.add("e");
                }
            }

            // First try directions without obstacles
            if (possibleDirsFree.size() > 0) {
                int index = (int)Math.floor(Math.random() * possibleDirsFree.size());
                return possibleDirsFree.get(index);
            }

            if (possibleDirs.size() > 0) {
                int index = (int)Math.floor(Math.random() * possibleDirs.size());
                return possibleDirs.get(index);
            }
            step++;
        }
        float random = new Random().nextFloat();
        if (random < 0.25) {
            return "n";
        } else if (random < 0.5) {
            return "e";
        } else if (random < 0.75) {
            return "w";
        } else {
            return "s";
        }
    }

    public List<InterestingPoint> getInterestingPoints(int maxCount, boolean useRoleZones) {
        List<InterestingPoint> result = new ArrayList<>();
        // Dispensers
        result.addAll(dispenserCache);
        int countLeft = maxCount; //- dispenserCache.size();
        
        // Agents
        for (Entry<String, Point> e : agentPosition.entrySet()) {
            Point p = e.getValue();
            Point internal = new Point(p.x - topLeft.x, p.y - topLeft.y);
            InterestingPoint ip = new InterestingPoint(internal, ZoneType.NONE, CellType.TEAMMATE, e.getKey());
            result.add(ip);
        }

        // Meeting Points in goal Zone
        /*
        for (Point p : meetingPoints) {
            if (countLeft == 0) {
                break;
            }
            Point internal = new Point(p.x - topLeft.x, p.y - topLeft.y);
            InterestingPoint ip = new InterestingPoint(internal, ZoneType.GOALZONE, CellType.UNKNOWN, "");
            result.add(ip);
            countLeft -= 1;
        }*/
        
        // Goal and Role Zones 
        List<List<Point>> goalLists = filterZones(goalCache, 6);
        List<List<Point>> roleLists = new ArrayList<>();
        if (useRoleZones) {
            roleLists = filterZones(roleCache, 6);
        }
        
        // Alternate between goal and role
        while (countLeft > 0 && (goalLists.size() != 0 || roleLists.size() != 0)) {
            int goalListSize = goalLists.size();
            int roleListSize = roleLists.size();
            int maxSize = Math.max(goalListSize, roleListSize);
            
            for (int i = 0; i < maxSize; i++) {
                int goalFactor = 2;
                if (i < goalListSize) {
                    List<Point> goalList = goalLists.get(i);
                    if (goalList.size() > 0) {
                        Point p = goalList.get(0);
                        InterestingPoint ip = new InterestingPoint(p, ZoneType.GOALZONE, CellType.UNKNOWN, "");
                        result.add(ip);
                        countLeft -= 1;
                        for (int k = 0; k < goalFactor; k++) {
                            if (goalList.size() > 0) {
                                goalList.remove(0);
                            }
                        }

                        // Remove Empty List
                        if (goalList.size() == 0) {
                            goalLists.remove(goalList);
                            goalListSize = goalLists.size();
                        }
                    }
                }
                int roleFactor = 12;
                if (i < roleListSize && useRoleZones) {
                    List<Point> roleList = roleLists.get(i);
                    if (roleList.size() > 0) {
                        Point p = roleList.get(0);
                        InterestingPoint ip = new InterestingPoint(p, ZoneType.ROLEZONE, CellType.UNKNOWN, "");
                        result.add(ip);
                        countLeft -= 1;
                        for (int k = 0; k < roleFactor; k++) {
                            if (roleList.size() > 0) {
                                roleList.remove(0);
                            }
                        }
                        // Remove Empty List
                        if (roleList.size() == 0) {
                            roleLists.remove(roleList);
                            roleListSize = roleLists.size();
                        }
                    }
                }
            }
        }
        return result;
    }

    // creates different lists of close points
    private List<List<Point>> filterZones(List<Point> zonePoints, int distinctionDistance) {
        List<List<Point>> result = new LinkedList<>();
        for (Point p : zonePoints) {
            boolean added = false;
            for (List<Point> list : result) {
                Point first = list.get(0);
                if (Math.abs(first.x - p.x) <= distinctionDistance) {
                    list.add(p);
                    added = true;
                    break;
                }
            }
            if (!added) {
                List<Point> newList = new LinkedList<>();
                newList.add(p);
                result.add(newList);
            }
        }
        return result;
    }

    public Point getMapSize() {
        return size == null ? initialSize : size;
    }

    public FloatBuffer getEmptyBuffer() {
        Point curSize = size == null ? initialSize : size;
        FloatBuffer data = BufferUtils.createFloatBuffer(curSize.x * curSize.y * 2);
        
        for (int i = 0; i < curSize.y; i++) {
            for (int j = 0; j < curSize.x; j++) {
                data.put(0.0f);
                data.put(0.0f);
            }
        }

        data.flip();
        return data;
    }

    boolean isAgentInGroupAtPosition(Point p) {
        return agentPosition.containsValue(p);
    }

    private MapCell getCell(int x, int y) {
        return cells[getCellY(y)][getCellX(x)];
    }
    
    private int getCellX(int x) {
        return size == null ? x - topLeft.x : (x - topLeft.x) % size.x;
    }
    
    private int getCellY(int y) {
        return size == null ? y - topLeft.y : (y - topLeft.y) % size.y;
    }
    
    private void checkBounds(int x, int y) {
        // Double Array Size if size is not enough
        boolean extendLeft = x < topLeft.x;
        boolean extendRight = x >= initialSize.x + topLeft.x;
        boolean extendHorizontal = extendLeft || extendRight;
        boolean extendTop = y < topLeft.y;
        boolean extendBottom = y >= initialSize.y + topLeft.y;
        boolean extendVertical = extendBottom || extendTop;

        if (size == null && (extendHorizontal || extendVertical)) {
            Point newInitialSize;
            Point offset;
            // Extend to right
            if (extendRight) {
                newInitialSize = new Point(initialSize.x + mapExtensionSize, initialSize.y);
                offset = new Point(0, 0);
            }
            // Extend to left
            else if (extendLeft) {
                newInitialSize = new Point(initialSize.x + mapExtensionSize, initialSize.y);
                offset = new Point(mapExtensionSize, 0);				
            }
            // Extend to top
            else if (extendTop) {
                newInitialSize = new Point(initialSize.x, initialSize.y + mapExtensionSize);
                offset = new Point(0, mapExtensionSize);				
            }
            // Extend to bottom
            else {
                newInitialSize = new Point(initialSize.x, initialSize.y + mapExtensionSize);
                offset = new Point(0, 0);				
            }

            MapCell[][] newCells = new MapCell[newInitialSize.y][newInitialSize.x];		
            
            // Copy old values
            for (int i = 0; i < initialSize.y; i++) {
                for (int j = 0; j < initialSize.x; j++) {
                    newCells[i + offset.y][j + offset.x] = cells[i][j];
                }
            }
            // Fill rest with unknowns
            if (extendHorizontal) {
                int startX = (initialSize.x + offset.x) % newInitialSize.x;
                for (int i = 0; i < newInitialSize.y; i++) {
                    for (int j = 0; j < newInitialSize.x - initialSize.x; j++) {
                        newCells[i][startX + j] = new MapCell();
                    }
                }				
            } else {
                int startY = (initialSize.y + offset.y) % newInitialSize.y;
                for (int i = 0; i < newInitialSize.y - initialSize.y; i++) {
                    for (int j = 0; j < newInitialSize.x; j++) {
                        newCells[i + startY][j] = new MapCell();
                    }
                }
            }

            initialSize = newInitialSize;
            cells = newCells;
            topLeft = new Point(topLeft.x - offset.x, topLeft.y - offset.y);
        }
    }
}
