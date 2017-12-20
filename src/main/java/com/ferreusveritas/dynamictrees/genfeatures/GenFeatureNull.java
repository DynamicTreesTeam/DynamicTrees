package com.ferreusveritas.dynamictrees.genfeatures;

import java.util.List;

import com.ferreusveritas.dynamictrees.api.IGenFeature;
import com.ferreusveritas.dynamictrees.api.backport.BlockPos;
import com.ferreusveritas.dynamictrees.api.backport.World;


public class GenFeatureNull implements IGenFeature {

	@Override
	public void gen(World world, BlockPos treePos, List<BlockPos> endPoints) {}

}
