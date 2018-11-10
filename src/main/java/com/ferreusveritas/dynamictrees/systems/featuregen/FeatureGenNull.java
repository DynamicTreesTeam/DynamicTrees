package com.ferreusveritas.dynamictrees.systems.featuregen;

import java.util.Random;

import com.ferreusveritas.dynamictrees.api.IGenFeature;
import com.ferreusveritas.dynamictrees.util.SafeChunkBounds;

import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;

public class FeatureGenNull implements IGenFeature {

	@Override
	public void generate(World world, BlockPos rootPos, Biome biome, Random random, int radius, SafeChunkBounds safeBounds) {}

}
