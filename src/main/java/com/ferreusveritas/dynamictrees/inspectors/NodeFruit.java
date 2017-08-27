package com.ferreusveritas.dynamictrees.inspectors;

import com.ferreusveritas.dynamictrees.DynamicTrees;
import com.ferreusveritas.dynamictrees.trees.DynamicTree;

import net.minecraft.block.Block;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;

public class NodeFruit implements INodeInspector {

	DynamicTree tree;//Destroy any thin branches made of the same kind of wood.

	public NodeFruit(DynamicTree tree) {
		this.tree = tree;
	}

	@Override
	public boolean run(World world, Block block, int x, int y, int z, ForgeDirection fromDir) {
		return false;
	}

	@Override
	public boolean returnRun(World world, Block block, int x, int y, int z, ForgeDirection fromDir) {
		return false;
	}

}
