package com.ferreusveritas.dynamictrees.systems.featuregen;

import java.util.List;

import com.ferreusveritas.dynamictrees.api.IPostGenFeature;
import com.ferreusveritas.dynamictrees.api.IPostGrowFeature;
import com.ferreusveritas.dynamictrees.api.TreeHelper;
import com.ferreusveritas.dynamictrees.api.network.MapSignal;
import com.ferreusveritas.dynamictrees.blocks.BlockBranch;
import com.ferreusveritas.dynamictrees.systems.nodemappers.NodeFindEnds;
import com.ferreusveritas.dynamictrees.trees.Species;
import com.ferreusveritas.dynamictrees.util.CoordUtils;
import com.ferreusveritas.dynamictrees.util.SafeChunkBounds;

import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;

public class FeatureGenFruit implements IPostGrowFeature, IPostGenFeature {

	protected Species species;
	protected IBlockState fruitState;
	protected float verSpread = 30;
	protected int qty = 4;
	protected float rayDistance = 5;
	protected boolean enableHash = true;
	
	public FeatureGenFruit(Species species, IBlockState fruitState) {
		this.species = species;
		setFruit(fruitState);
	}
	
	public FeatureGenFruit setFruit(IBlockState fruitState) {
		this.fruitState = fruitState;
		return this;
	}
	
	public FeatureGenFruit setQuantity(int qty) {
		this.qty = qty;
		return this;
	}
	
	public FeatureGenFruit setRayDistance(float rayDistance) {
		this.rayDistance = rayDistance;
		return this;
	}
	
	public FeatureGenFruit setEnableHash(boolean doHash) {
		this.enableHash = doHash;
		return this;
	}
	
	@Override
	public boolean postGeneration(World world, BlockPos rootPos, Biome biome, int radius, List<BlockPos> endPoints, SafeChunkBounds safeBounds, IBlockState initialDirtState) {
		if(!endPoints.isEmpty()) {
			for(int i = 0; i < qty; i++) {
				BlockPos endPoint = endPoints.get(world.rand.nextInt(endPoints.size()));
				addFruit(world, species, rootPos.up(), endPoint, safeBounds);
			}
			return true;
		}
		return false;
	}
	
	@Override
	public boolean postGrow(World world, BlockPos rootPos, BlockPos treePos, int soilLife, boolean natural) {
		IBlockState blockState = world.getBlockState(treePos);
		BlockBranch branch = TreeHelper.getBranch(blockState);
		
		if(branch != null && branch.getRadius(blockState) >= 8 && natural) {
			NodeFindEnds endFinder = new NodeFindEnds();
			TreeHelper.startAnalysisFromRoot(world, rootPos, new MapSignal(endFinder));
			List<BlockPos> endPoints = endFinder.getEnds();
			if(!endPoints.isEmpty()) {
				for(int i = 0; i < qty; i++) {
					BlockPos endPoint = endPoints.get(world.rand.nextInt(endPoints.size()));
					addFruit(world, species, rootPos.up(), endPoint, SafeChunkBounds.ANY);
				}
			}		
		}
		
		return true;
	}
	
	protected void addFruit(World world, Species species, BlockPos treePos, BlockPos branchPos, SafeChunkBounds safeBounds) {
		BlockPos fruitPos = CoordUtils.getRayTraceFruitPos(world, species, treePos, branchPos, safeBounds);
		if(fruitPos != BlockPos.ORIGIN) {
			if ( !enableHash || ( (CoordUtils.coordHashCode(fruitPos, 0) & 3) == 0) ) {
				world.setBlockState(fruitPos, fruitState);
			}
		}
	}
	
}