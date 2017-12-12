package com.ferreusveritas.dynamictrees.special;

import java.util.List;

import com.ferreusveritas.dynamictrees.api.IGenFeature;

import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class GenFeatureNull implements IGenFeature {

	@Override
	public void gen(World world, BlockPos treePos, List<BlockPos> endPoints) {}

}
