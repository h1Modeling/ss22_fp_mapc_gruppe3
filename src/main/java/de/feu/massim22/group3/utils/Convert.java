package de.feu.massim22.group3.utils;

import de.feu.massim22.group3.map.CellType;
import massim.protocol.data.Thing;

public class Convert {
    public static CellType thingToCellType(Thing t) {
        switch (t.type) {
            case Thing.TYPE_BLOCK: return blockToCellType(t.details);
            case Thing.TYPE_DISPENSER: return dispenserToCellType(t.details);
            case Thing.TYPE_OBSTACLE: return CellType.OBSTACLE;
            case Thing.TYPE_MARKER: return CellType.FREE;
            default: return CellType.UNKNOWN;
        } 
    }

    public static CellType blockNameToDispenser(Thing t) {
        switch (t.type) {
            case "b0": return CellType.DISPENSER_0;
            case "b1": return CellType.DISPENSER_1;
            case "b2": return CellType.DISPENSER_2;
            case "b3": return CellType.DISPENSER_3;
            case "b4": return CellType.DISPENSER_4;
            default: return CellType.UNKNOWN;
        }
    }

    private static CellType blockToCellType(String blockDetail) {
        switch (blockDetail) {
            case "b0": return CellType.BLOCK_0;
            case "b1": return CellType.BLOCK_1;
            case "b2": return CellType.BLOCK_2;
            case "b3": return CellType.BLOCK_3;
            case "b4": return CellType.BLOCK_4;
            default: return CellType.UNKNOWN;
        }
    }

    private static CellType dispenserToCellType(String blockDetail) {
        switch (blockDetail) {
            case "b0": return CellType.DISPENSER_0;
            case "b1": return CellType.DISPENSER_1;
            case "b2": return CellType.DISPENSER_2;
            case "b3": return CellType.DISPENSER_3;
            case "b4": return CellType.DISPENSER_4;
            default: return CellType.UNKNOWN;
        }
    }
}
