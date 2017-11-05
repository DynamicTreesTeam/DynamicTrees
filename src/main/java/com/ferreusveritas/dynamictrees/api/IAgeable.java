package com.ferreusveritas.dynamictrees.api;

import java.util.Random;

import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

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
	public boolean age(World world, BlockPos pos, IBlockState state, Random rand, boolean fast);
	
}
