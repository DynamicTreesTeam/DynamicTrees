package com.ferreusveritas.dynamictrees.inspectors;

import com.ferreusveritas.dynamictrees.api.TreeHelper;
import com.ferreusveritas.dynamictrees.api.backport.BlockPos;
import com.ferreusveritas.dynamictrees.api.backport.EnumFacing;
import com.ferreusveritas.dynamictrees.api.backport.WorldDec;
import com.ferreusveritas.dynamictrees.api.network.INodeInspector;
import com.ferreusveritas.dynamictrees.blocks.BlockBranch;
import com.ferreusveritas.dynamictrees.trees.DynamicTree;

import net.minecraft.block.Block;

/**
* Destroys all thin(radius == 1) branches on a tree.. leaving it to rot.
* @author ferreusveritas
*/
public class NodeDisease implements INodeInspector {

	DynamicTree tree;//Destroy any thin branches made of the same kind of wood.

	public NodeDisease(DynamicTree tree) {
		this.tree = tree;
	}

	@Override
	public boolean run(WorldDec world, Block block, BlockPos pos, EnumFacing fromDir) {
		BlockBranch branch = TreeHelper.getBranch(block);
		
		if(branch != null && tree == branch.getTree()) {
			if(branch.getRadius(world, pos) == 1) {
				world.setBlockToAir(pos);//Destroy the thin branch
			}
		}

		return true;
	}

	@Override
	public boolean returnRun(WorldDec world, Block block, BlockPos pos, EnumFacing fromDir) {
		return false;
	}

}
