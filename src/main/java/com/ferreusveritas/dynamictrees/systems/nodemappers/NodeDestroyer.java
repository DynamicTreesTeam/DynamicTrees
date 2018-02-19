package com.ferreusveritas.dynamictrees.systems.nodemappers;

import java.util.ArrayList;

import com.ferreusveritas.dynamictrees.api.TreeHelper;
import com.ferreusveritas.dynamictrees.api.network.INodeInspector;
import com.ferreusveritas.dynamictrees.blocks.BlockBranch;
import com.ferreusveritas.dynamictrees.trees.DynamicTree;
import com.ferreusveritas.dynamictrees.trees.Species;
import com.ferreusveritas.dynamictrees.util.CompatHelper;

import net.minecraft.block.Block;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

/**
* Destroys all branches on a tree and the surrounding leaves.
* @author ferreusveritas
*/
public class NodeDestroyer implements INodeInspector {

	Species species;//Destroy any node that's made of the same kind of wood

	public NodeDestroyer(Species species) {
		this.species = species;
	}

	@Override
	public boolean run(World world, Block block, BlockPos pos, EnumFacing fromDir) {
		BlockBranch branch = TreeHelper.getBranch(block);

		if(branch != null && species.getTree() == branch.getTree()) {
			if(branch.getRadius(world, pos) == species.getPrimaryThickness()) {
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
		if (!world.isRemote && !world.restoringBlockSnapshots) { // do not drop items while restoring blockstates, prevents item dupe
			ArrayList<ItemStack> dropList = new ArrayList<ItemStack>();
			DynamicTree tree = species.getTree();
			for(BlockPos leavesPos : BlockPos.getAllInBox(twigPos.add(-3, -3, -3), twigPos.add(3, 3, 3))) {
				if(tree.isCompatibleGenericLeaves(world, leavesPos)) {
					world.setBlockToAir(leavesPos);
					dropList.clear();
					species.getTreeHarvestDrops(world, leavesPos, dropList, world.rand);
					for(ItemStack stack : dropList) {
						EntityItem itemEntity = new EntityItem(world, leavesPos.getX() + 0.5, leavesPos.getY() + 0.5, leavesPos.getZ() + 0.5, stack);
						CompatHelper.spawnEntity(world, itemEntity);
					}
				}
			}
		}
	}

}
