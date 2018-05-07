package com.ferreusveritas.dynamictrees.systems.featuregen;

import java.util.List;

import com.ferreusveritas.dynamictrees.api.IGenFeature;
import com.ferreusveritas.dynamictrees.trees.Species;
import com.ferreusveritas.dynamictrees.util.CoordUtils;
import com.ferreusveritas.dynamictrees.util.SafeChunkBounds;

import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class FeatureGenFruit implements IGenFeature {

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
	public void gen(World world, BlockPos treePos, List<BlockPos> endPoints, SafeChunkBounds safeBounds) {
		if(!endPoints.isEmpty()) {
			for(int i = 0; i < qty; i++) {
				BlockPos endPoint = endPoints.get(world.rand.nextInt(endPoints.size()));
				addFruit(world, species, treePos, endPoint, safeBounds);
			}
		}
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