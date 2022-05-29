package de.feu.massim22.group3.map;

import massim.protocol.data.Thing;

public enum CellType {
    UNKNOWN("TYPE_UNKNOWN"),
    TEAMMATE(Thing.TYPE_ENTITY),
    ENEMY(Thing.TYPE_ENTITY),
    OBSTACLE(Thing.TYPE_OBSTACLE),
    FREE("TYPE_FREE"),
    DISPENSER_0(Thing.TYPE_DISPENSER),
    DISPENSER_1(Thing.TYPE_DISPENSER),
    DISPENSER_2(Thing.TYPE_DISPENSER),
    DISPENSER_3(Thing.TYPE_DISPENSER),
    DISPENSER_4(Thing.TYPE_DISPENSER),
    BLOCK_0(Thing.TYPE_BLOCK),
    BLOCK_1(Thing.TYPE_BLOCK),
    BLOCK_2(Thing.TYPE_BLOCK),
    BLOCK_3(Thing.TYPE_BLOCK),
    BLOCK_4(Thing.TYPE_BLOCK);

    private CellType(String type) {
        this.type = type;
    }
    private String type;

    public boolean isDispenser() {
        return type.equals(Thing.TYPE_DISPENSER);
    }

    public boolean isBlock() {
        return type.equals(Thing.TYPE_BLOCK);
    }
}
