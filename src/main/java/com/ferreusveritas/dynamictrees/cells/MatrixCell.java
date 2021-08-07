package com.ferreusveritas.dynamictrees.cells;

import com.ferreusveritas.dynamictrees.api.cells.ICell;
import net.minecraft.util.Direction;

public class MatrixCell implements ICell {

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
