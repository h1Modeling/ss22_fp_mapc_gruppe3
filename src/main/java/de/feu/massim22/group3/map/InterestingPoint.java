package de.feu.massim22.group3.map;

import java.awt.Point;

/**
 * The Record <code>InterestingPoint</code> defines a data structure to store information about special cells
 * in the <code>GameMap</code> which are used in the path finding process.
 *
 * @author Heinz Stadler
 */
public record InterestingPoint(Point point, ZoneType zoneType, CellType cellType, String data) { 
    public String toString() {
        return "Point: " + point.toString() + " ZoneType: " + zoneType.name() + " CellType: " + cellType.name() + " Data: " + data;
    }
}
