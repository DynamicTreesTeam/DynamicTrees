package com.ferreusveritas.dynamictrees.systems.featuregen;

import com.ferreusveritas.dynamictrees.ModBlocks;
import com.ferreusveritas.dynamictrees.trees.Species;
import com.ferreusveritas.dynamictrees.util.SafeChunkBounds;
import com.ferreusveritas.dynamictrees.util.SimpleVoxmap;
import com.ferreusveritas.dynamictrees.util.SimpleVoxmap.Cell;

import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class FeatureGenMound {
	
	private static SimpleVoxmap moundMap = new SimpleVoxmap(5, 4, 5, new byte[] {
			0, 0, 0, 0, 0, 0, 2, 2, 2, 0, 0, 2, 2, 2, 0, 0, 2, 2, 2, 0, 0, 0, 0, 0, 0,
			0, 2, 2, 2, 0, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 0, 2, 2, 2, 0,
			0, 1, 1, 1, 0, 1, 2, 2, 2, 1, 1, 2, 2, 2, 1, 1, 2, 2, 2, 1, 0, 1, 1, 1, 0,
			0, 0, 0, 0, 0, 0, 1, 1, 1, 0, 0, 1, 1, 1, 0, 0, 1, 1, 1, 0, 0, 0, 0, 0, 0
		}).setCenter(new BlockPos(2, 3, 2));
	
	public FeatureGenMound(Species species) { }
	
	public BlockPos gen(World world, BlockPos rootPos, SafeChunkBounds safeBounds) {
		
		IBlockState initialDirtState = world.getBlockState(rootPos);
		IBlockState initialUnderState = world.getBlockState(rootPos.down());
		
		if(initialUnderState.getMaterial() != Material.GROUND || initialUnderState.getMaterial() != Material.ROCK) {
			initialUnderState = ModBlocks.blockStates.dirt;
		}
		
		rootPos = rootPos.up();
		
		for(Cell cell: moundMap.getAllNonZeroCells()) {
			IBlockState placeState = cell.getValue() == 1 ? initialDirtState : initialUnderState;
			world.setBlockState(rootPos.add(cell.getPos()), placeState);
		}
		
		return rootPos;
	}
	
}
