package com.ferreusveritas.dynamictrees.api.worldgen;

import com.ferreusveritas.dynamictrees.worldgen.BiomeDatabase;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.ISeedReader;

/**
 * Implementations will find a suitable area to generate a tree on the ground.
 */
public interface IGroundFinder {

	/**
	 * Finds the {@link BlockPos} of the first ground block for the y-column of the
	 * start {@link BlockPos} given.
	 *
	 * @param entry The {@link BiomeDatabase.Entry} of the biome for the position.
	 * @param world The {@link ISeedReader} world object.
	 * @param start The {@link BlockPos} to start from.
	 * @return The {@link BlockPos} of the first ground block.
	 */
	BlockPos findGround(BiomeDatabase.Entry entry, ISeedReader world, BlockPos start);

}
