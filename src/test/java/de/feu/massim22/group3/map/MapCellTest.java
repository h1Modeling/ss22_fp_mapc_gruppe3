package de.feu.massim22.group3.map;

import static org.junit.Assert.assertEquals;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * The Class <code>MapCellTest</code> provides methods for testing the class <code>MapCell</code>.
 * 
 * @see MapCell
 * @author Heinz Stadler
 */
class MapCellTest {
    
    private MapCell cell;
    
    /**
     * Instantiates a new MapCell for further use.
     */
    @BeforeEach
    void init() {
        cell = new MapCell();
    }

    /**
     * Tests if an agent report can successfully be removed from the map.
     */
    @Test
    void testRemoveAgentReport() {
        cell.addReport(new MapCellReport(CellType.OBSTACLE, ZoneType.NONE, 0, 0));
        cell.addReport(new MapCellReport(CellType.FREE, ZoneType.NONE, 1, 1));
        cell.removeAgentReport(1);
        assertEquals(CellType.OBSTACLE, cell.getCellType());
    }

    /**
     * Tests if two cells can be successfully merged without loosing information.
     * @throws InterruptedException when the Thread was interrupted while waiting
     */
    @Test
    void testMergeIntoCell() throws InterruptedException {
        cell.addReport(new MapCellReport(CellType.OBSTACLE, ZoneType.NONE, 0, 0));
        cell.addReport(new MapCellReport(CellType.FREE, ZoneType.NONE, 1, 1));
        Thread.sleep(1);
        MapCell toMerge = new MapCell();
        toMerge.addReport(new MapCellReport(CellType.DISPENSER_0, ZoneType.NONE, 2, 2));
        cell.mergeIntoCell(toMerge);
        assertEquals(CellType.DISPENSER_0, cell.getCellType());
    }

}
