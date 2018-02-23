package com.ferreusveritas.dynamictrees.systems.nodemappers;

import com.ferreusveritas.dynamictrees.api.TreeHelper;
import com.ferreusveritas.dynamictrees.api.network.INodeInspector;
import com.ferreusveritas.dynamictrees.blocks.BlockBranch;
import com.ferreusveritas.dynamictrees.trees.DynamicTree;
import com.ferreusveritas.dynamictrees.trees.Species;

import net.minecraft.block.BlockLeaves;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;


public class NodeFreezer implements INodeInspector {

	Species species;

	public NodeFreezer(Species species) {
		this.species = species;
	}
	
	@Override
	public boolean run(IBlockState blockState, World world, BlockPos pos, EnumFacing fromDir) {
		BlockBranch branch = TreeHelper.getBranch(blockState);
		if(branch != null) {
			int radius = branch.getRadius(blockState, world, pos);
			if(radius == 1) {
				freezeSurroundingLeaves(world, branch, pos);
			}
		}

		return true;
	}

	@Override
	public boolean returnRun(IBlockState blockState, World world, BlockPos pos, EnumFacing fromDir) {
		return false;
	}

	//Clumsy hack to freeze leaves
	public void freezeSurroundingLeaves(World world, BlockBranch branch, BlockPos twigPos) {		
		if (!world.isRemote && !world.restoringBlockSnapshots) { // do not drop items while restoring blockstates, prevents item dupe
			DynamicTree tree = branch.getTree();
			IBlockState primLeaves = species.getLeavesProperties().getPrimitiveLeaves();
			for(BlockPos leavesPos : BlockPos.getAllInBox(twigPos.add(-3, -3, -3), twigPos.add(3, 3, 3))) {
				if(tree.isCompatibleGenericLeaves(world.getBlockState(leavesPos), world, leavesPos)) {
					world.setBlockState(leavesPos, primLeaves.withProperty(BlockLeaves.DECAYABLE, false).withProperty(BlockLeaves.CHECK_DECAY, false));
				}
			}
		}
	}

}
