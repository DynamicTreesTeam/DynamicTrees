package com.ferreusveritas.dynamictrees.inspectors;

import com.ferreusveritas.dynamictrees.api.TreeHelper;
import com.ferreusveritas.dynamictrees.api.network.INodeInspector;
import com.ferreusveritas.dynamictrees.blocks.BlockBranch;
import com.ferreusveritas.dynamictrees.trees.DynamicTree;
import com.ferreusveritas.dynamictrees.util.CompatHelper;

import net.minecraft.block.Block;
import net.minecraft.entity.item.EntityItem;
import com.ferreusveritas.dynamictrees.api.backport.EnumFacing;
import com.ferreusveritas.dynamictrees.api.backport.World;
import com.ferreusveritas.dynamictrees.api.backport.BlockPos;

/**
* Destroys all branches on a tree and the surrounding leaves.
* @author ferreusveritas
*/
public class NodeDestroyer implements INodeInspector {

	DynamicTree tree;//Destroy any node that's made of the same kind of wood

	public NodeDestroyer(DynamicTree tree) {
		this.tree = tree;
	}

	@Override
	public boolean run(World world, Block block, BlockPos pos, EnumFacing fromDir) {
		BlockBranch branch = TreeHelper.getBranch(block);

		if(branch != null && tree == branch.getTree()) {
			if(branch.getRadius(world, pos) == 1) {
				killSurroundingLeaves(world, pos);//Destroy the surrounding leaves
			}
			world.setBlockToAir(pos);//Destroy the branch
		}

		return true;
	}

	@Override
	public boolean returnRun(World world, Block block, BlockPos pos, EnumFacing fromDir) {
		return false;
	}

	public void killSurroundingLeaves(World world, BlockPos twigPos) {
		if (!world.isRemote() && !world.restoringBlockSnapshots()) { // do not drop items while restoring blockstates, prevents item dupe
			for(BlockPos leavesPos : BlockPos.getAllInBox(twigPos.add(-3, -3, -3), twigPos.add(3, 3, 3))) {
				//if(tree.getLeafClusterPoint(twigPos, leavesPos) != 0) {//We're only interested in where leaves could possibly be
					if(tree.isCompatibleGenericLeaves(world, leavesPos)) {
						world.setBlockToAir(leavesPos);
						int qty = tree.getDynamicLeaves().quantitySeedDropped(world.rand);
						if(qty > 0) {
							EntityItem itemEntity = new EntityItem(world.real(), leavesPos.getX() + 0.5, leavesPos.getY() + 0.5, leavesPos.getZ() + 0.5, tree.getSeedStack(qty));
							CompatHelper.spawnEntity(world, itemEntity);
						}
					}
				//}
			}
		}
	}

}
