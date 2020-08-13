package com.ferreusveritas.dynamictrees.api.cells;

import net.minecraft.util.Direction;

public interface ICell {

	//This is the actual value of the cell.
	public int getValue();
	
	//This is the value the cell returns for a side
	public int getValueFromSide(Direction side);
	
}
