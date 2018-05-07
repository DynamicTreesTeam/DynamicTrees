package com.ferreusveritas.dynamictrees.systems.featuregen;

import java.util.List;

import com.ferreusveritas.dynamictrees.api.IGenFeature;
import com.ferreusveritas.dynamictrees.trees.Species;
import com.ferreusveritas.dynamictrees.util.CoordUtils;
import com.ferreusveritas.dynamictrees.util.MathHelper;
import com.ferreusveritas.dynamictrees.util.SafeChunkBounds;

import net.minecraft.block.Block;
import net.minecraft.block.BlockVine;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockPos.MutableBlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;

public class FeatureGenVine implements IGenFeature {

	protected final PropertyBool vineMap[] = new PropertyBool[] {null, null, BlockVine.NORTH, BlockVine.SOUTH, BlockVine.WEST, BlockVine.EAST};
	protected int qty = 4;
	protected int maxLength = 8;
	protected float verSpread = 60;
	protected float rayDistance = 5;
	protected Species species;
	protected Block vineBlock = Blocks.VINE;
	
	public FeatureGenVine(Species species) {
		this.species = species;
	}
	
	public FeatureGenVine setQuantity(int qty) {
		this.qty = qty;
		return this;
	}
	
	public FeatureGenVine setMaxLength(int length) {
		this.maxLength = length;
		return this;
	}
	
	public FeatureGenVine setVerSpread(float verSpread) {
		this.verSpread = verSpread;
		return this;
	}
	
	public FeatureGenVine setRayDistance(float rayDistance) {
		this.rayDistance = rayDistance;
		return this;
	}
	
	public FeatureGenVine setVineBlock(Block vineBlock) {
		this.vineBlock = vineBlock;
		return this;
	}
	
	@Override
	public void gen(World world, BlockPos treePos, List<BlockPos> endPoints, SafeChunkBounds safeBounds) {
		if(!endPoints.isEmpty()) {
			for(int i = 0; i < qty; i++) {
				BlockPos endPoint = endPoints.get(world.rand.nextInt(endPoints.size()));
				addVine(world, species, treePos, endPoint, safeBounds);
			}
		}
	}
	
	protected void addVine(World world, Species species, BlockPos treePos, BlockPos branchPos, SafeChunkBounds safeBounds) {
		
		RayTraceResult result = CoordUtils.branchRayTrace(world, species, treePos, branchPos, 90, verSpread, rayDistance, safeBounds);
		
		if(result != null) {
			BlockPos vinePos = result.getBlockPos().offset(result.sideHit);
			if(vinePos != BlockPos.ORIGIN && safeBounds.inBounds(vinePos, true)) {
				PropertyBool vineSide = vineMap[result.sideHit.getOpposite().getIndex()];
				if(vineSide != null) {
					IBlockState vineState = vineBlock.getDefaultState().withProperty(vineSide, Boolean.valueOf(true));
					int len = MathHelper.clamp(world.rand.nextInt(maxLength) + 3, 3, maxLength);
					MutableBlockPos mPos = new MutableBlockPos(vinePos);
					for(int i = 0; i < len; i++) {
						if(world.isAirBlock(mPos)) {
							world.setBlockState(mPos, vineState);
							mPos.setY(mPos.getY() - 1);
						} else {
							break;
						}
					}
				}
			}
		}
	}
	
}
