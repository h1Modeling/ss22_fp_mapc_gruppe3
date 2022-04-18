package de.feu.massim22.group3.map;

import static org.junit.jupiter.api.Assertions.*;

import java.awt.Point;

import org.junit.jupiter.api.Test;

class GameMapTest {
	
	private GameMap map = new GameMap(3, 5);

	@Test
	void testMapExtensionLeft() {
		map.addReport(-2, 0, CellType.FREE, ZoneType.NONE, 0);
		assertEquals(-21, map.getTopLeft().x);
		assertEquals(CellType.FREE, map.getCellType(-2, 0));
	}
	
	@Test
	void testMapExtensionRight() {
		map.addReport(4, 0, CellType.FREE, ZoneType.NONE, 0);
		assertEquals(-1, map.getTopLeft().x);
		assertEquals(CellType.FREE, map.getCellType(4, 0));
	}
	
	@Test
	void testMapExtensionTop() {
		map.addReport(0, -5, CellType.FREE, ZoneType.NONE, 0);
		assertEquals(-22, map.getTopLeft().y);
		assertEquals(CellType.FREE, map.getCellType(0, -5));
	}

	@Test
	void testMapExtensionBottom() {
		map.addReport(0, 5, CellType.FREE, ZoneType.NONE, 0);
		assertEquals(-2, map.getTopLeft().y);
		assertEquals(CellType.FREE, map.getCellType(0, 5));
	}

	@Test
	void testAddReport() {
		assertEquals(-1, map.getTopLeft().x);
		assertEquals(-2, map.getTopLeft().y);
		map.addReport(-1, 0, CellType.FREE, ZoneType.NONE, 0);
		assertEquals(CellType.FREE, map.getCellType(-1, 0));
	}
	
	@Test
	void testMergeIntoMapUndiscovered() throws InterruptedException {
		map.addReport(1, 0, CellType.FREE, ZoneType.NONE, 0);
		GameMap toMerge = new GameMap(5, 10);
		Thread.sleep(1);
		toMerge.addReport(1, 0, CellType.DISPENSER_0, ZoneType.NONE, 1);
		Point offset = map.mergeIntoMap(toMerge, new Point(2, 1), new Point(4, -1));
		assertEquals(CellType.FREE, map.getCellType(1, 0));
		assertEquals(CellType.DISPENSER_0, map.getCellType(1 + offset.x, 0 + offset.y));
	}

	@Test
	void testMergeIntoMapDiscovered() throws InterruptedException {
		map.addReport(1, 0, CellType.FREE, ZoneType.NONE, 0);
		Thread.sleep(1);
		map.setFinalSize(10, 10);
		GameMap toMerge = new GameMap(10, 10);
		Thread.sleep(1);
		toMerge.addReport(1, 1, CellType.DISPENSER_0, ZoneType.NONE, 1);
		//toMerge.setFinalSize(10, 10);
		Thread.sleep(1);
		Point offset = map.mergeIntoMap(toMerge, new Point(2, 1), new Point(4, -1));
		assertEquals(CellType.FREE, map.getCellType(1, 0));
		assertEquals(CellType.DISPENSER_0, map.getCellType(1 + offset.x, 1 + offset.y));
	}

	@Test
	void testSetFinalSize() {
		map.addReport(1, 3, CellType.FREE, ZoneType.NONE, 1);
		map.setFinalSize(10, 10);
		assertEquals(CellType.FREE, map.getCellType(1, 3));
	}
}
