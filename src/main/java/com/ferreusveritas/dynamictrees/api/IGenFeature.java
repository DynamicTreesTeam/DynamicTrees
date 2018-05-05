package com.ferreusveritas.dynamictrees.api;

import java.util.List;

import com.ferreusveritas.dynamictrees.util.SafeChunkBounds;

import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public interface IGenFeature {

	public void gen(World world, BlockPos treePos, List<BlockPos> endPoints, SafeChunkBounds safeBounds);
	
}
