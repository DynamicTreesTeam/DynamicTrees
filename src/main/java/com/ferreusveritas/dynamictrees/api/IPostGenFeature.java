package com.ferreusveritas.dynamictrees.api;

import com.ferreusveritas.dynamictrees.trees.Species;
import com.ferreusveritas.dynamictrees.util.SafeChunkBounds;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IWorld;
import net.minecraft.world.biome.Biome;

import java.util.List;

public interface IPostGenFeature extends IGenFeature {

	/**
	 * Do post generation operations
	 *
	 *
	 * @param world The world - use for other things like world.getBiome
	 * @param rootPos The position of the rooty dirt block
	 * @param species The species being processed
	 * @param biome The biome at this location
	 * @param radius The Poisson disc radius
	 * @param endPoints A list of branch endpoints
	 * @param safeBounds A safebounds structure for preventing runaway cascading generation
	 * @param initialDirtState The state of the dirt block before tree generation took place
	 * @param seasonValue The current season value
	 * @param seasonFruitProductionFactor The production factor for fruit generation
	 * @return true if operation succeeded. false otherwise.
	 */
	boolean postGeneration(IWorld world, BlockPos rootPos, Species species, Biome biome, int radius, List<BlockPos> endPoints, SafeChunkBounds safeBounds, BlockState initialDirtState, Float seasonValue, Float seasonFruitProductionFactor);

}
