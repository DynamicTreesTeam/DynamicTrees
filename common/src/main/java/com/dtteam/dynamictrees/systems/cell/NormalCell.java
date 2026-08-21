package com.dtteam.dynamictrees.systems.cell;

import com.dtteam.dynamictrees.api.cell.Cell;
import net.minecraft.core.Direction;

/**
 * Cell that simply returns it's value
 *
 * @author ferreusveritas
 */
public class NormalCell implements Cell {

    private final int value;

    public NormalCell(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }

    public int getValueFromSide(Direction side) {
        return value;
    }

}
