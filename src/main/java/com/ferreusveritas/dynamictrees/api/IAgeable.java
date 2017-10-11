package com.ferreusveritas.dynamictrees.api;

import java.util.Random;

import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public interface IAgeable {

	public void age(World world, BlockPos pos, IBlockState state, Random rand, boolean fast);
	
}
