package com.ferreusveritas.growingtrees.inspectors;

import com.ferreusveritas.growingtrees.GrowingTrees;
import com.ferreusveritas.growingtrees.trees.GrowingTree;

import net.minecraft.block.Block;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;

public class NodeFruit implements INodeInspector {

	GrowingTree tree;//Destroy any thin branches made of the same kind of wood.

	public NodeFruit(GrowingTree tree) {
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
