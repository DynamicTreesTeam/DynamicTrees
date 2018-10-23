package com.ferreusveritas.dynamictrees.worldgen;

import com.ferreusveritas.dynamictrees.api.worldgen.IGroundFinder;
import com.ferreusveritas.dynamictrees.worldgen.BiomeDataBase.BiomeEntry;

import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class CubicGeneratorTrees { //TODO: Implement CubicChunkGenerator interface
	
	public static class GroundFinder implements IGroundFinder {
		@Override
		public BlockPos findGround(BiomeEntry biomeEntry, World world, BlockPos start) {
			return BlockPos.ORIGIN;
		}
	}
	
	//TODO: Change to appropriate function for the cubic chunks generation interface
	public void generate(World world, int chunkX, int chunkY, int chunkZ) { }
	
}
