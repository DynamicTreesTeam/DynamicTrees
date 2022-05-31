package com.ferreusveritas.dynamictrees.cells;

import com.ferreusveritas.dynamictrees.api.cells.Cell;
import net.minecraft.core.Direction;

public class MatrixCell implements Cell {

    private final int value;
    private final byte[] valMap;

    public MatrixCell(int value, byte[] valMap) {
        this.value = value;
        this.valMap = valMap;
    }

    @Override
    public int getValue() {
        return value;
    }

    @Override
    public int getValueFromSide(Direction side) {
        return valMap[(side.ordinal() << 3) + value];
    }

}
