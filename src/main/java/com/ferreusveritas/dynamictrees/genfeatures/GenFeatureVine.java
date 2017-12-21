package com.ferreusveritas.dynamictrees.genfeatures;

import java.util.List;

import com.ferreusveritas.dynamictrees.api.IGenFeature;
import com.ferreusveritas.dynamictrees.api.backport.BlockPos;
import com.ferreusveritas.dynamictrees.api.backport.BlockState;
import com.ferreusveritas.dynamictrees.api.backport.IBlockState;
import com.ferreusveritas.dynamictrees.api.backport.PropertyInteger;
import com.ferreusveritas.dynamictrees.api.backport.RayTraceResult;
import com.ferreusveritas.dynamictrees.api.backport.World;
import com.ferreusveritas.dynamictrees.trees.Species;
import com.ferreusveritas.dynamictrees.util.MathHelper;

import net.minecraft.init.Blocks;

public class GenFeatureVine implements IGenFeature {

	PropertyInteger SOUTH = PropertyInteger.create("south", 0, 1, PropertyInteger.Bits.B000X);
	PropertyInteger WEST  = PropertyInteger.create("west",  0, 1, PropertyInteger.Bits.B00X0);
	PropertyInteger NORTH = PropertyInteger.create("north", 0, 1, PropertyInteger.Bits.B0X00);
	PropertyInteger EAST  = PropertyInteger.create("east",  0, 1, PropertyInteger.Bits.BX000);
	
	protected final PropertyInteger vineMap[] = new PropertyInteger[] {null, null, NORTH, SOUTH, WEST, EAST};
	protected int qty = 4;
	protected int maxLength = 8;
	protected float verSpread = 60;
	protected float rayDistance = 5;
	protected Species species;
	
	public GenFeatureVine(Species species) {
		this.species = species;
	}
	
	public GenFeatureVine setQuantity(int qty) {
		this.qty = qty;
		return this;
	}
	
	public GenFeatureVine setMaxLength(int length) {
		this.maxLength = length;
		return this;
	}
	
	public GenFeatureVine setVerSpread(float verSpread) {
		this.verSpread = verSpread;
		return this;
	}
	
	public GenFeatureVine setRayDistance(float rayDistance) {
		this.rayDistance = rayDistance;
		return this;
	}
	
	@Override
	public void gen(World world, BlockPos treePos, List<BlockPos> endPoints) {
		if(!endPoints.isEmpty()) {
			for(int i = 0; i < qty; i++) {
				BlockPos endPoint = endPoints.get(world.rand.nextInt(endPoints.size()));
				addVine(world, species, treePos, endPoint);
			}
		}
	}
	
	protected void addVine(World world, Species species, BlockPos treePos, BlockPos branchPos) {
		
		RayTraceResult result = species.branchRayTrace(world, treePos, branchPos, 90, verSpread, rayDistance);
		
		if(result != null) {
			BlockPos vinePos = result.getBlockPos().offset(result.sideHit);
			PropertyInteger vineSide = vineMap[result.sideHit.getOpposite().getIndex()];
			if(vineSide != null) {
				IBlockState vineState = new BlockState(Blocks.vine).withProperty(vineSide, 1);
				int len = MathHelper.clamp(world.rand.nextInt(maxLength) + 3, 3, maxLength);
				for(int i = 0; i < len; i++) {
					if(world.isAirBlock(vinePos)) {
						world.setBlockState(vinePos, vineState);
						vinePos = vinePos.down();
					} else {
						break;
					}
				}
			}
		}
	}
	
	public static int coordHashCode(BlockPos pos) {
		int hash = (pos.getX() * 4111 ^ pos.getY() * 271 ^ pos.getZ() * 3067) >> 1;
		return hash & 0xFFFF;
	}
	
}
