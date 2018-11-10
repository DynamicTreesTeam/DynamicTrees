package com.ferreusveritas.dynamictrees.systems.featuregen;

import java.util.List;
import java.util.function.Predicate;

import com.ferreusveritas.dynamictrees.api.IPostGenFeature;
import com.ferreusveritas.dynamictrees.util.SafeChunkBounds;

import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;

public class FeatureGenPredicate implements IPostGenFeature {

	private Predicate<Biome> biomePredicate = i -> true;
	private final IPostGenFeature feature;
	private boolean onlyWorldGen = false;
	
	public FeatureGenPredicate(IPostGenFeature feature) {
		this.feature = feature;
	}
	
	public FeatureGenPredicate onlyWorldGen(boolean onlyWorldGen) {
		this.onlyWorldGen = onlyWorldGen;
		return this;
	}
	
	public FeatureGenPredicate setBiomePredicate(Predicate<Biome> biomePredicate) {
		this.biomePredicate = biomePredicate;
		return this;
	}
	
	@Override
	public boolean postGeneration(World world, BlockPos rootPos, Biome biome, int radius, List<BlockPos> endPoints, SafeChunkBounds safeBounds, IBlockState initialDirtState) {
		boolean worldGen = safeBounds != SafeChunkBounds.ANY;
		if(!(onlyWorldGen && !worldGen) && biomePredicate.test(biome)) {
			return feature.postGeneration(world, rootPos, biome, radius, endPoints, safeBounds, initialDirtState);
		}
		return false;
	}
	
}
