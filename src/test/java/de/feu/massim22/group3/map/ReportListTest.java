package de.feu.massim22.group3.map;

import static org.junit.Assert.assertEquals;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * The Class <code>ReportListTest</code> provides methods for testing the class <code>ReportList</code>.
 * 
 * @see ReportList
 * @author Heinz Stadler
 */
class ReportListTest {
    
    private ReportList list;
    
    /**
     * Instantiates a new ReportList with size 5.
     */
    @BeforeEach
    void init() {
        list = new ReportList(5);
    }

    /**
     * Tests if a report of an agent overwrites the old reports of the agent.
     */
    @Test
    void testAddSameAgent() {
        list.add(new MapCellReport(CellType.FREE, ZoneType.NONE, 0, 0));
        list.add(new MapCellReport(CellType.ENEMY, ZoneType.NONE, 0, 0));
        assertEquals(1, list.getSize());
    }
    
    /**
     * Tests if the list can handle report overflows correctly.
     */
    @Test
    void testAddOverflow() {
        list.add(new MapCellReport(CellType.FREE, ZoneType.NONE, 0, 0));
        list.add(new MapCellReport(CellType.FREE, ZoneType.NONE, 1, 1));
        list.add(new MapCellReport(CellType.FREE, ZoneType.NONE, 2, 2));
        list.add(new MapCellReport(CellType.FREE, ZoneType.NONE, 3, 3));
        list.add(new MapCellReport(CellType.FREE, ZoneType.NONE, 4, 4));
        list.add(new MapCellReport(CellType.ENEMY, ZoneType.NONE, 5, 5));
        assertEquals(5, list.getSize());
        assertEquals(CellType.ENEMY, list.getRecent().getCellType());
    }

    /**
     * Tests if a report can be successfully removed from the list.
     */
    @Test
    void testRemove() {
        list.add(new MapCellReport(CellType.FREE, ZoneType.NONE, 0, 1));
        list.add(new MapCellReport(CellType.OBSTACLE, ZoneType.NONE, 1, 1));
        list.remove(0);
        assertEquals(1, list.getSize());
        assertEquals(CellType.OBSTACLE, list.getRecent().getCellType());
        list.remove(2);
        assertEquals(1, list.getSize());
        assertEquals(CellType.OBSTACLE, list.getRecent().getCellType());
    }

    /**
     * Tests if the most recent report gets correctly read.
     */
    @Test
    void testGetRecent() {
        list.add(new MapCellReport(CellType.FREE, ZoneType.NONE, 0, 0));
        list.add(new MapCellReport(CellType.OBSTACLE, ZoneType.NONE, 1, 1));
        assertEquals(CellType.OBSTACLE, list.getRecent().getCellType());
    }

    /**
     * Tests if two lists get correctly merged with a combined list size that results in an overflow.
     * @throws InterruptedException when the Thread was interrupted while waiting
     */
    @Test
    void testMerge() throws InterruptedException {
        ReportList toMerge = new ReportList(5);
        list.add(new MapCellReport(CellType.DISPENSER_0, ZoneType.NONE, 0, 0));
        Thread.sleep(1);
        list.add(new MapCellReport(CellType.DISPENSER_1, ZoneType.NONE, 1, 1));
        Thread.sleep(1);
        toMerge.add(new MapCellReport(CellType.DISPENSER_2, ZoneType.NONE, 2, 2));
        Thread.sleep(1);
        list.add(new MapCellReport(CellType.DISPENSER_3, ZoneType.NONE, 3, 3));
        Thread.sleep(1);
        toMerge.add(new MapCellReport(CellType.DISPENSER_4, ZoneType.NONE, 4, 4));
        Thread.sleep(1);
        list.add(new MapCellReport(CellType.DISPENSER_1, ZoneType.NONE, 5, 5));
        Thread.sleep(1);
        toMerge.add(new MapCellReport(CellType.DISPENSER_2, ZoneType.NONE, 6, 6));
        Thread.sleep(1);
        toMerge.add(new MapCellReport(CellType.DISPENSER_3, ZoneType.NONE, 7, 7));
        Thread.sleep(1);
        
        list.merge(toMerge);
        
        assertEquals(CellType.DISPENSER_3, list.get(4).getCellType());
        assertEquals(CellType.DISPENSER_2, list.get(3).getCellType());
        assertEquals(CellType.DISPENSER_1, list.get(2).getCellType());
        assertEquals(CellType.DISPENSER_4, list.get(1).getCellType());
        assertEquals(CellType.DISPENSER_3, list.get(0).getCellType());
    }
    
    /**
     * Tests if two lists get correctly merged with a combined list size smaller than the internal list size.
     * @throws InterruptedException when the Thread was interrupted while waiting
     */
    @Test
    void testMerge2() throws InterruptedException {
        list.add(new MapCellReport(CellType.OBSTACLE, ZoneType.NONE, 0, 0));
        list.add(new MapCellReport(CellType.FREE, ZoneType.NONE, 1, 1));
        Thread.sleep(1);
        ReportList toMerge = new ReportList(5);
        toMerge.add(new MapCellReport(CellType.DISPENSER_0, ZoneType.NONE, 2, 2));
        list.merge(toMerge);
        assertEquals(CellType.DISPENSER_0, list.getRecent().getCellType());
    }
}
