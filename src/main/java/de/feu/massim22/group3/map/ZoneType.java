package de.feu.massim22.group3.map;

/**
 * The Enumeration <code>ZoneType</code> indicates the zone type of a certain cell in the <code>GameMap</code>
 *
 * @see GameMap
 * @author Heinz Stadler
 */
public enum ZoneType {
    /** The cell is on top of a goal zone */
    GOALZONE,
    /** The cell is on top of a role zone */
    ROLEZONE,
    /** The cell is on top of a clear zone */
    CLEARZONE,
    /** The cell has no zone information */
    NONE
}
