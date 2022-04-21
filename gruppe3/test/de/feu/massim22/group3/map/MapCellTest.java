package de.feu.massim22.group3.map;

import static org.junit.Assert.assertEquals;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class MapCellTest {
	
	private MapCell cell;
	
	@BeforeEach
	void init() {
		cell = new MapCell();
	}

	@Test
	void testRemoveAgentReport() {
		cell.addReport(new MapCellReport(CellType.OBSTACLE, ZoneType.NONE, 0));
		cell.addReport(new MapCellReport(CellType.FREE, ZoneType.NONE, 1));
		cell.removeAgentReport(1);
		assertEquals(CellType.OBSTACLE, cell.getCellType());
	}

	@Test
	void testMergeIntoCell() throws InterruptedException {
		cell.addReport(new MapCellReport(CellType.OBSTACLE, ZoneType.NONE, 0));
		cell.addReport(new MapCellReport(CellType.FREE, ZoneType.NONE, 1));
		Thread.sleep(1);
		MapCell toMerge = new MapCell();
		toMerge.addReport(new MapCellReport(CellType.DISPENSER_0, ZoneType.NONE, 2));
		cell.mergeIntoCell(toMerge);
		assertEquals(CellType.DISPENSER_0, cell.getCellType());
	}

}
