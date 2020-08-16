package com.ferreusveritas.dynamictrees.api;

import com.ferreusveritas.dynamictrees.trees.Species;
import com.ferreusveritas.dynamictrees.util.SafeChunkBounds;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;

import java.util.Random;

public interface IFullGenFeature extends IGenFeature {

	public boolean generate(World world, BlockPos rootPos, Species species, Biome biome, Random random, int radius, SafeChunkBounds safeBounds);

}
