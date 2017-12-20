package com.ferreusveritas.dynamictrees.api;

import java.util.List;

import com.ferreusveritas.dynamictrees.api.backport.BlockPos;
import com.ferreusveritas.dynamictrees.api.backport.World;


public interface IGenFeature {

	public void gen(World world, BlockPos treePos, List<BlockPos> endPoints);
	
}
