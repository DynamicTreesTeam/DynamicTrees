package com.ferreusveritas.dynamictrees.systems.nodemappers;

import com.ferreusveritas.dynamictrees.api.TreeHelper;
import com.ferreusveritas.dynamictrees.api.network.INodeInspector;
import com.ferreusveritas.dynamictrees.blocks.BlockBranch;
import com.ferreusveritas.dynamictrees.blocks.BlockRooty;
import com.ferreusveritas.dynamictrees.trees.Species;

import net.minecraft.block.Block;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class NodeSpecies implements INodeInspector {

	private Species determination = Species.NULLSPECIES;

	@Override
	public boolean run(World world, Block block, BlockPos pos, EnumFacing fromDir) {

		BlockRooty rootyBlock = TreeHelper.getRooty(block);
		if(rootyBlock != null) {
			determination = rootyBlock.getSpecies(world.getBlockState(pos), world, pos);
		}
		
		if(determination == Species.NULLSPECIES) {
			BlockBranch branchBlock = TreeHelper.getBranch(block);
			if(branchBlock != null) {
				determination = branchBlock.getTree().getCommonSpecies();
			}
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
