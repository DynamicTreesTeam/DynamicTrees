package com.ferreusveritas.dynamictrees.api.substances;

import com.ferreusveritas.dynamictrees.api.backport.BlockPos;
import com.ferreusveritas.dynamictrees.blocks.BlockRootyDirt;

import net.minecraft.world.World;

/**
 * A substance effect is like a potion effect but for trees.
 * 
 * @author ferreusveritas
 *
 */
public interface ISubstanceEffect {
	
	/**
	 * For an instant effect.
	 * 
	 * @param world
	 * @param dirt
	 * @param pos
	 * @return true for success.  false otherwise
	 */
	public boolean apply(World world, BlockRootyDirt dirt, BlockPos pos);
	
	/**
	 * For a continuously updating effect.
	 * 
	 * @param world
	 * @param dirt
	 * @param pos
	 * @param deltaTicks
	 * @return true to stay alive. false to kill effector
	 */
	public boolean update(World world, BlockRootyDirt dirt, BlockPos pos, int deltaTicks);
	
	/**
	 * Get the name of the effect.  Used to compare existing effects in the environment.
	 * 
	 * @return the name of the effect.
	 */
	public String getName();
	
	/**
	 * Determines if the effect is continuous or instant
	 * 
	 * @return true if continuous, false if instant
	 */
	public boolean isLingering();
	
}
