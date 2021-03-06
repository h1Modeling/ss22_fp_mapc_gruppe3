package de.feu.massim22.group3.map;

import java.awt.Point;

public record InterestingPoint(Point point, ZoneType zoneType, CellType cellType, String data) { 
    public String toString() {
        return "Point: " + point.toString() + " ZoneType: " + zoneType.name() + " CellType: " + cellType.name() + " Data: " + data;
    }
}
