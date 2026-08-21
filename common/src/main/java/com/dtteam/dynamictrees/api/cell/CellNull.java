package com.dtteam.dynamictrees.api.cell;

import net.minecraft.core.Direction;

/**
 * Cell that always returns 0
 *
 * @author ferreusveritas
 */
public class CellNull implements Cell {

    public static final CellNull NULL_CELL = new CellNull();

    public int getValue() {
        return 0;
    }

    public int getValueFromSide(Direction side) {
        return 0;
    }

}
