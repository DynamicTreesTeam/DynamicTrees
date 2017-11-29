package com.ferreusveritas.dynamictrees.api.cells;

import net.minecraft.util.EnumFacing;

/**
 * Cell that always returns 0
 * 
 * @author ferreusveritas
 *
 */
public class CellNull implements ICell {

	@Override
	public int getValue() {
		return 0;
	}

	@Override
	public int getValueFromSide(EnumFacing side) {
		return 0;
	}

}
