package com.ferreusveritas.dynamictrees.api;

import java.util.Random;

import com.ferreusveritas.dynamictrees.util.SafeChunkBounds;

import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;

public interface IGenFeature {

	public void gen(World world, BlockPos rootPos, Biome biome, Random random, int radius, SafeChunkBounds safeBounds);
	
}
