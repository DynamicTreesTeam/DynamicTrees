package com.ferreusveritas.dynamictrees.util;

import java.util.Random;

import net.minecraft.util.math.BlockPos;

public class RandomXOR extends Random {

	private static final long serialVersionUID = -3477272122511092632L;

	int xor = 0;
	
	public RandomXOR() {
	}
	
	public RandomXOR(long seed) {
		super(seed);
	}
	
	public void setXOR(BlockPos pos) {
		setXOR((((int)pos.getX() * 674365771) ^ ((int)pos.getZ() * 254326997)) >> 4);
	}
    
    public void setXOR(int xor) {
    	this.xor = xor;
    }
    
	@Override
	protected int next(int bits) {
		return super.next(bits) ^ (xor & ((1 << bits) - 1));
	}
	
}
