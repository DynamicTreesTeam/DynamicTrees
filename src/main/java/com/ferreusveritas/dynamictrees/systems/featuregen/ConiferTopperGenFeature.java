package com.ferreusveritas.dynamictrees.systems.featuregen;

import com.ferreusveritas.dynamictrees.api.IPostGenFeature;
import com.ferreusveritas.dynamictrees.api.treedata.ILeavesProperties;
import com.ferreusveritas.dynamictrees.trees.Species;
import com.ferreusveritas.dynamictrees.util.SafeChunkBounds;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IWorld;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;

import java.util.Collections;
import java.util.List;

public class ConiferTopperGenFeature implements IPostGenFeature {
	
	protected final ILeavesProperties leavesProperties;
	
	public ConiferTopperGenFeature(ILeavesProperties leavesProperties) {
		this.leavesProperties = leavesProperties;
	}
	
	@Override
	public boolean postGeneration(IWorld world, BlockPos rootPos, Species species, Biome biome, int radius, List<BlockPos> endPoints, SafeChunkBounds safeBounds, BlockState initialDirtState) {
		//Manually place the highest few blocks of the conifer since the leafCluster voxmap won't handle it
		BlockPos highest = Collections.max(endPoints, (a, b) -> a.getY() - b.getY());
		world.setBlockState(highest.up(1), leavesProperties.getDynamicLeavesState(4), 3);
		world.setBlockState(highest.up(2), leavesProperties.getDynamicLeavesState(3), 3);
		world.setBlockState(highest.up(3), leavesProperties.getDynamicLeavesState(1), 3);

		return true;
	}
	
}
