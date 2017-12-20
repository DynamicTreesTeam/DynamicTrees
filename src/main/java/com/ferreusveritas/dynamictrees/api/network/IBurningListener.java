package com.ferreusveritas.dynamictrees.api.network;

import com.ferreusveritas.dynamictrees.api.backport.BlockPos;
import com.ferreusveritas.dynamictrees.api.backport.IBlockState;

import net.minecraft.world.World;

public interface IBurningListener {

	public void onBurned(World world, IBlockState oldState, BlockPos burnedPos);
	
}
