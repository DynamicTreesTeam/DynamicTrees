package com.ferreusveritas.dynamictrees.inspectors;

import com.ferreusveritas.dynamictrees.api.network.INodeInspector;
import com.ferreusveritas.dynamictrees.trees.ISpecies;

import net.minecraft.block.Block;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class NodeFruit implements INodeInspector {

	ISpecies species;

	public NodeFruit(ISpecies species) {
		this.species = species;
	}

	@Override
	public boolean run(World world, Block block, BlockPos pos, EnumFacing fromDir) {
		return false;
	}

	@Override
	public boolean returnRun(World world, Block block, BlockPos pos, EnumFacing fromDir) {
		return false;
	}

}
