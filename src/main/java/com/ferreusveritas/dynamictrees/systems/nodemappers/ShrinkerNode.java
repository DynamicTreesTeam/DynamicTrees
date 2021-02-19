package com.ferreusveritas.dynamictrees.systems.nodemappers;

import com.ferreusveritas.dynamictrees.api.TreeHelper;
import com.ferreusveritas.dynamictrees.api.network.INodeInspector;
import com.ferreusveritas.dynamictrees.blocks.branches.BranchBlock;
import com.ferreusveritas.dynamictrees.trees.Species;
import net.minecraft.block.BlockState;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IWorld;
import net.minecraft.world.World;

public class ShrinkerNode implements INodeInspector {
	
	private float radius;
	Species species;
	
	public ShrinkerNode(Species species) {
		this.species = species;
	}
	
	@Override
	public boolean run(BlockState blockState, IWorld world, BlockPos pos, Direction fromDir) {
		
		BranchBlock branch = TreeHelper.getBranch(blockState);
		
		if(branch != null) {
			radius = branch.getRadius(blockState);
			if(radius > BranchBlock.RADMAX_NORMAL) {
				branch.setRadius(world, pos, BranchBlock.RADMAX_NORMAL, fromDir);
			}
		}
		
		return false;
	}
	
	@Override
	public boolean returnRun(BlockState blockState, IWorld world, BlockPos pos, Direction fromDir) {
		return false;
	}
	
}
