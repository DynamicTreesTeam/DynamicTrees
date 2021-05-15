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
import com.ferreusveritas.dynamictrees.systems.nodemappers.FindEndsNode;
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

	public FruitGenFeature(ResourceLocation registryName) {
		super(registryName, FRUIT_BLOCK, VERTICAL_SPREAD, QUANTITY, RAY_DISTANCE, FRUITING_RADIUS, PLACE_CHANCE);
	}

	@Override
	public ConfiguredGenFeature<GenFeature> createDefaultConfiguration() {
		return super.createDefaultConfiguration().with(FRUIT_BLOCK, DTRegistries.APPLE_FRUIT)
				.with(VERTICAL_SPREAD, 30f).with(QUANTITY, 4).with(RAY_DISTANCE, 5f).with(FRUITING_RADIUS, 8).with(PLACE_CHANCE, 1f);
	}

    @Override
	public boolean postGeneration(ConfiguredGenFeature<?> configuredGenFeature, IWorld world, BlockPos rootPos, Species species, Biome biome, int radius, List<BlockPos> endPoints, SafeChunkBounds safeBounds, BlockState initialDirtState, Float seasonValue, Float seasonFruitProductionFactor) {
		if(!endPoints.isEmpty()) {
			int qty = configuredGenFeature.get(QUANTITY);
			qty *= seasonFruitProductionFactor;
			for(int i = 0; i < qty; i++) {
				BlockPos endPoint = endPoints.get(world.getRandom().nextInt(endPoints.size()));
				addFruit(configuredGenFeature, world, species, rootPos.above(), endPoint, true, false, safeBounds, seasonValue);
			}
			return true;
		}
		return false;
	}

	@Override
	public boolean postGrow(ConfiguredGenFeature<?> configuredGenFeature, World world, BlockPos rootPos, BlockPos treePos, Species species, int fertility, boolean natural) {
		BlockState blockState = world.getBlockState(treePos);
		BranchBlock branch = TreeHelper.getBranch(blockState);

		if(branch != null && branch.getRadius(blockState) >= configuredGenFeature.get(FRUITING_RADIUS) && natural) {
			if (species.seasonalFruitProductionFactor(world, rootPos) > world.random.nextFloat()) {
				FindEndsNode endFinder = new FindEndsNode();
				TreeHelper.startAnalysisFromRoot(world, rootPos, new MapSignal(endFinder));
				List<BlockPos> endPoints = endFinder.getEnds();
				int qty = configuredGenFeature.get(QUANTITY);
				if (!endPoints.isEmpty()) {
					for (int i = 0; i < qty; i++) {
						BlockPos endPoint = endPoints.get(world.random.nextInt(endPoints.size()));
						addFruit(configuredGenFeature, world, species, rootPos.above(), endPoint, false, true, SafeChunkBounds.ANY, SeasonHelper.getSeasonValue(world, rootPos));
					}
				}
			}
		}

		return true;
	}

	protected void addFruit(ConfiguredGenFeature<?> configuredGenFeature, IWorld world, Species species, BlockPos treePos, BlockPos branchPos, boolean worldGen, boolean enableHash, SafeChunkBounds safeBounds, Float seasonValue) {
		BlockPos fruitPos = CoordUtils.getRayTraceFruitPos(world, species, treePos, branchPos, safeBounds);
		if(fruitPos != BlockPos.ZERO &&
			!enableHash || ((CoordUtils.coordHashCode(fruitPos, 0) & 3) == 0) &&
			world.getRandom().nextFloat() <= configuredGenFeature.get(PLACE_CHANCE)) {
				FruitBlock fruitBlock = configuredGenFeature.get(FRUIT_BLOCK);
				BlockState setState = fruitBlock.getStateForAge(worldGen ? fruitBlock.getAgeForSeasonalWorldGen(world, fruitPos, seasonValue) : 0);
				world.setBlock(fruitPos, setState, 3);
		}
	}

}