package com.ferreusveritas.dynamictrees.api;

import com.ferreusveritas.dynamictrees.trees.Species;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public interface ICustomRootDecay {
	/**
	 * @param world      The World
	 * @param rootPos    The position of the root block
	 * @param rootyState The blockstate of the root block
	 * @param species    The species of the tree that was removed
	 * @return true if handled, false to run the default decay algorithm
	 */
	boolean doDecay(World world, BlockPos rootPos, IBlockState rootyState, Species species);
}
