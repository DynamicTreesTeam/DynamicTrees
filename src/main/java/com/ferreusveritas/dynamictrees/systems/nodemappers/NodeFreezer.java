package com.ferreusveritas.dynamictrees.systems.nodemappers;

import com.ferreusveritas.dynamictrees.api.TreeHelper;
import com.ferreusveritas.dynamictrees.api.network.INodeInspector;
import com.ferreusveritas.dynamictrees.blocks.BlockBranch;
import com.ferreusveritas.dynamictrees.trees.DynamicTree;

import net.minecraft.block.Block;
import net.minecraft.block.BlockLeaves;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;


public class NodeFreezer implements INodeInspector {

	@Override
	public boolean run(World world, Block block, BlockPos pos, EnumFacing fromDir) {
		BlockBranch branch = TreeHelper.getBranch(block);
		if(branch != null) {
			int radius = branch.getRadius(world, pos);
			if(radius == 1) {
				freezeSurroundingLeaves(world, branch, pos);
			}
		}

		return true;
	}

	@Override
	public boolean returnRun(World world, Block block, BlockPos pos, EnumFacing fromDir) {
		return false;
	}

	//Clumsy hack to freeze leaves
	public void freezeSurroundingLeaves(World world, BlockBranch branch, BlockPos twigPos) {		
		if (!world.isRemote && !world.restoringBlockSnapshots) { // do not drop items while restoring blockstates, prevents item dupe
			DynamicTree tree = branch.getTree();
			IBlockState primLeaves = tree.getPrimitiveLeaves();
			for(BlockPos leavesPos : BlockPos.getAllInBox(twigPos.add(-3, -3, -3), twigPos.add(3, 3, 3))) {
				if(tree.isCompatibleGenericLeaves(world, leavesPos)) {
					world.setBlockState(leavesPos, primLeaves.withProperty(BlockLeaves.DECAYABLE, false).withProperty(BlockLeaves.CHECK_DECAY, false));
				}
			}
		}
	}

}
