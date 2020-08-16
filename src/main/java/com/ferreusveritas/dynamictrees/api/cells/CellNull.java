package com.ferreusveritas.dynamictrees.api.cells;

import net.minecraft.util.Direction;

/**
 * Cell that always returns 0
 * 
 * @author ferreusveritas
 *
 */
public class CellNull implements ICell {

	public static final CellNull NULLCELL = new CellNull();
	
	@Override
	public int getValue() {
		return 0;
	}

	@Override
	public int getValueFromSide(Direction side) {
		return 0;
	}

}
