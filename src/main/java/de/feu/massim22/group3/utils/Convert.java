package de.feu.massim22.group3.utils;

import de.feu.massim22.group3.map.CellType;
import massim.protocol.data.Thing;

/**
 * The Class <code>Convert</code> contains static methods to convert between massim definitions and definitions defined by the <code>GameMap</code>
 * and its parts.
 *
 * @author Heinz Stadler
 */
public class Convert {

    /**
     * Translates a Thing to a CellType.
     * 
     * @param t the thing
     * @return the cell type
     */
    public static CellType thingToCellType(Thing t) {
        switch (t.type) {
            case Thing.TYPE_BLOCK: return blockToCellType(t.details);
            case Thing.TYPE_DISPENSER: return dispenserToCellType(t.details);
            case Thing.TYPE_OBSTACLE: return CellType.OBSTACLE;
            case Thing.TYPE_MARKER: return CellType.FREE;
            default: return CellType.UNKNOWN;
        } 
    }

    /**
     * Translates a task requirement to a cell type.
     * 
     * @param t the task requirement
     * @return the cell type
     */
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

    /**
     * Translates a dispenser cell type to a thing type name.
     * 
     * @param t the cell type
     * @return the thing type name
     */
    public static String cellTypeToThingDetail(CellType t) {
        switch (t) {
            case DISPENSER_0: return "b0";
            case DISPENSER_1: return "b1";
            case DISPENSER_2: return "b2";
            case DISPENSER_3: return "b3";
            case DISPENSER_4: return "b4";
            default: return "";
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
