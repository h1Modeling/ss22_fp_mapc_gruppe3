package de.feu.massim22.group3.map;

public class MapCellReport {
	
	private CellType cellType;
	private ZoneType zoneType;
	private int reporter;
	private long reportTime;
	private int step;

	public MapCellReport(CellType cellType, ZoneType zoneType, int reporter, int step) {
		this.cellType = cellType;
		this.zoneType = zoneType;
		this.reporter = reporter;
		this.reportTime = System.currentTimeMillis();
		this.step = step;
	}

	public int getStep() {
		return step;
	}

	CellType getCellType() {
		return cellType;
	}
	
	ZoneType getZoneType() {
		return zoneType;
	}

	int getReporter() {
		return reporter;
	}
	
	long getReportTime() {
		return reportTime;
	}
	
	static MapCellReport createEmpty() {
		return new MapCellReport(CellType.UNKNOWN, ZoneType.NONE, -1, 0);
	}
}
