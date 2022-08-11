package de.feu.massim22.group3.map;

import massim.protocol.data.Thing;

/**
 * The Enumeration <code>CellType</code> indicates the type of a certain cell in the <code>GameMap</code>
 *
 * @see GameMap
 * @author Heinz Stadler
 */
public enum CellType {
    /** The cell type is unknown */
    UNKNOWN("TYPE_UNKNOWN"),
    /** The cell indicates a member of the own team */
    TEAMMATE(Thing.TYPE_ENTITY),
    /** the cell indicates a member of the enemy team */
    ENEMY(Thing.TYPE_ENTITY),
    /** the cell indicates an obstacle */
    OBSTACLE(Thing.TYPE_OBSTACLE),
    /** the cell indicates the absence of a thing at the cell */
    FREE("TYPE_FREE"),
    /** the cell indicates a dispenser with detail d0 */
    DISPENSER_0(Thing.TYPE_DISPENSER),
    /** the cell indicates a dispenser with detail d1 */
    DISPENSER_1(Thing.TYPE_DISPENSER),
    /** the cell indicates a dispenser with detail d2 */
    DISPENSER_2(Thing.TYPE_DISPENSER),
    /** the cell indicates a dispenser with detail d3 */
    DISPENSER_3(Thing.TYPE_DISPENSER),
    /** the cell indicates a dispenser with detail d4 */
    DISPENSER_4(Thing.TYPE_DISPENSER),
    /** the cell indicates a block with detail b0 */
    BLOCK_0(Thing.TYPE_BLOCK),
    /** the cell indicates a block with detail b1 */
    BLOCK_1(Thing.TYPE_BLOCK),
    /** the cell indicates a block with detail b2 */
    BLOCK_2(Thing.TYPE_BLOCK),
    /** the cell indicates a block with detail b3 */
    BLOCK_3(Thing.TYPE_BLOCK),
    /** the cell indicates a block with detail b4 */
    BLOCK_4(Thing.TYPE_BLOCK);

    private CellType(String type) {
        this.type = type;
    }
    private String type;

    /**
     * Gets if the cell is of type dispenser.
     * 
     * @return true if the cell is a dispenser
     */
    public boolean isDispenser() {
        return type.equals(Thing.TYPE_DISPENSER);
    }

    /**
     * Gets if the cell is of type block.
     * 
     * @return true if the cell is a block
     */
    public boolean isBlock() {
        return type.equals(Thing.TYPE_BLOCK);
    }
}
