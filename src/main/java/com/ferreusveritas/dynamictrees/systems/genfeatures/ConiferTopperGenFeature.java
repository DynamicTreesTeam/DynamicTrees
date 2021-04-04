package com.ferreusveritas.dynamictrees.systems.genfeatures;

import com.ferreusveritas.dynamictrees.api.IPostGenFeature;
import com.ferreusveritas.dynamictrees.blocks.leaves.LeavesProperties;
import com.ferreusveritas.dynamictrees.systems.genfeatures.config.ConfiguredGenFeature;
import com.ferreusveritas.dynamictrees.systems.genfeatures.config.GenFeatureProperty;
import com.ferreusveritas.dynamictrees.trees.Species;
import com.ferreusveritas.dynamictrees.util.SafeChunkBounds;
import net.minecraft.block.BlockState;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3i;
import net.minecraft.world.IWorld;
import net.minecraft.world.biome.Biome;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class ConiferTopperGenFeature extends GenFeature implements IPostGenFeature {

	public static final GenFeatureProperty<LeavesProperties> LEAVES_PROPERTIES = GenFeatureProperty.createProperty("leaves_properties", LeavesProperties.class);

	public ConiferTopperGenFeature(ResourceLocation registryName) {
		super(registryName, LEAVES_PROPERTIES);
	}

	@Override
	public ConfiguredGenFeature<GenFeature> createDefaultConfiguration() {
		return super.createDefaultConfiguration().with(LEAVES_PROPERTIES, LeavesProperties.NULL_PROPERTIES);
	}

	@Override
	public boolean postGeneration(ConfiguredGenFeature<?> configuredGenFeature, IWorld world, BlockPos rootPos, Species species, Biome biome, int radius, List<BlockPos> endPoints, SafeChunkBounds safeBounds, BlockState initialDirtState, Float seasonValue, Float seasonFruitProductionFactor) {
		//Manually place the highest few blocks of the conifer since the leafCluster voxmap won't handle it
		BlockPos highest = Collections.max(endPoints, Comparator.comparingInt(Vector3i::getY));
		LeavesProperties leavesProperties = configuredGenFeature.get(LEAVES_PROPERTIES);

		world.setBlock(highest.above(1), leavesProperties.getDynamicLeavesState(4), 3);
		world.setBlock(highest.above(2), leavesProperties.getDynamicLeavesState(3), 3);
		world.setBlock(highest.above(3), leavesProperties.getDynamicLeavesState(1), 3);

		return true;
	}

}
