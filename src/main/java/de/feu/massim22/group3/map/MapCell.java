package de.feu.massim22.group3.map;

public class MapCell {
	
	private int maxSizeReports = 5;
	private ReportList reports = new ReportList(maxSizeReports);
	private MapCellReport recentReport;
	
	MapCell() {
		// initialize Reports with empty cell
		recentReport = MapCellReport.createEmpty();
	}
	
	void addReport(MapCellReport report) {
		recentReport = report;
		reports.add(report);
	}
	
	CellType getCellType() {
		return recentReport.getCellType();
	}
	
	ZoneType getZoneType() {
		return recentReport.getZoneType();
	}
	
	void removeAgentReport(int agentIndex) {
		reports.remove(agentIndex);
		// Update last report
		if (recentReport.getReporter() == agentIndex) {
			recentReport = reports.getRecent();
		}
	}
	
	void mergeIntoCell(MapCell foreignCell) {
		reports.merge(foreignCell.reports);
		recentReport = reports.getRecent();
	}
}
