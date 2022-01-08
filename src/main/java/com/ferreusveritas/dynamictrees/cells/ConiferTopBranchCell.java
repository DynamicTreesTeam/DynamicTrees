package com.ferreusveritas.dynamictrees.cells;

import com.ferreusveritas.dynamictrees.api.cells.Cell;
import net.minecraft.util.Direction;

public class ConiferTopBranchCell implements Cell {

    @Override
    public int getValue() {
        return 5;
    }

    static final int[] map = {2, 5, 3, 3, 3, 3};

    //Used for giving more hydration if the below block is also a branch
    @Override
    public int getValueFromSide(Direction side) {
        return map[side.ordinal()];
    }

}
