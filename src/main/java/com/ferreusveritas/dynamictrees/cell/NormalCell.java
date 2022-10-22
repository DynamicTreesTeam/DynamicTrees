package com.ferreusveritas.dynamictrees.cell;

import com.ferreusveritas.dynamictrees.api.cells.Cell;
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

    @Override
    public int getValue() {
        return value;
    }

    @Override
    public int getValueFromSide(Direction side) {
        return value;
    }

}
