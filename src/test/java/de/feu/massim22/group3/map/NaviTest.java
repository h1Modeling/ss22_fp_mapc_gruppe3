package de.feu.massim22.group3.map;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import massim.protocol.data.NormInfo;
import massim.protocol.data.TaskInfo;
import massim.protocol.data.Thing;

import java.awt.Point;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class NaviTest {

    @BeforeEach
    void init() {
        Navi.<INaviTest>get().setDebug(false);
        Navi.<INaviTest>get().clear();
    }

    @Test
	void getBlankCellArrayTest() {
        CellType[][] cells = Navi.<INavi>get().getBlankCellArray(3);

        CellType[] row0 = {CellType.UNKNOWN, CellType.UNKNOWN, CellType.UNKNOWN, CellType.FREE, CellType.UNKNOWN, CellType.UNKNOWN, CellType.UNKNOWN };
        CellType[] row1 = {CellType.UNKNOWN, CellType.UNKNOWN, CellType.FREE, CellType.FREE, CellType.FREE, CellType.UNKNOWN, CellType.UNKNOWN };
        CellType[] row2 = {CellType.UNKNOWN, CellType.FREE, CellType.FREE, CellType.FREE, CellType.FREE, CellType.FREE, CellType.UNKNOWN };
        CellType[] row3 = {CellType.FREE, CellType.FREE, CellType.FREE, CellType.FREE, CellType.FREE, CellType.FREE, CellType.FREE };
        assertArrayEquals(row0, cells[0]);
        assertArrayEquals(row1, cells[1]);
        assertArrayEquals(row2, cells[2]);
        assertArrayEquals(row3, cells[3]);
        assertArrayEquals(row2, cells[4]);
        assertArrayEquals(row1, cells[5]);
        assertArrayEquals(row0, cells[6]);
	}

    @Test
    void pathFindingTestSimple() {
        /*  - - | - -
            - - | - -
            - A - - -   
            - - | G -
            - - | - -    */
        INaviAgentV1 navi = Navi.<INaviAgentV1>get();
        navi.registerAgent("A1", "A");

        Point position = new Point(0, 0);
        Set<Thing> things = new HashSet<>();
        things.add(new Thing(1, -2, Thing.TYPE_OBSTACLE, ""));
        things.add(new Thing(1, -1, Thing.TYPE_OBSTACLE, ""));
        things.add(new Thing(1, 1, Thing.TYPE_OBSTACLE, ""));
        things.add(new Thing(1, 2, Thing.TYPE_OBSTACLE, ""));

        List<Point> goalPoints = new ArrayList<>();
        goalPoints.add(new Point(2, 1));

        List<Point> rolePoints = new ArrayList<>();
    
        PathFindingResult[][] result = navi.updateMapAndPathfind("A1", "A1", 1, position, 3, things,
            goalPoints, rolePoints, 1, "A", 2, 0, new HashSet<NormInfo>(), new HashSet<TaskInfo>(), new ArrayList<Point>());
        navi.dispose();

        assertEquals(3, result[0][1].distance());
    }

    @Test
    void pathFindingTestSimpleWall() {
        /*  - - | - -
            - - | - -
            - A | - -   
            - - | G -
            - - | - -    */
        INaviAgentV1 navi = Navi.<INaviAgentV1>get();
        navi.registerAgent("a1", "A");

        Point position = new Point(0, 0);
        Set<Thing> things = new HashSet<>();
        things.add(new Thing(1, -2, Thing.TYPE_OBSTACLE, ""));
        things.add(new Thing(1, -1, Thing.TYPE_OBSTACLE, ""));
        things.add(new Thing(1, 0, Thing.TYPE_OBSTACLE, ""));
        things.add(new Thing(1, 1, Thing.TYPE_OBSTACLE, ""));
        things.add(new Thing(1, 2, Thing.TYPE_OBSTACLE, ""));

        List<Point> goalPoints = new ArrayList<>();
        goalPoints.add(new Point(2, 1));

        List<Point> rolePoints = new ArrayList<>();
    
        PathFindingResult[][] result = navi.updateMapAndPathfind("a1", "a1", 1, position, 3, things,
            goalPoints, rolePoints, 1, "a", 2, 0, new HashSet<NormInfo>(), new HashSet<TaskInfo>(), new ArrayList<Point>());
        navi.dispose();

        assertEquals(6, result[0][1].distance());
    }

    @Test
    void pathFindingTestAttached() {
        /*  - - | - -
            - # - - -
            - A - - -   
            - - | G -
            - - | - -    */
        INaviAgentV1 navi = Navi.<INaviAgentV1>get();
        navi.registerAgent("a1", "A");

        Point position = new Point(0, 0);
        Set<Thing> things = new HashSet<>();
        things.add(new Thing(1, -2, Thing.TYPE_OBSTACLE, ""));
        things.add(new Thing(1, 1, Thing.TYPE_OBSTACLE, ""));
        things.add(new Thing(1, 2, Thing.TYPE_OBSTACLE, ""));

        List<Point> goalPoints = new ArrayList<>();
        goalPoints.add(new Point(2, 1));

        List<Point> rolePoints = new ArrayList<>();

        List<Point> attached = new ArrayList<Point>();
        attached.add(new Point(0, -1));
    
        PathFindingResult[][] result = navi.updateMapAndPathfind("a1", "a1", 1, position, 3, things,
            goalPoints, rolePoints, 1, "a", 2, 0, new HashSet<NormInfo>(), new HashSet<TaskInfo>(), attached);
        navi.dispose();

        assertEquals(3, result[0][1].distance());
    }

    @Test
    void pathFindingTestAttached2() {
        /*  | G - | -
            | - - | -
            ---- ----   
            - - - A -
            - - - # -    */
        INaviAgentV1 navi = Navi.<INaviAgentV1>get();
        navi.registerAgent("a1", "A");

        Point position = new Point(0, 0);
        Set<Thing> things = new HashSet<>();
        things.add(new Thing(-3, -1, Thing.TYPE_OBSTACLE, ""));
        things.add(new Thing(-2, -1, Thing.TYPE_OBSTACLE, ""));
        things.add(new Thing(0, -1, Thing.TYPE_OBSTACLE, ""));
        things.add(new Thing(1, -1, Thing.TYPE_OBSTACLE, ""));
        things.add(new Thing(-3, -3, Thing.TYPE_OBSTACLE, ""));
        things.add(new Thing(-3, -2, Thing.TYPE_OBSTACLE, ""));
        things.add(new Thing(0, -3, Thing.TYPE_OBSTACLE, ""));
        things.add(new Thing(0, -2, Thing.TYPE_OBSTACLE, ""));

        List<Point> goalPoints = new ArrayList<>();
        goalPoints.add(new Point(-2, -3));

        List<Point> rolePoints = new ArrayList<>();

        List<Point> attached = new ArrayList<Point>();
        attached.add(new Point(0, 1));
    
        PathFindingResult[][] result = navi.updateMapAndPathfind("a1", "a1", 1, position, 5, things,
            goalPoints, rolePoints, 1, "a", 2, 0, new HashSet<NormInfo>(), new HashSet<TaskInfo>(), attached);
        navi.dispose();

        assertEquals(5, result[0][1].distance());
    }

    @Test
    void pathFindingTestAttachedFail() {
        /*  - - | - -
            - # | - -
            - A - - -   
            - - | G -
            - - | - -    */
        INaviAgentV1 navi = Navi.<INaviAgentV1>get();
        navi.registerAgent("a1", "A");

        Point position = new Point(0, 0);
        Set<Thing> things = new HashSet<>();
        things.add(new Thing(1, -2, Thing.TYPE_OBSTACLE, ""));
        things.add(new Thing(1, -1, Thing.TYPE_OBSTACLE, ""));
        things.add(new Thing(1, 1, Thing.TYPE_OBSTACLE, ""));
        things.add(new Thing(1, 2, Thing.TYPE_OBSTACLE, ""));

        List<Point> goalPoints = new ArrayList<>();
        goalPoints.add(new Point(2, 1));

        List<Point> rolePoints = new ArrayList<>();

        List<Point> attached = new ArrayList<Point>();
        attached.add(new Point(0, -1));
    
        PathFindingResult[][] result = navi.updateMapAndPathfind("a1", "a1", 1, position, 3, things,
            goalPoints, rolePoints, 1, "a", 2, 0, new HashSet<NormInfo>(), new HashSet<TaskInfo>(), attached);
        navi.dispose();

        assertEquals(3, result[0][1].distance());
    }
}
