package com.ferreusveritas.dynamictrees.systems.nodemappers;

import com.ferreusveritas.dynamictrees.ModBlocks;
import com.ferreusveritas.dynamictrees.api.TreeHelper;
import com.ferreusveritas.dynamictrees.api.network.INodeInspector;
import com.ferreusveritas.dynamictrees.blocks.BlockBranch;
import com.ferreusveritas.dynamictrees.util.CoordUtils;

import net.minecraft.block.BlockCocoa;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class NodeFruitCocoa implements INodeInspector {
	
	boolean finished = false;
	boolean worldGen = false;
	
	public NodeFruitCocoa() {
	}
	
	public NodeFruitCocoa setWorldGen(boolean worldGen) {
		this.worldGen = worldGen;
		return this;
	}
	
	public boolean run(IBlockState blockState, World world, BlockPos pos, EnumFacing fromDir) {
		
		if(!finished) {
			int hashCode = CoordUtils.coordHashCode(pos, 1);
			if((hashCode % 97) % 29 == 0) {
				BlockBranch branch = TreeHelper.getBranch(blockState);
				if(branch != null && branch.getRadius(blockState) == 8) {
					int side = (hashCode % 4) + 2;
					EnumFacing dir = EnumFacing.getFront(side);
					BlockPos deltaPos = pos.offset(dir);
					if (world.isAirBlock(deltaPos)) {
						IBlockState cocoaState = ModBlocks.blockFruitCocoa.getStateForPlacement(world, deltaPos, dir, 0, 0, 0, 0, null);
						world.setBlockState(deltaPos, cocoaState.withProperty(BlockCocoa.AGE, worldGen ? 2 : 0), 2);
					}
				} else {
					finished = true;
				}
			}
		}
		return false;
	}
	
	@Override
	public boolean returnRun(IBlockState blockState, World world, BlockPos pos, EnumFacing fromDir) {
		return false;
	}

}
