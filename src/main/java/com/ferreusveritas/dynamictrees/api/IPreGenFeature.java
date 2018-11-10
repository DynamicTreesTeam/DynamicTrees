package com.ferreusveritas.dynamictrees.api;

import com.ferreusveritas.dynamictrees.util.SafeChunkBounds;
import com.ferreusveritas.dynamictrees.worldgen.JoCode;

import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public interface IPreGenFeature extends IGenFeature {
	
	/**
	 * Do pre generation operations
	 * 
	 * @param world The world
	 * @param rootPos The position of the rooty dirt
	 * @param radius The Poisson disc radius  
	 * @param facing The facing direction that will be applied to the JoCode during generation
	 * @param safeBounds A safebounds structure for preventing runaway cascading generation
	 * @param joCode The JoCode that will be used to generate this tree
	 * @return The modified position of the rooty dirt
	 */
	public BlockPos preGeneration(World world, BlockPos rootPos, int radius, EnumFacing facing, SafeChunkBounds safeBounds, JoCode joCode);
	
}
