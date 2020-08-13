package com.ferreusveritas.dynamictrees.systems.nodemappers;

import com.ferreusveritas.dynamictrees.api.TreeHelper;
import com.ferreusveritas.dynamictrees.api.network.INodeInspector;
import com.ferreusveritas.dynamictrees.blocks.BlockBranch;
import com.ferreusveritas.dynamictrees.trees.Species;
import net.minecraft.block.BlockState;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

/**
* Destroys all thin(radius == 1) branches on a tree.. leaving it to rot.
* @author ferreusveritas
*/
public class NodeDisease implements INodeInspector {
	
	Species species;//Destroy any thin branches made of the same kind of wood.
	
	public NodeDisease(Species tree) {
		this.species = tree;
	}
	
	@Override
	public boolean run(BlockState blockState, World world, BlockPos pos, Direction fromDir) {
		BlockBranch branch = TreeHelper.getBranch(blockState);
		
		if(branch != null && species.getFamily() == branch.getFamily()) {
			if(branch.getRadius(blockState) == 1) {
				world.removeBlock(pos, false);//Destroy the thin branch
			}
		}
		
		return true;
	}
	
	@Override
	public boolean returnRun(BlockState blockState, World world, BlockPos pos, Direction fromDir) {
		return false;
	}
	
}
