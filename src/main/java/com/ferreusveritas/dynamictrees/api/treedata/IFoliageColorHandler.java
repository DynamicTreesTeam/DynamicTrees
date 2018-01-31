package com.ferreusveritas.dynamictrees.api.treedata;

import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;

public interface IFoliageColorHandler {

	int foliageColorMultiplier(IBlockState state, IBlockAccess world, BlockPos pos);
	
}
