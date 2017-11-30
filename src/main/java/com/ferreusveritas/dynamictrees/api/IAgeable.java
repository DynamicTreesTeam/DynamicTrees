package com.ferreusveritas.dynamictrees.api;

import java.util.Random;

import com.ferreusveritas.dynamictrees.api.backport.BlockPos;
import com.ferreusveritas.dynamictrees.api.backport.IBlockState;
import com.ferreusveritas.dynamictrees.api.backport.WorldDec;

public interface IAgeable {

	/**
	 * 
	 * @param world
	 * @param pos
	 * @param state
	 * @param rand
	 * @param fast
	 * @return true if block was destroyed after the ageing, false otherwise
	 */
	public boolean age(WorldDec world, BlockPos pos, IBlockState state, Random rand, boolean fast);
	
}
