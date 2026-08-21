package com.dtteam.dynamictrees.systems.cell;

import com.dtteam.dynamictrees.api.cell.Cell;
import net.minecraft.core.Direction;

public class MatrixCell implements Cell {

    private final int value;
    private final byte[] valMap;

    public MatrixCell(int value, byte[] valMap) {
        this.value = value;
        this.valMap = valMap;
    }

    public int getValue() {
        return value;
    }

    public int getValueFromSide(Direction side) {
        return valMap[(side.ordinal() << 3) + value];
    }

}
