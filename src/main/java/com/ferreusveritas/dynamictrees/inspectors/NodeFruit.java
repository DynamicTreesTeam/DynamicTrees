package com.ferreusveritas.dynamictrees.inspectors;

import com.ferreusveritas.dynamictrees.api.network.INodeInspector;
import com.ferreusveritas.dynamictrees.trees.DynamicTree;

import net.minecraft.block.Block;
import com.ferreusveritas.dynamictrees.api.backport.EnumFacing;
import com.ferreusveritas.dynamictrees.api.backport.WorldDec;
import com.ferreusveritas.dynamictrees.api.backport.BlockPos;

public class NodeFruit implements INodeInspector {

	DynamicTree tree;

	public NodeFruit(DynamicTree tree) {
		this.tree = tree;
	}

	@Override
	public boolean run(WorldDec world, Block block, BlockPos pos, EnumFacing fromDir) {
		return false;
	}

	@Override
	public boolean returnRun(WorldDec world, Block block, BlockPos pos, EnumFacing fromDir) {
		return false;
	}

}
