package com.ferreusveritas.dynamictrees.systems.genfeatures;

import com.ferreusveritas.dynamictrees.api.IPostGenFeature;
import com.ferreusveritas.dynamictrees.api.IPostGrowFeature;
import com.ferreusveritas.dynamictrees.api.TreeHelper;
import com.ferreusveritas.dynamictrees.api.network.MapSignal;
import com.ferreusveritas.dynamictrees.blocks.FruitBlock;
import com.ferreusveritas.dynamictrees.blocks.branches.BranchBlock;
import com.ferreusveritas.dynamictrees.compat.seasons.SeasonHelper;
import com.ferreusveritas.dynamictrees.init.DTRegistries;
import com.ferreusveritas.dynamictrees.systems.genfeatures.config.GenFeatureProperty;
import com.ferreusveritas.dynamictrees.systems.genfeatures.config.ConfiguredGenFeature;
import com.ferreusveritas.dynamictrees.systems.nodemappers.NodeFindEnds;
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

public class FruitGenFeature extends GenFeature implements IPostGrowFeature, IPostGenFeature {

	public static final GenFeatureProperty<FruitBlock> FRUIT_BLOCK = GenFeatureProperty.createProperty("fruit_block", FruitBlock.class);
	public static final GenFeatureProperty<Integer> FRUITING_RADIUS = GenFeatureProperty.createIntegerProperty("fruiting_radius");

	public FruitGenFeature(ResourceLocation registryName) {
		super(registryName, FRUIT_BLOCK, VERTICAL_SPREAD, QUANTITY, RAY_DISTANCE, FRUITING_RADIUS);
	}

	@Override
	public ConfiguredGenFeature<?> createDefaultConfiguration() {
		return super.createDefaultConfiguration().with(FRUIT_BLOCK, DTRegistries.appleBlock)
				.with(VERTICAL_SPREAD, 30f).with(QUANTITY, 4).with(RAY_DISTANCE, 5f).with(FRUITING_RADIUS, 8);
	}

    @Override
	public boolean postGeneration(ConfiguredGenFeature<?> configuredGenFeature, IWorld world, BlockPos rootPos, Species species, Biome biome, int radius, List<BlockPos> endPoints, SafeChunkBounds safeBounds, BlockState initialDirtState, Float seasonValue, Float seasonFruitProductionFactor) {
		if(!endPoints.isEmpty()) {
			int qty = configuredGenFeature.get(QUANTITY);
			qty *= seasonFruitProductionFactor;
			for(int i = 0; i < qty; i++) {
				BlockPos endPoint = endPoints.get(world.getRandom().nextInt(endPoints.size()));
				addFruit(configuredGenFeature, world, species, rootPos.up(), endPoint, true, false, safeBounds, seasonValue);
			}
			return true;
		}
		return false;
	}

	@Override
	public boolean postGrow(ConfiguredGenFeature<?> configuredGenFeature, World world, BlockPos rootPos, BlockPos treePos, Species species, int soilLife, boolean natural) {
		BlockState blockState = world.getBlockState(treePos);
		BranchBlock branch = TreeHelper.getBranch(blockState);

		if(branch != null && branch.getRadius(blockState) >= configuredGenFeature.get(FRUITING_RADIUS) && natural) {
			if (species.seasonalFruitProductionFactor(world, rootPos) > world.rand.nextFloat()) {
				NodeFindEnds endFinder = new NodeFindEnds();
				TreeHelper.startAnalysisFromRoot(world, rootPos, new MapSignal(endFinder));
				List<BlockPos> endPoints = endFinder.getEnds();
				int qty = configuredGenFeature.get(QUANTITY);
				if (!endPoints.isEmpty()) {
					for (int i = 0; i < qty; i++) {
						BlockPos endPoint = endPoints.get(world.rand.nextInt(endPoints.size()));
						addFruit(configuredGenFeature, world, species, rootPos.up(), endPoint, false, true, SafeChunkBounds.ANY, SeasonHelper.getSeasonValue(world, rootPos));
					}
				}
			}
		}

		return true;
	}

	protected void addFruit(ConfiguredGenFeature<?> configuredGenFeature, IWorld world, Species species, BlockPos treePos, BlockPos branchPos, boolean worldGen, boolean enableHash, SafeChunkBounds safeBounds, Float seasonValue) {
		BlockPos fruitPos = CoordUtils.getRayTraceFruitPos(world, species, treePos, branchPos, safeBounds);
		if(fruitPos != BlockPos.ZERO) {
			if (!enableHash || ((CoordUtils.coordHashCode(fruitPos, 0) & 3) == 0) ) {
				FruitBlock fruitBlock = configuredGenFeature.get(FRUIT_BLOCK);
				BlockState setState = fruitBlock.getStateForAge(worldGen ? fruitBlock.getAgeForSeasonalWorldGen(world, fruitPos, seasonValue) : 0);
				world.setBlockState(fruitPos, setState, 3);
			}
		}
	}

}