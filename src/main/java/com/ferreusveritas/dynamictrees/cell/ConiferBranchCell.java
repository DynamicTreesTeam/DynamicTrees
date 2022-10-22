package com.ferreusveritas.dynamictrees.cell;

import com.ferreusveritas.dynamictrees.api.cells.Cell;
import net.minecraft.core.Direction;

public class ConiferBranchCell implements Cell {

    @Override
    public int getValue() {
        return 5;
    }

    static final int[] map = {2, 2, 3, 3, 3, 3};

    @Override
    public int getValueFromSide(Direction side) {
        return map[side.ordinal()];
    }

}
