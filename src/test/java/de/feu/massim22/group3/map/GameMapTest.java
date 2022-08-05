package de.feu.massim22.group3.map;

import static org.junit.jupiter.api.Assertions.*;

import java.awt.Point;
import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

/**
 * The Class <code>GameMapTest</code> provides methods for testing the class <code>GameMap</code>.
 * 
 * @see GameMap
 * @author Heinz Stadler
 */
class GameMapTest {
    
    private GameMap map = new GameMap(3, 5, "A");

    /**
     * Tests a map size increase to the left side resulting from a report which didn't fit into the map.
     */
    @Test
    void testMapExtensionLeft() {
        map.addReport(-2, 0, CellType.FREE, ZoneType.NONE, 0, 0);
        assertEquals(-21, map.getTopLeft().x);
        assertEquals(CellType.FREE, map.getCellType(-2, 0));
    }
    
    /**
     * Tests a map size increase to the right side resulting from a report which didn't fit into the map.
     */
    @Test
    void testMapExtensionRight() {
        map.addReport(4, 0, CellType.FREE, ZoneType.NONE, 0, 0);
        assertEquals(-1, map.getTopLeft().x);
        assertEquals(CellType.FREE, map.getCellType(4, 0));
    }
    
    /**
     * Tests a map size increase to the top side resulting from a report which didn't fit into the map.
     */
    @Test
    void testMapExtensionTop() {
        map.addReport(0, -5, CellType.FREE, ZoneType.NONE, 0, 0);
        assertEquals(-22, map.getTopLeft().y);
        assertEquals(CellType.FREE, map.getCellType(0, -5));
    }

    /**
     * Tests a map size increase to the bottom side resulting from a report which didn't fit into the map.
     */
    @Test
    void testMapExtensionBottom() {
        map.addReport(0, 5, CellType.FREE, ZoneType.NONE, 0, 0);
        assertEquals(-2, map.getTopLeft().y);
        assertEquals(CellType.FREE, map.getCellType(0, 5));
    }

    /**
     * Tests if a report is correctly saved into the map.
     */
    @Test
    void testAddReport() {
        assertEquals(-1, map.getTopLeft().x);
        assertEquals(-2, map.getTopLeft().y);
        map.addReport(-1, 0, CellType.FREE, ZoneType.NONE, 0, 0);
        assertEquals(CellType.FREE, map.getCellType(-1, 0));
    }
    
    /**
     * Tests if two maps get correctly merged if the game map size isn't discovered yet.
     * @throws InterruptedException
     */
    @Test
    void testMergeIntoMapUndiscovered() throws InterruptedException {
        map.addReport(1, 0, CellType.FREE, ZoneType.NONE, 0, 0);
        GameMap toMerge = new GameMap(5, 10, "A");
        Thread.sleep(1);
        toMerge.addReport(1, 0, CellType.DISPENSER_0, ZoneType.NONE, 1, 1);
        Point offset = map.mergeIntoMap(toMerge, new Point(2, 1), new Point(4, -1));
        assertEquals(CellType.FREE, map.getCellType(1, 0));
        assertEquals(CellType.DISPENSER_0, map.getCellType(1 + offset.x, 0 + offset.y));
    }

    /**
     * Tests if two maps get correctly merged if the game map size is already discovered.
     * @throws InterruptedException
     */
    @Test
    void testMergeIntoMapDiscovered() throws InterruptedException {
        map.addReport(1, 0, CellType.FREE, ZoneType.NONE, 0, 0);
        Thread.sleep(1);
        map.setFinalSize(10, 10);
        GameMap toMerge = new GameMap(10, 10, "A");
        Thread.sleep(1);
        toMerge.addReport(1, 1, CellType.DISPENSER_0, ZoneType.NONE, 1, 1);
        //toMerge.setFinalSize(10, 10);
        Thread.sleep(1);
        Point offset = map.mergeIntoMap(toMerge, new Point(2, 1), new Point(4, -1));
        assertEquals(CellType.FREE, map.getCellType(1, 0));
        assertEquals(CellType.DISPENSER_0, map.getCellType(1 + offset.x, 1 + offset.y));
    }

    /**
     * Tests if saved cells are still contained in the map after the map size is set.
     */
    @Test
    void testSetFinalSize() {
        map.addReport(1, 3, CellType.FREE, ZoneType.NONE, 1, 0);
        map.setFinalSize(10, 10);
        assertEquals(CellType.FREE, map.getCellType(1, 3));
    }

    /**
     * Tests if the map buffer gests correctly exported.
     */
    @Test
    void testGetMapBuffer() {
        GameMap m = new GameMap(3, 3, "A");
        m.addReport(0, 0, CellType.FREE, ZoneType.NONE, 0, 1);
        FloatBuffer result = m.getMapBuffer();
        float[] actual = new float[18];
        result.get(actual);
        float[] expected = {1, 0, 1, 0, 1, 0, 1, 0, 0, 0, 1, 0, 1, 0, 1, 0, 1, 0};
        assertArrayEquals(expected, actual);
    }

    /**
     * Tests if the agent attached code gets correctly saved into the map.
     */
    @Test
    void setAgentAttached() {
        GameMap m = new GameMap(3, 3, "A");
        List<Point> points = new ArrayList<>();
        for (int y = -2; y < 3; y++) {
            for (int x = -2; x < 3; x++) {
                points.add(new Point(x, y));
            }
        }
        m.setAgentAttached("A1", points);
        int result = m.getAgentAttached("A1");
        assertEquals(Math.pow(2, 25) - 4097, result);
    }

    /**
     * Tests if the agent attached code contains the right attached values.
     */
    @Test
    void attachedAtRelativePoint() {
        GameMap m = new GameMap(10, 10, "A");
        boolean result1 = m.attachedAtRelativePoint(new Point(0, -1), 141440);
        boolean result2 = m.attachedAtRelativePoint(new Point(0, 1), 141440);
        boolean result3 = m.attachedAtRelativePoint(new Point(-1, 0), 141440);
        boolean result4 = m.attachedAtRelativePoint(new Point(1, 0), 141440);
        assertEquals(true, result1 && result2 && result3 && result4);
        boolean result5 = m.attachedAtRelativePoint(new Point(0, -1), 64);
        assertEquals(false, result5);
    }

    /**
     * Tests if the map provides correct values if a block is attached. 
     */
    @Test
    void isBlockAttachedTest() {
        GameMap map = new GameMap(20, 20, "A");
        map.addReport(0, 0, CellType.FREE, ZoneType.NONE, 0, 0);
        map.setAgentPosition("a1", new Point(2,3));
        List<Point> attachedThings = new ArrayList<>();
        attachedThings.add(new Point(0, -1));
        map.setAgentAttached("a1", attachedThings);
        boolean result = map.isBlockAttached(new Point(2, 2));
        assertEquals(true, result);
    }
}
