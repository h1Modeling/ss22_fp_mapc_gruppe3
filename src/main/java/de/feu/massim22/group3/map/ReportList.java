package de.feu.massim22.group3.map;

import java.util.ArrayList;
import java.util.List;

/**
 * The Class <code>ReportList</code> stores a List of <code>MapCellReports</code> and
 * provides methods for managing the reports.
 *
 * @author Heinz Stadler
 */
public class ReportList
{
    private List<MapCellReport> list;
    private int maxSize;

    /**
     * Initializes a new ReportList.
     * @param size the initial size of the list
     */
    ReportList(int size) {
        this.list = new ArrayList<>(size);
        this.maxSize = size;
    }

    /**
     * Adds a new report at a certain index in the list.
     * 
     * @param report the report to add
     * @param index the index at which the report should be added
     */
    void add(MapCellReport report, int index) {
        // Remove old entry from the reporter
        for (int i = 0; i < list.size(); i++) {
            MapCellReport r = list.get(i);
            if (r.getReporter() == report.getReporter()) {
                list.remove(r);
                if (i < index) index--;
                break;
            }
        }
        list.add(index, report);
        // Remove oldest entry if list is full
        if (list.size() > maxSize) {
            list.remove(0);
        }
    }
    
    /**
     * Adds a new report
     * @param report the report to add
     */
    void add(MapCellReport report) {
        add(report, list.size());
    }
    
    /**
     * Adds a report at a certain position in the list to keep the list sorted by
     * the report time.
     * 
     * @param foreignReport the report which should be added
     */
    void addFromMerge(MapCellReport foreignReport) {
        // Add if list is empty
        if (list.size() == 0) {
            add(foreignReport);
            return;
        }
        // Add the report between the existing reports depending on report time
        for (int i = list.size() - 1; i >= 0; i--) {
            MapCellReport current = list.get(i);
            if (current.getReportTime() < foreignReport.getReportTime()) {
                add(foreignReport, i + 1);
                break;
            }
        }
    }
    
    /**
     * Removes all reports from an agent with the provided id.
     * 
     * @param agentId the id of the agent which reports should be removed
     */
    void remove(int agentId) {
        for (MapCellReport r : list) {
            if (r.getReporter() == agentId) {
                list.remove(r);
                break;
            }
        }
    }

    /**
     * Gets the most recent report in the list.
     */
    MapCellReport getRecent() {
        if (list.isEmpty()) return MapCellReport.createEmpty();
        return list.get(list.size() - 1);
    }
    
    /**
     * Gets the report at a certain index.
     * 
     * @param index the index of the report
     * @return the report at the provided index
     */
    MapCellReport get(int index) {
        if (list.isEmpty()) return MapCellReport.createEmpty();
        return list.get(index);
    }

    /**
     * Tests if the list is empty.
     * @return true if the list is empty
     */
    boolean isEmpty() {
        return list.isEmpty();
    }
    
    /**
     * Merges a <code>ReportList</code> into this <code>ReportList</code>.
     * After the merge the list will still be sorted by report time.
     * 
     * @param foreignList the ReportList to merge
     */
    void merge(ReportList foreignList) {
        for (int i = foreignList.list.size() - 1; i >= 0; i--) {
            addFromMerge(foreignList.list.get(i));
        }
    }
    
    /**
     * Gets the size of the list.
     * 
     * @return the size of the list
     */
    int getSize() {
        return list.size();
    }
}
