package de.feu.massim22.group3.map;

/**
 * The Class <code>MapCell</code> stores information of a Cell in the <code>GameMap</code>.
 *
 * @author Heinz Stadler
 */
public class MapCell {
    
    private int maxSizeReports = 5;
    private ReportList reports = new ReportList(maxSizeReports);
    private MapCellReport recentReport;
    
    /**
     * Instantiates a new <code>MapCell</code>.
     */
    MapCell() {
        // initialize Reports with empty cell
        recentReport = MapCellReport.createEmpty();
    }
    
    /**
     * Adds information to the cell.
     * 
     * @param report the information which should be stored into the cell
     */
    void addReport(MapCellReport report) {
        // avoid overriding dispensers by blocks
        if (recentReport.getCellType().isDispenser()) {
            report.copyCellTypeFromReport(recentReport);
        }
        recentReport = report;
        reports.add(report);
    }
    
    /**
     * Gets the cell type of the cell.
     * 
     * @return the <code>CellType</code> of the cell
     */
    CellType getCellType() {
        return recentReport.getCellType();
    }
    
    /**
     * Gets the zone type of the cell.
     * 
     * @return the <code>ZoneType</code> of the cell
     */
    ZoneType getZoneType() {
        return recentReport.getZoneType();
    }
    
    /**
     * Removes all reports of the agent with the provided id and sets
     * the cell data to the most recent report of the remaining reports.
     * 
     * @param agentIndex the index of the agent which reports should be removed
     */
    void removeAgentReport(int agentIndex) {
        reports.remove(agentIndex);
        // Update last report
        if (recentReport.getReporter() == agentIndex) {
            recentReport = reports.getRecent();
        }
    }
    
    /**
     * Merges information of another cell into this cell.
     * 
     * @param foreignCell the cell which information should be merged in
     */
    void mergeIntoCell(MapCell foreignCell) {
        reports.merge(foreignCell.reports);
        recentReport = reports.getRecent();
    }
}
