package com.ferreusveritas.dynamictrees.api;

import java.util.Random;

import com.ferreusveritas.dynamictrees.trees.DynamicTree;

import com.ferreusveritas.dynamictrees.api.backport.BlockPos;
import com.ferreusveritas.dynamictrees.api.backport.World;

public interface IBottomListener {

	/**
	 * Override to create a custom special effect
	 * 
	 * @param world
	 * @param tree
	 * @param pos
	 * @param random
	 */
	public void run(World world, DynamicTree tree, BlockPos pos, Random random);

	/**
	 * @return 0 to 1 chance of something happening. Must return a positive non-zero number.  Return 1 to always run
	 */
	public float chance();

	/**
	 * @return Name of the special effect
	 */
	public String getName();
}
