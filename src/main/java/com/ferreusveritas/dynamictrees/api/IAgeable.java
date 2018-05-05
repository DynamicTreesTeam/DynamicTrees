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
	 * @param rapid
	 * @return -1 if block was destroyed after the ageing, otherwise the hydro value of the block
	 */
	public int age(World world, BlockPos pos, IBlockState state, Random rand, boolean rapid);
	
}
