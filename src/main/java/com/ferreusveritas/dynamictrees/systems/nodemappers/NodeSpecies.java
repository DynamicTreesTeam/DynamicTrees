package com.ferreusveritas.dynamictrees.systems.nodemappers;

import com.ferreusveritas.dynamictrees.api.TreeHelper;
import com.ferreusveritas.dynamictrees.api.network.INodeInspector;
import com.ferreusveritas.dynamictrees.api.treedata.ITreePart;
import com.ferreusveritas.dynamictrees.trees.Species;
import net.minecraft.block.BlockState;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IWorld;

public class NodeSpecies implements INodeInspector {
	
	private Species determination = Species.NULL_SPECIES;
	
	@Override
	public boolean run(BlockState blockState, IWorld world, BlockPos pos, Direction fromDir) {
		
		ITreePart treePart = TreeHelper.getTreePart(blockState);

		switch(treePart.getTreePartType()) {
			case BRANCH:
				if(determination == Species.NULL_SPECIES) {
					determination = TreeHelper.getBranch(treePart).getFamily().getCommonSpecies();
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
	public boolean returnRun(BlockState blockState, IWorld world, BlockPos pos, Direction fromDir) {
		return false;
	}
	
	public Species getSpecies() {
		return determination;
	}
	
}
