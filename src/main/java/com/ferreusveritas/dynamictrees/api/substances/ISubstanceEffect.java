package com.ferreusveritas.dynamictrees.api.substances;

import net.minecraft.util.math.BlockPos;
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
	 * @param rootPos
	 * @return true for success.  false otherwise
	 */
	public boolean apply(World world, BlockPos rootPos);
	
	/**
	 * For a continuously updating effect.
	 * 
	 * @param world
	 * @param rootPos
	 * @param deltaTicks
	 * @return true to stay alive. false to kill effector
	 */
	public boolean update(World world, BlockPos rootPos, int deltaTicks);
	
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
