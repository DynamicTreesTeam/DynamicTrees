package com.ferreusveritas.dynamictrees.cells;

import com.ferreusveritas.dynamictrees.api.cells.ICell;

import net.minecraft.util.Direction;

public class CellConiferBranch implements ICell {

	@Override
	public int getValue() {
		return 5;
	}

	static final int map[] = {2, 2, 3, 3, 3, 3};
	
	@Override
	public int getValueFromSide(Direction side) {
		return map[side.ordinal()];
	}
	
}
