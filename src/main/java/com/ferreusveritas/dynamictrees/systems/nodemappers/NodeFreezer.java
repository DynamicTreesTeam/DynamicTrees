package com.ferreusveritas.dynamictrees.systems.nodemappers;

import com.ferreusveritas.dynamictrees.api.TreeHelper;
import com.ferreusveritas.dynamictrees.api.network.INodeInspector;
import com.ferreusveritas.dynamictrees.blocks.BlockBranch;
import com.ferreusveritas.dynamictrees.trees.Species;
import com.ferreusveritas.dynamictrees.trees.TreeFamily;
import net.minecraft.block.BlockState;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;


public class NodeFreezer implements INodeInspector {
	
	Species species;
	
	public NodeFreezer(Species species) {
		this.species = species;
	}
	
	@Override
	public boolean run(BlockState blockState, World world, BlockPos pos, Direction fromDir) {
		BlockBranch branch = TreeHelper.getBranch(blockState);
		if(branch != null) {
			int radius = branch.getRadius(blockState);
			if(radius == 1) {
				freezeSurroundingLeaves(world, branch, pos);
			}
		}
		
		return true;
	}
	
	@Override
	public boolean returnRun(BlockState blockState, World world, BlockPos pos, Direction fromDir) {
		return false;
	}
	
	//Clumsy hack to freeze leaves
	public void freezeSurroundingLeaves(World world, BlockBranch branch, BlockPos twigPos) {
//		if (!world.isRemote && !world.restoringBlockSnapshots) { // do not drop items while restoring blockstates, prevents item dupe
//			TreeFamily tree = branch.getFamily();
//			BlockState primLeaves = species.getLeavesProperties().getPrimitiveLeaves();
//			for(BlockPos leavesPos : BlockPos.getAllInBox(twigPos.add(-3, -3, -3), twigPos.add(3, 3, 3))) {
//				if(tree.isCompatibleGenericLeaves(world.getBlockState(leavesPos), world, leavesPos)) {
//					world.setBlockState(leavesPos, primLeaves.withProperty(BlockLeaves.DECAYABLE, false).withProperty(BlockLeaves.CHECK_DECAY, false));
//				}
//			}
//		}
	}
	
}
