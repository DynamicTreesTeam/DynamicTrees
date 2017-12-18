package com.ferreusveritas.dynamictrees.api.network;

import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public interface IBurningListener {

	public void onBurned(World world, IBlockState oldState, BlockPos burnedPos);
	
}
