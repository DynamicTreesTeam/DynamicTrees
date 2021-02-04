package com.ferreusveritas.dynamictrees.systems.featuregen;

import com.ferreusveritas.dynamictrees.api.IPostGenFeature;
import com.ferreusveritas.dynamictrees.trees.Species;
import com.ferreusveritas.dynamictrees.util.SafeChunkBounds;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IWorld;
import net.minecraft.world.biome.Biome;

import java.util.List;
import java.util.function.Predicate;

public class PredicateGenFeature implements IPostGenFeature {

	private Predicate<Biome> biomePredicate = i -> true;
	private final IPostGenFeature feature;
	private boolean onlyWorldGen = false;
	
	public PredicateGenFeature(IPostGenFeature feature) {
		this.feature = feature;
	}
	
	public PredicateGenFeature onlyWorldGen(boolean onlyWorldGen) {
		this.onlyWorldGen = onlyWorldGen;
		return this;
	}
	
	public PredicateGenFeature setBiomePredicate(Predicate<Biome> biomePredicate) {
		this.biomePredicate = biomePredicate;
		return this;
	}
	
	@Override
	public boolean postGeneration(IWorld world, BlockPos rootPos, Species species, Biome biome, int radius, List<BlockPos> endPoints, SafeChunkBounds safeBounds, BlockState initialDirtState, float seasonValue, float seasonFruitProductionFactor) {
		boolean worldGen = safeBounds != SafeChunkBounds.ANY;
		if(!(onlyWorldGen && !worldGen) && biomePredicate.test(biome)) {
			return feature.postGeneration(world, rootPos, species, biome, radius, endPoints, safeBounds, initialDirtState, seasonValue, seasonFruitProductionFactor);
		}
		return false;
	}
	
}
