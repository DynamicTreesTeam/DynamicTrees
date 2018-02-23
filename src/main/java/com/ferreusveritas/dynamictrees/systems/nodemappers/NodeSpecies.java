package com.ferreusveritas.dynamictrees.systems.nodemappers;

import com.ferreusveritas.dynamictrees.api.TreeHelper;
import com.ferreusveritas.dynamictrees.api.network.INodeInspector;
import com.ferreusveritas.dynamictrees.api.treedata.ITreePart;
import com.ferreusveritas.dynamictrees.trees.Species;

import net.minecraft.block.Block;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class NodeSpecies implements INodeInspector {

	private Species determination = Species.NULLSPECIES;

	@Override
	public boolean run(World world, Block block, BlockPos pos, EnumFacing fromDir) {

		ITreePart treePart = TreeHelper.getTreePart(world.getBlockState(pos));

		switch(treePart.getTreePartType()) {
			case BRANCH:
				if(determination == Species.NULLSPECIES) {
					determination = TreeHelper.getBranch(treePart).getTree().getCommonSpecies();
				}
				break;
			case ROOT:
				determination = TreeHelper.getRooty(treePart).getSpecies(world.getBlockState(pos), world, pos);
				break;
			default:
				break;
		}

		return true;
	}

	@Override
	public boolean returnRun(World world, Block block, BlockPos pos, EnumFacing fromDir) {
		return false;
	}

	public Species getSpecies() {
		return determination;
	}

}
