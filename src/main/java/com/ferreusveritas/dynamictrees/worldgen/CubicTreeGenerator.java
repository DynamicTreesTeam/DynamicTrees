package com.ferreusveritas.dynamictrees.worldgen;

import java.util.Random;

import com.ferreusveritas.dynamictrees.api.WorldGenRegistry;
import com.ferreusveritas.dynamictrees.util.SafeChunkBounds;
import com.ferreusveritas.dynamictrees.worldgen.BiomeDataBase.BiomeEntry;

import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.World;
import net.minecraft.world.chunk.IChunkProvider;
import net.minecraft.world.gen.IChunkGenerator;

public class CubicTreeGenerator extends TreeGenerator { //TODO: Implement CubicChunkGenerator interface
	
	public static void preInit() {
		if(WorldGenRegistry.isWorldGenEnabled()) {
			INSTANCE = new CubicTreeGenerator();
		}
	}
	
	public static CubicTreeGenerator getWorldGenerator() {
		return (CubicTreeGenerator) INSTANCE;
	}
	
	@Override
	protected BlockPos findGround(BiomeEntry biomeEntry, World world, BlockPos start) {
		return BlockPos.ORIGIN;
	}
	
	//TODO: Change to appropriate function for the cubic chunks generation interface
	//@Override
	public void generate(Random randomUnused, int chunkX, int chunkZ, World world, IChunkGenerator chunkGenerator, IChunkProvider chunkProvider) {
		BiomeDataBase dbase = getBiomeDataBase(world.provider.getDimension());
		if(dbase != BLACKLISTED) {
			random.setXOR(new BlockPos(chunkX, 0, chunkZ));
			SafeChunkBounds safeBounds = new SafeChunkBounds(world, new ChunkPos(chunkX, chunkZ));//Area that is safe to place blocks during worldgen
			circleMan.getCircles(world, random, chunkX, chunkZ).forEach(c -> makeTree(world, dbase, c, safeBounds));
		}
	}
	
}
