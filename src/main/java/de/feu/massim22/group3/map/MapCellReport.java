package de.feu.massim22.group3.map;

public class MapCellReport {
	
	private CellType cellType;
	private ZoneType zoneType;
	private int reporter;
	private long reportTime;

	public MapCellReport(CellType cellType, ZoneType zoneType, int reporter) {
		this.cellType = cellType;
		this.zoneType = zoneType;
		this.reporter = reporter;
		this.reportTime = System.currentTimeMillis();
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
		return new MapCellReport(CellType.UNKNOWN, ZoneType.NONE, -1);
	}
}
