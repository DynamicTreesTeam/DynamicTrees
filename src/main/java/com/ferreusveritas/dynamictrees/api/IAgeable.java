package com.ferreusveritas.dynamictrees.api;

import com.ferreusveritas.dynamictrees.util.SafeChunkBounds;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.Random;

public interface IAgeable {

	/**
	 * @param world The world
	 * @param pos   the position of this block that is being aged
	 * @param state the state of this block
	 * @param rand  random number generator
	 * @return -1 if block was destroyed after the ageing, otherwise the hydro value of the block
	 */
	int age(World world, BlockPos pos, IBlockState state, Random rand, SafeChunkBounds safeBounds);

}
