package com.ferreusveritas.dynamictrees.systems.nodemappers;

import com.ferreusveritas.dynamictrees.api.TreeHelper;
import com.ferreusveritas.dynamictrees.api.network.INodeInspector;
import com.ferreusveritas.dynamictrees.blocks.branches.BranchBlock;
import com.ferreusveritas.dynamictrees.util.CoordUtils;
import net.minecraft.block.BlockState;
import net.minecraft.util.Direction;
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
	
	public boolean run(BlockState blockState, World world, BlockPos pos, Direction fromDir) {
		
		if(!finished) {
			int hashCode = CoordUtils.coordHashCode(pos, 1);
			if((hashCode % 97) % 29 == 0) {
				BranchBlock branch = TreeHelper.getBranch(blockState);
				if(branch != null && branch.getRadius(blockState) == 8) {
					int side = (hashCode % 4) + 2;
					Direction dir = Direction.byIndex(side);
					BlockPos deltaPos = pos.offset(dir);
//					if (world.isAirBlock(deltaPos)) {
//						BlockState cocoaState = ModRegistries.blockFruitCocoa.getStateForPlacement(world, deltaPos, dir, 0, 0, 0, 0, null);
//						world.setBlockState(deltaPos, cocoaState.withProperty(BlockCocoa.AGE, worldGen ? 2 : 0), 2);
//					}
				} else {
					finished = true;
				}
			}
		}
		return false;
	}
	
	@Override
	public boolean returnRun(BlockState blockState, World world, BlockPos pos, Direction fromDir) {
		return false;
	}

}
