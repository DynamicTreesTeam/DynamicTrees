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

public class TransformNode implements INodeInspector {
	
	private final Species fromSpecies;
	private final Species toSpecies;
	
	public TransformNode(Species fromTree, Species toTree) {
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

	private static final int TEST_LEAVES_RADIUS = 3;
	
	public void transformSurroundingLeaves(IWorld world, BlockPos twigPos) {
		if (!world.isClientSide()) {
			BlockPos.betweenClosedStream(twigPos.offset(-TEST_LEAVES_RADIUS, -TEST_LEAVES_RADIUS, -TEST_LEAVES_RADIUS), twigPos.offset(TEST_LEAVES_RADIUS, TEST_LEAVES_RADIUS, TEST_LEAVES_RADIUS)).forEach(leavesPos -> {
				if (fromSpecies.getLeavesProperties().getCellKit().getLeafCluster().getVoxel(twigPos, leavesPos) != 0) {//We're only interested in where leaves could possibly be
					BlockState state = world.getBlockState(leavesPos);
					if (fromSpecies.getFamily().isCompatibleGenericLeaves(state, world, leavesPos)) {
						int hydro = state.getBlock() instanceof DynamicLeavesBlock ? state.getValue(DynamicLeavesBlock.DISTANCE) : 2;
						world.setBlock(leavesPos, toSpecies.getLeavesProperties().getDynamicLeavesState(hydro), 3);
					}
				}
			});
		}
	}
	
}
