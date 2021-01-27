package com.ferreusveritas.dynamictrees.systems.nodemappers;

import com.ferreusveritas.dynamictrees.api.TreeHelper;
import com.ferreusveritas.dynamictrees.api.network.INodeInspector;
import com.ferreusveritas.dynamictrees.blocks.BlockBranch;
import com.ferreusveritas.dynamictrees.blocks.BlockDynamicLeaves;
import com.ferreusveritas.dynamictrees.trees.Species;

import com.ferreusveritas.dynamictrees.trees.TreeCactus;
import com.ferreusveritas.dynamictrees.trees.TreeFamily;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class NodeTransform implements INodeInspector {
	
	Species fromSpecies;
	Species toSpecies;
	TreeFamily toFamily;
	IBlockState toSpeciesLeaves;
	
	public NodeTransform(Species fromSpecies, Species toSpecies) {
		this.fromSpecies = fromSpecies;
		this.toSpecies = toSpecies;
		this.toFamily = toSpecies.getFamily();
		this.toSpeciesLeaves = toSpecies.getLeavesProperties().getDynamicLeavesState();
	}
	
	@Override
	public boolean run(IBlockState blockState, World world, BlockPos pos, EnumFacing fromDir) {
		BlockBranch branch = TreeHelper.getBranch(blockState);
		
		if(branch != null && fromSpecies.getFamily() == branch.getFamily()) {
			int radius = branch.getRadius(blockState);
			if(radius > 0) {
				toFamily.getDynamicBranch().setRadius(world, pos, radius, null);
					
				if(radius == 1) {
					transformSurroundingLeaves(world, pos);
				}
			}
		}
		
		return true;
	}
	
	@Override
	public boolean returnRun(IBlockState blockState, World world, BlockPos pos, EnumFacing fromDir) {
		return false;
	}
	
	public void transformSurroundingLeaves(World world, BlockPos twigPos) {
		if (!world.isRemote) {
			for(BlockPos leavesPos : BlockPos.getAllInBox(twigPos.add(-3, -3, -3), twigPos.add(3, 3, 3))) {
				if(fromSpecies.getLeavesProperties().getCellKit().getLeafCluster().getVoxel(twigPos, leavesPos) != 0) {//We're only interested in where leaves could possibly be
					IBlockState state = world.getBlockState(leavesPos);
					if(fromSpecies.getFamily().isCompatibleGenericLeaves(state, world, leavesPos)) {
						int hydro = state.getBlock() instanceof BlockDynamicLeaves ? state.getValue(BlockDynamicLeaves.HYDRO) : 2;
						world.setBlockState(leavesPos, toSpeciesLeaves == null ? Blocks.AIR.getDefaultState() : toSpeciesLeaves.withProperty(BlockDynamicLeaves.HYDRO, hydro));
					}
				}
			}
		}
	}
	
}
