package com.ferreusveritas.dynamictrees.api.cells;

import com.ferreusveritas.dynamictrees.api.backport.EnumFacing;

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
