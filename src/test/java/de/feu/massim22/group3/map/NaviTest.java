package de.feu.massim22.group3.map;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;

import org.junit.jupiter.api.Test;

public class NaviTest {

    @Test
	void getBlankCellArrayTest() {
        CellType[][] cells = Navi.get().getBlankCellArray(3);

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
}
