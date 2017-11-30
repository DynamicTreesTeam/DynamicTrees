package com.ferreusveritas.dynamictrees.inspectors;

import com.ferreusveritas.dynamictrees.api.TreeHelper;
import com.ferreusveritas.dynamictrees.api.network.INodeInspector;
import com.ferreusveritas.dynamictrees.blocks.BlockBranch;
import com.ferreusveritas.dynamictrees.trees.DynamicTree;

import net.minecraft.block.Block;
import com.ferreusveritas.dynamictrees.api.backport.IBlockState;
import com.ferreusveritas.dynamictrees.api.backport.WorldDec;
import com.ferreusveritas.dynamictrees.api.backport.EnumFacing;
import com.ferreusveritas.dynamictrees.api.backport.BlockPos;


public class NodeFreezer implements INodeInspector {

	@Override
	public boolean run(WorldDec world, Block block, BlockPos pos, EnumFacing fromDir) {
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
	public boolean returnRun(WorldDec world, Block block, BlockPos pos, EnumFacing fromDir) {
		return false;
	}

	//Clumsy hack to freeze leaves
	public void freezeSurroundingLeaves(WorldDec world, BlockBranch branch, BlockPos twigPos) {
		int noDecayBits = 0x04;
		if (!world.isRemote() && !world.restoringBlockSnapshots()) { // do not drop items while restoring blockstates, prevents item dupe
			DynamicTree tree = branch.getTree();
			IBlockState primLeaves = tree.getPrimitiveLeaves();
			for(BlockPos leavesPos : BlockPos.getAllInBox(twigPos.add(-3, -3, -3), twigPos.add(3, 3, 3))) {
				//if(tree.getLeafClusterPoint(twigPos, leavesPos) != 0) {//We're only interested in where leaves could possibly be
					if(tree.isCompatibleGenericLeaves(world, leavesPos)) {
						world.setBlockState(leavesPos, primLeaves.withMeta(primLeaves.getMeta() | noDecayBits));
					}
				//}
			}
		}
	}

}
