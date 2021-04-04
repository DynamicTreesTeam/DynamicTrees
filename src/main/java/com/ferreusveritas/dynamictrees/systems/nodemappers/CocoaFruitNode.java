package com.ferreusveritas.dynamictrees.systems.nodemappers;

import com.ferreusveritas.dynamictrees.api.TreeHelper;
import com.ferreusveritas.dynamictrees.api.network.INodeInspector;
import com.ferreusveritas.dynamictrees.blocks.branches.BranchBlock;
import com.ferreusveritas.dynamictrees.init.DTRegistries;
import com.ferreusveritas.dynamictrees.util.CoordUtils;
import net.minecraft.block.BlockState;
import net.minecraft.block.CocoaBlock;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IWorld;

public class CocoaFruitNode implements INodeInspector {
	
	private boolean finished = false;
	private boolean worldGen = false;

	public CocoaFruitNode setWorldGen(boolean worldGen) {
		this.worldGen = worldGen;
		return this;
	}

	@Override
	public boolean run(BlockState blockState, IWorld world, BlockPos pos, Direction fromDir) {
		
		if(!finished) {
			int hashCode = CoordUtils.coordHashCode(pos, 1);
			if((hashCode % 97) % 29 == 0) {
				BranchBlock branch = TreeHelper.getBranch(blockState);
				if(branch != null && branch.getRadius(blockState) == 8) {
					int side = (hashCode % 4) + 2;
					Direction dir = Direction.from3DDataValue(side);
					BlockPos deltaPos = pos.relative(dir);
					if (world.isEmptyBlock(deltaPos)) {
						if (!dir.getAxis().isHorizontal()) dir = Direction.NORTH;
						world.setBlock(deltaPos, DTRegistries.COCOA_FRUIT.defaultBlockState().setValue(CocoaBlock.FACING, dir.getOpposite()).setValue(CocoaBlock.AGE, worldGen ? 2 : 0), 2);
					}
				} else {
					finished = true;
				}
			}
		}
		return false;
	}
	
	@Override
	public boolean returnRun(BlockState blockState, IWorld world, BlockPos pos, Direction fromDir) {
		return false;
	}

}
