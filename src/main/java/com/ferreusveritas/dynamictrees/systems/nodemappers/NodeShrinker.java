package com.ferreusveritas.dynamictrees.systems.nodemappers;

import com.ferreusveritas.dynamictrees.api.TreeHelper;
import com.ferreusveritas.dynamictrees.api.network.INodeInspector;
import com.ferreusveritas.dynamictrees.blocks.BlockBranch;
import com.ferreusveritas.dynamictrees.trees.Species;
import net.minecraft.block.BlockState;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class NodeShrinker implements INodeInspector {
	
	private float radius;
	Species species;
	
	public NodeShrinker(Species species) {
		this.species = species;
	}
	
	@Override
	public boolean run(BlockState blockState, World world, BlockPos pos, Direction fromDir) {
		
		BlockBranch branch = TreeHelper.getBranch(blockState);
		
		if(branch != null) {
			radius = branch.getRadius(blockState);
			if(radius > BlockBranch.RADMAX_NORMAL) {
				branch.setRadius(world, pos, BlockBranch.RADMAX_NORMAL, fromDir);
			}
		}
		
		return false;
	}
	
	@Override
	public boolean returnRun(BlockState blockState, World world, BlockPos pos, Direction fromDir) {
		return false;
	}
	
}
