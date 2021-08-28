package com.ferreusveritas.dynamictrees.api.substances;

import com.ferreusveritas.dynamictrees.blocks.BlockRooty;
import com.ferreusveritas.dynamictrees.entities.EntityLingeringEffector;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

/**
 * Represents a type of effect, somewhat like a potion effect, but for trees.
 * <p>
 * Effects may either be lingering or instant, which can be checked by {@link #isLingering()}. Lingering potion effects
 * can use {@link #apply(World, BlockPos)} to set up the effect and {@link #update(World, BlockPos, int, int)} to apply 
 * the effect on each tick. These types are controlled by a {@linkplain EntityLingeringEffector parent entity}. 
 * <p>
 * Instant effects should use {@link #apply(World, BlockPos)} to apply the effect and return {@code false} on {@link 
 * #update(World, BlockPos, int, int)}. 
 *
 * @author ferreusveritas
 * @see EntityLingeringEffector
 */
public interface ISubstanceEffect {

	/**
	 * Applies the substance effect. For lingering effects this may be used for setup.
	 *
	 * @param world the current world
	 * @param rootPos the position of the {@linkplain BlockRooty root block}
	 * @return {@code true} if the application was successful; {@code false} otherwise
	 */
	boolean apply(World world, BlockPos rootPos);

	/**
	 * Updates a lingering effect. Called every tick by the {@linkplain EntityLingeringEffector controlling entity}.
	 *
	 * @param world the current world
	 * @param rootPos the position of the {@linkplain BlockRooty root block}
	 * @param deltaTicks the number of ticks since the effect started
	 * @param fertility the fertility of the tree 
	 * @return {@code true} if the effect should linger; {@code false} otherwise
	 */
	boolean update(World world, BlockPos rootPos, int deltaTicks, int fertility);

	/**
	 * Returns the name of the effect. Used to compare existing effects in the environment.
	 *
	 * @return the name of the effect
	 */
	String getName();

	/**
	 * Determines if the effect is continuous or instant. 
	 *
	 * @return {@code true} if continuous; {@code false} if instant
	 */
	boolean isLingering();

}
