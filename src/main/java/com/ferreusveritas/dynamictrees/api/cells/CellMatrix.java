package com.ferreusveritas.dynamictrees.api.cells;

import net.minecraft.util.EnumFacing;

public class CellMatrix implements ICell {

	private final int value;
	private final byte[] valMap;
	
	public CellMatrix(int value, byte valMap[]) {
		this.value = value;
		this.valMap = valMap;
	}
	
	@Override
	public int getValue() {
		return value;
	}
	
	@Override
	public int getValueFromSide(EnumFacing side) {
		return valMap[(side.ordinal() << 3) + value];
	}
	
}
