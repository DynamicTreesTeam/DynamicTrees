package com.ferreusveritas.dynamictrees.systems.nodemappers;

import com.ferreusveritas.dynamictrees.api.TreeHelper;
import com.ferreusveritas.dynamictrees.api.network.INodeInspector;
import com.ferreusveritas.dynamictrees.blocks.BlockBranch;
import com.ferreusveritas.dynamictrees.blocks.BlockDynamicLeaves;
import com.ferreusveritas.dynamictrees.trees.Species;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class NodeTransform implements INodeInspector {
	
	Species fromSpecies;
	Species toSpecies;
	
	public NodeTransform(Species fromTree, Species toTree) {
		this.fromSpecies = fromTree;
		this.toSpecies = toTree;
	}
	
	@Override
	public boolean run(World world, Block block, BlockPos pos, EnumFacing fromDir) {
		BlockBranch branch = TreeHelper.getBranch(block);
		
		if(branch != null && fromSpecies.getTree() == branch.getTree()) {
			int radius = branch.getRadius(world, pos);
			if(radius > 0) {
				world.setBlockState(pos, toSpecies.getTree().getDynamicBranch().getDefaultState().withProperty(BlockBranch.RADIUS, radius));
				if(radius == 1) {
					transformSurroundingLeaves(world, pos);
				}
			}
		}
		
		return true;
	}
	
	@Override
	public boolean returnRun(World world, Block block, BlockPos pos, EnumFacing fromDir) {
		return false;
	}
	
	public void transformSurroundingLeaves(World world, BlockPos twigPos) {
		if (!world.isRemote) {
			for(BlockPos leavesPos : BlockPos.getAllInBox(twigPos.add(-3, -3, -3), twigPos.add(3, 3, 3))) {
				if(fromSpecies.getLeavesProperties().getCellKit().getLeafCluster().getVoxel(twigPos, leavesPos) != 0) {//We're only interested in where leaves could possibly be
					if(fromSpecies.getTree().isCompatibleGenericLeaves(world, leavesPos)) {
						IBlockState state = world.getBlockState(leavesPos);
						int hydro = state.getBlock() instanceof BlockDynamicLeaves ? state.getValue(BlockDynamicLeaves.HYDRO) : 2;
						world.setBlockState(leavesPos, toSpecies.getLeavesProperties().getDynamicLeavesState(hydro));
					}
				}
			}
		}
	}
	
}
