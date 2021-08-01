package com.ferreusveritas.dynamictrees.systems.genfeatures;

import com.ferreusveritas.dynamictrees.api.IPostGenFeature;
import com.ferreusveritas.dynamictrees.api.IPostGrowFeature;
import com.ferreusveritas.dynamictrees.api.configurations.ConfigurationProperty;
import com.ferreusveritas.dynamictrees.init.DTTrees;
import com.ferreusveritas.dynamictrees.systems.genfeatures.config.ConfiguredGenFeature;
import com.ferreusveritas.dynamictrees.trees.Species;
import com.ferreusveritas.dynamictrees.util.CoordUtils;
import com.ferreusveritas.dynamictrees.util.SafeChunkBounds;
import net.minecraft.block.BlockState;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IWorld;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;

import java.util.List;

public class RandomPredicateGenFeature extends GenFeature implements IPostGenFeature, IPostGrowFeature {

	public static final ConfigurationProperty<Boolean> ONLY_WORLD_GEN = ConfigurationProperty.bool("only_world_gen");
	public static final ConfigurationProperty<ConfiguredGenFeature<GenFeature>> GEN_FEATURE = ConfigurationProperty.property("gen_feature", ConfiguredGenFeature.NULL_CONFIGURED_FEATURE_CLASS);
	public RandomPredicateGenFeature(ResourceLocation registryName) {
		super(registryName);
	}

	@Override
	protected void registerProperties() {
		this.register(PLACE_CHANCE, GEN_FEATURE, ONLY_WORLD_GEN);
	}

	@Override
	protected ConfiguredGenFeature<GenFeature> createDefaultConfiguration() {
		return super.createDefaultConfiguration()
				.with(PLACE_CHANCE, 0.5f)
				.with(GEN_FEATURE, ConfiguredGenFeature.NULL_CONFIGURED_FEATURE)
				.with(ONLY_WORLD_GEN, false);
	}

	@Override
	public boolean postGeneration(ConfiguredGenFeature<?> configuredGenFeature, IWorld world, BlockPos rootPos, Species species, Biome biome, int radius, List<BlockPos> endPoints, SafeChunkBounds safeBounds, BlockState initialDirtState, Float seasonValue, Float seasonFruitProductionFactor) {
		//If the chance is not met, just return false
		if (Math.abs(CoordUtils.coordHashCode(rootPos, 2)/(float)0xFFFF) > configuredGenFeature.get(PLACE_CHANCE))
			return false;

		final boolean worldGen = safeBounds != SafeChunkBounds.ANY;
		final ConfiguredGenFeature<?> configuredGenFeatureToPlace = configuredGenFeature.get(GEN_FEATURE);

		if (configuredGenFeature.getGenFeature().getRegistryName().equals(DTTrees.NULL)) // If the gen feature was null, do nothing.
			return false;

		final GenFeature genFeatureToPlace = configuredGenFeatureToPlace.getGenFeature();

		if (genFeatureToPlace instanceof IPostGenFeature && !(configuredGenFeature.get(ONLY_WORLD_GEN) && !worldGen)) {
			return ((IPostGenFeature) genFeatureToPlace).postGeneration(configuredGenFeatureToPlace, world, rootPos, species, biome, radius, endPoints, safeBounds, initialDirtState, seasonValue, seasonFruitProductionFactor);
		}

		return false;
	}

	@Override
	public boolean postGrow(ConfiguredGenFeature<?> configuredGenFeature, World world, BlockPos rootPos, BlockPos treePos, Species species, int fertility, boolean natural) {
		//If the chance is not met, or its only for world gen, just return false
		if (configuredGenFeature.get(ONLY_WORLD_GEN)
				|| Math.abs(CoordUtils.coordHashCode(rootPos, 2)/(float)0xFFFF) > configuredGenFeature.get(PLACE_CHANCE))
			return false;

		final ConfiguredGenFeature<?> configuredGenFeatureToPlace = configuredGenFeature.get(GEN_FEATURE);

		if (configuredGenFeature.getGenFeature().getRegistryName().equals(DTTrees.NULL)) // If the gen feature was null, do nothing.
			return false;

		final GenFeature genFeatureToPlace = configuredGenFeatureToPlace.getGenFeature();

		if (genFeatureToPlace instanceof IPostGrowFeature) {
			return ((IPostGrowFeature) genFeatureToPlace).postGrow(configuredGenFeatureToPlace, world, rootPos, treePos, species, fertility, natural);
		}

		return false;
	}
}
