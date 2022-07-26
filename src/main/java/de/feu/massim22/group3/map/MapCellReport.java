package de.feu.massim22.group3.map;

/**
 * The Class <code>MapCellReport</code> stores information of the current state of a Cell in the vision of an agent.
 *
 * @author Heinz Stadler
 */
public class MapCellReport {
    
    private CellType cellType;
    private ZoneType zoneType;
    private int reporter;
    private long reportTime;
    private int step;

    /**
     * Initializes a new MapCellReport.
     * 
     * @param cellType the cell type of the cell
     * @param zoneType the zone type of the cell
     * @param reporter the agent which sends the data
     * @param step the current step of the simulation
     */
    public MapCellReport(CellType cellType, ZoneType zoneType, int reporter, int step) {
        this.cellType = cellType;
        this.zoneType = zoneType;
        this.reporter = reporter;
        this.reportTime = System.currentTimeMillis();
        this.step = step;
    }

    /**
     * Gets the step of the report.
     * 
     * @return the step of the report
     */
    public int getStep() {
        return step;
    }

    /**
     * Gets the cell type of the reported cell.
     * 
     * @return the cell type of the reported cell 
     */
    CellType getCellType() {
        return cellType;
    }
    
    /**
     * Gets the zone type of the reported cell.
     * 
     * @return the zone type of the reported cell 
     */
    ZoneType getZoneType() {
        return zoneType;
    }

    /**
     * Gets the index of the agent which has sent the report.
     * 
     * @return the index of the agent which has sent the report
     */
    int getReporter() {
        return reporter;
    }
    
    /**
     * Gets the time of the report.
     * 
     * @return the time of the report
     */
    long getReportTime() {
        return reportTime;
    }
    
    /**
     * Creates a new <code>MapCellReport> with CellType.UNKNOWN and ZoneType.NONE.
     * 
     * @return a new <code>MapCellReport> with CellType.UNKNOWN and ZoneType.NONE
     */
    static MapCellReport createEmpty() {
        return new MapCellReport(CellType.UNKNOWN, ZoneType.NONE, -1, 0);
    }
}
