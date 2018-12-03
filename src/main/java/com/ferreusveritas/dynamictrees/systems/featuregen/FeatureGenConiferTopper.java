package com.ferreusveritas.dynamictrees.systems.featuregen;

import java.util.Collections;
import java.util.List;

import com.ferreusveritas.dynamictrees.api.IPostGenFeature;
import com.ferreusveritas.dynamictrees.api.treedata.ILeavesProperties;
import com.ferreusveritas.dynamictrees.trees.Species;
import com.ferreusveritas.dynamictrees.util.SafeChunkBounds;

import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;

public class FeatureGenConiferTopper implements IPostGenFeature {
	
	protected final ILeavesProperties leavesProperties;
	
	public FeatureGenConiferTopper(ILeavesProperties leavesProperties) {
		this.leavesProperties = leavesProperties;
	}
	
	@Override
	public boolean postGeneration(World world, BlockPos rootPos, Species species, Biome biome, int radius, List<BlockPos> endPoints, SafeChunkBounds safeBounds, IBlockState initialDirtState) {
		//Manually place the highest few blocks of the conifer since the leafCluster voxmap won't handle it
		BlockPos highest = Collections.max(endPoints, (a, b) -> a.getY() - b.getY());
		world.setBlockState(highest.up(1), leavesProperties.getDynamicLeavesState(4));
		world.setBlockState(highest.up(2), leavesProperties.getDynamicLeavesState(3));
		world.setBlockState(highest.up(3), leavesProperties.getDynamicLeavesState(1));

		return true;
	}
	
}
