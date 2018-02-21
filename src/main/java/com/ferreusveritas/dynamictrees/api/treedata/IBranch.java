package com.ferreusveritas.dynamictrees.api.treedata;

import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

public interface IBranch {
	
	int getRadius(IBlockState blockState, IBlockAccess blockAccess, BlockPos pos);
	
	void setRadius(World world, BlockPos pos, int radius, int flags);
	
	default void setRadius(World world, BlockPos pos, int radius) {
		setRadius(world, pos, radius, 2);
	}	
	
}
