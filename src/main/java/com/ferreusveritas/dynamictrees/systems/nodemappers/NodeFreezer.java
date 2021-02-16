package com.ferreusveritas.dynamictrees.systems.nodemappers;

import com.ferreusveritas.dynamictrees.api.TreeHelper;
import com.ferreusveritas.dynamictrees.api.network.INodeInspector;
import com.ferreusveritas.dynamictrees.blocks.branches.BranchBlock;
import com.ferreusveritas.dynamictrees.trees.Species;
import com.ferreusveritas.dynamictrees.trees.TreeFamily;
import net.minecraft.block.BlockState;
import net.minecraft.block.LeavesBlock;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IWorld;
import net.minecraft.world.World;

public class NodeFreezer implements INodeInspector {
	
	Species species;
	private static final int freezeRadius = 3;
	
	public NodeFreezer(Species species) {
		this.species = species;
	}
	
	@Override
	public boolean run(BlockState blockState, IWorld world, BlockPos pos, Direction fromDir) {
		BranchBlock branch = TreeHelper.getBranch(blockState);
		if(branch != null) {
			int radius = branch.getRadius(blockState);
			if(radius == 1) {
				freezeSurroundingLeaves(world, branch, pos);
			}
		}
		
		return true;
	}
	
	@Override
	public boolean returnRun(BlockState blockState, IWorld world, BlockPos pos, Direction fromDir) {
		return false;
	}
	
	//Clumsy hack to freeze leaves
	public void freezeSurroundingLeaves(IWorld world, BranchBlock branch, BlockPos twigPos) {
		if (!world.isRemote()/* && !world.restoringBlockSnapshots*/) { // do not drop items while restoring blockstates, prevents item dupe
			TreeFamily tree = branch.getFamily();
			BlockState primLeaves = species.getLeavesProperties().getPrimitiveLeaves();
			BlockPos.getAllInBox(twigPos.add(-freezeRadius, -freezeRadius, -freezeRadius), twigPos.add(freezeRadius, freezeRadius, freezeRadius)).forEach(leavesPos -> {
				if (tree.isCompatibleGenericLeaves(world.getBlockState(leavesPos), world, leavesPos)) {
					world.setBlockState(leavesPos, primLeaves.with(LeavesBlock.PERSISTENT, true), 2);
				}
			});
		}
	}
	
}
