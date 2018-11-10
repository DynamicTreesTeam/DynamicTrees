package com.ferreusveritas.dynamictrees.api;

import java.util.List;

import com.ferreusveritas.dynamictrees.util.SafeChunkBounds;

import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;

public interface IPostGenFeature extends IGenModule {
	
	/**
	 * Do post generation operations
	 * 
	 * @param world The world
	 * @param rootPos The position of the rooty dirt block
	 * @param biome The biome at this location
	 * @param radius The Poisson disc radius  
	 * @param endPoints A list of branch endpoints
	 * @param safeBounds A safebounds structure for preventing runaway cascading generation
	 * @param initialDirtState The state of the dirt block before tree generation took place
	 * @return true if operation succeeded. false otherwise.
	 */
	boolean postGeneration(World world, BlockPos rootPos, Biome biome, int radius, List<BlockPos> endPoints, SafeChunkBounds safeBounds, IBlockState initialDirtState);
	
}
