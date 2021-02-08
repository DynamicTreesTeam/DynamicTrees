package com.ferreusveritas.dynamictrees.systems.featuregen;

import com.ferreusveritas.dynamictrees.api.IPostGenFeature;
import com.ferreusveritas.dynamictrees.api.IPostGrowFeature;
import com.ferreusveritas.dynamictrees.api.TreeHelper;
import com.ferreusveritas.dynamictrees.api.network.MapSignal;
import com.ferreusveritas.dynamictrees.blocks.branches.BranchBlock;
import com.ferreusveritas.dynamictrees.blocks.FruitBlock;
import com.ferreusveritas.dynamictrees.compat.seasons.SeasonHelper;
import com.ferreusveritas.dynamictrees.systems.nodemappers.NodeFindEnds;
import com.ferreusveritas.dynamictrees.trees.Species;
import com.ferreusveritas.dynamictrees.util.CoordUtils;
import com.ferreusveritas.dynamictrees.util.SafeChunkBounds;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IWorld;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;

import java.util.List;

public class FruitGenFeature implements IPostGrowFeature, IPostGenFeature {

	protected final FruitBlock fruitBlock;
	protected final BlockState ripeFruitState;
	protected final BlockState unripeFruitState;
	protected float verSpread = 30;
	protected int qty = 4;
	protected float rayDistance = 5;
	protected int fruitingRadius = 8;

	public FruitGenFeature(BlockState unripeFruitState, BlockState ripeFruitState) {
		this.ripeFruitState = ripeFruitState;
		this.unripeFruitState = unripeFruitState;
		this.fruitBlock = null;
	}

	public FruitGenFeature(FruitBlock fruit) {
		this.ripeFruitState = null;
		this.unripeFruitState = null;
		this.fruitBlock = fruit;
	}

	public FruitGenFeature setRayDistance(float rayDistance) {
		this.rayDistance = rayDistance;
		return this;
	}

	public FruitGenFeature setFruitingRadius(int fruitingRadius) {
		this.fruitingRadius = fruitingRadius;
		return this;
	}

	public int getQuantity(boolean worldGen) {
		return worldGen ? 10 : 1;
	}

    @Override
	public boolean postGeneration(IWorld world, BlockPos rootPos, Species species, Biome biome, int radius, List<BlockPos> endPoints, SafeChunkBounds safeBounds, BlockState initialDirtState, Float seasonValue, Float seasonFruitProductionFactor) {
		if(!endPoints.isEmpty()) {
			int qty = getQuantity(true);
			qty *= seasonFruitProductionFactor;
			for(int i = 0; i < qty; i++) {
				BlockPos endPoint = endPoints.get(world.getRandom().nextInt(endPoints.size()));
				addFruit(world, species, rootPos.up(), endPoint, true, false, safeBounds, seasonValue);
			}
			return true;
		}
		return false;
	}

	@Override
	public boolean postGrow(World world, BlockPos rootPos, BlockPos treePos, Species species, int soilLife, boolean natural) {
		BlockState blockState = world.getBlockState(treePos);
		BranchBlock branch = TreeHelper.getBranch(blockState);

		if(branch != null && branch.getRadius(blockState) >= fruitingRadius && natural) {
			if (species.seasonalFruitProductionFactor(world, rootPos) > world.rand.nextFloat()) {
				NodeFindEnds endFinder = new NodeFindEnds();
				TreeHelper.startAnalysisFromRoot(world, rootPos, new MapSignal(endFinder));
				List<BlockPos> endPoints = endFinder.getEnds();
				int qty = getQuantity(false);
				if (!endPoints.isEmpty()) {
					for (int i = 0; i < qty; i++) {
						BlockPos endPoint = endPoints.get(world.rand.nextInt(endPoints.size()));
						addFruit(world, species, rootPos.up(), endPoint, false, true, SafeChunkBounds.ANY, SeasonHelper.getSeasonValue(world, rootPos));
					}
				}
			}
		}

		return true;
	}

	protected void addFruit(IWorld world, Species species, BlockPos treePos, BlockPos branchPos, boolean worldGen, boolean enableHash, SafeChunkBounds safeBounds, Float seasonValue) {
		BlockPos fruitPos = CoordUtils.getRayTraceFruitPos(world, species, treePos, branchPos, safeBounds);
		if(fruitPos != BlockPos.ZERO) {
			if ( !enableHash || ( (CoordUtils.coordHashCode(fruitPos, 0) & 3) == 0) ) {
				BlockState setState;
				if(fruitBlock != null) {
					setState = fruitBlock.getStateForAge(worldGen ? fruitBlock.getAgeForSeasonalWorldGen(world, fruitPos, seasonValue) : 0);
				} else {
					setState = worldGen ? ripeFruitState : unripeFruitState;
				}
				world.setBlockState(fruitPos, setState, 3);
			}
		}
	}

}