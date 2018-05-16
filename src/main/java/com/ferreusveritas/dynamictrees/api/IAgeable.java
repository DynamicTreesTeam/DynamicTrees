package com.ferreusveritas.dynamictrees.api;

import java.util.Random;

import com.ferreusveritas.dynamictrees.util.SafeChunkBounds;

import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public interface IAgeable {

	/**
	 * 
	 * @param world The world
	 * @param pos the position of this block that is being aged
	 * @param state the state of this block
	 * @param rand random number generator
	 * @param worldGen true if trying to age the block in a worldGen or instant create situation. false for regular block tick aging
	 * @return -1 if block was destroyed after the ageing, otherwise the hydro value of the block
	 */
	public int age(World world, BlockPos pos, IBlockState state, Random rand, SafeChunkBounds safeBounds);
	
}
