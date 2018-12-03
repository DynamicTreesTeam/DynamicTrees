package com.ferreusveritas.dynamictrees.systems.featuregen;

import java.util.ArrayList;
import java.util.List;

import com.ferreusveritas.dynamictrees.api.IPostGenFeature;
import com.ferreusveritas.dynamictrees.trees.Species;
import com.ferreusveritas.dynamictrees.util.SafeChunkBounds;

import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;

public class FeatureGenGroup implements IPostGenFeature {
	
	private List<IPostGenFeature> features = new ArrayList<>();
	
	@Override
	public boolean postGeneration(World world, BlockPos rootPos, Species species, Biome biome, int radius, List<BlockPos> endPoints, SafeChunkBounds safeBounds, IBlockState initialDirtState) {
		for(IPostGenFeature feature : features) {
			feature.postGeneration(world, rootPos, species, biome, radius, endPoints, safeBounds, initialDirtState);
		}
		return true;
	}
	
	public FeatureGenGroup add(IPostGenFeature feature) {
		features.add(feature);
		return this;
	}
	
}
