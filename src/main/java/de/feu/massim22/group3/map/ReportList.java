package de.feu.massim22.group3.map;

import java.util.ArrayList;
import java.util.List;

public class ReportList
{
    private List<MapCellReport> list;
    private int maxSize;

    ReportList(int size) {
        this.list = new ArrayList<>(size);
        this.maxSize = size;
    }

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
    
    void add(MapCellReport report) {
    	add(report, list.size());
    }
    
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
    
    void remove(int agentId) {
    	for (MapCellReport r : list) {
    		if (r.getReporter() == agentId) {
    			list.remove(r);
    			break;
    		}
    	}
    }

    MapCellReport getRecent() {
        if (list.isEmpty()) return MapCellReport.createEmpty();
        return list.get(list.size() - 1);
    }
    
    MapCellReport get(int index) {
        if (list.isEmpty()) return MapCellReport.createEmpty();
        return list.get(index);
    }

    boolean isEmpty() {
        return list.isEmpty();
    }
    
    void merge(ReportList foreignList) {
    	for (int i = foreignList.list.size() - 1; i >= 0; i--) {
    		addFromMerge(foreignList.list.get(i));
    	}
    }
    
    int getSize() {
    	return list.size();
    }
}
