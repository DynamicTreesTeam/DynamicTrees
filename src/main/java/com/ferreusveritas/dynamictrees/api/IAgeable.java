package com.ferreusveritas.dynamictrees.api;

import java.util.Random;

import com.ferreusveritas.dynamictrees.api.backport.BlockPos;

import net.minecraft.world.World;

public interface IAgeable {

	public void age(World world, BlockPos pos, Random rand, boolean fast);
	
}
