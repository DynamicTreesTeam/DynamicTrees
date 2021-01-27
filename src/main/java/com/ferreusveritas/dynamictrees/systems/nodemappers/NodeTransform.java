package com.ferreusveritas.dynamictrees.systems.nodemappers;

import com.ferreusveritas.dynamictrees.api.TreeHelper;
import com.ferreusveritas.dynamictrees.api.network.INodeInspector;
import com.ferreusveritas.dynamictrees.blocks.branches.BranchBlock;
import com.ferreusveritas.dynamictrees.blocks.leaves.DynamicLeavesBlock;
import com.ferreusveritas.dynamictrees.trees.Species;

import net.minecraft.block.BlockState;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IWorld;
import net.minecraft.world.World;

public class NodeTransform implements INodeInspector {
	
	Species fromSpecies;
	Species toSpecies;
	
	public NodeTransform(Species fromTree, Species toTree) {
		this.fromSpecies = fromTree;
		this.toSpecies = toTree;
	}
	
	@Override
	public boolean run(BlockState blockState, IWorld world, BlockPos pos, Direction fromDir) {
		BranchBlock branch = TreeHelper.getBranch(blockState);
		
		if(branch != null && fromSpecies.getFamily() == branch.getFamily()) {
			int radius = branch.getRadius(blockState);
			if(radius > 0) {
				BranchBlock newBranchBlock = toSpecies.getFamily().getDynamicBranch();

				// If the branch is stripped, make the replacement branch stripped.
				if (fromSpecies.getFamily().getDynamicStrippedBranch().equals(branch)) {
					newBranchBlock = toSpecies.getFamily().getDynamicStrippedBranch();
				}

				newBranchBlock.setRadius(world, pos, radius, null);
				if(radius == 1) {
					transformSurroundingLeaves(world, pos);
				}
			}
		}
		
		return true;
	}
	
	@Override
	public boolean returnRun(BlockState blockState, IWorld world, BlockPos pos, Direction fromDir) {
		return false;
	}
	
	public void transformSurroundingLeaves(IWorld world, BlockPos twigPos) {
		if (!world.isRemote()) {
			BlockPos.getAllInBox(twigPos.add(-3, -3, -3), twigPos.add(3, 3, 3)).forEach(leavesPos -> {
				if(fromSpecies.getLeavesProperties().getCellKit().getLeafCluster().getVoxel(twigPos, leavesPos) != 0) {//We're only interested in where leaves could possibly be
					BlockState state = world.getBlockState(leavesPos);
					if(fromSpecies.getFamily().isCompatibleGenericLeaves(state, world, leavesPos)) {
						int hydro = state.getBlock() instanceof DynamicLeavesBlock ? state.get(DynamicLeavesBlock.DISTANCE) : 2;
						world.setBlockState(leavesPos, toSpecies.getLeavesProperties().getDynamicLeavesState(hydro), 3);
					}
				}
			});
		}
	}
	
}
