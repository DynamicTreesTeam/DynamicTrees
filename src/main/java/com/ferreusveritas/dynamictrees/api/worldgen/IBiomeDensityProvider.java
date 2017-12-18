package com.ferreusveritas.dynamictrees.api.worldgen;

import java.util.Random;

import com.ferreusveritas.dynamictrees.api.TreeRegistry;
import com.ferreusveritas.dynamictrees.trees.Species;

import net.minecraft.world.biome.Biome;

/**
 * Provides the forest density for a given biome.
 * Mods should implement this interface and register it via the {@link TreeRegistry} to control how densely populated a {@link Biome} is.
 * 
 * @author ferreusveritas
 */
public interface IBiomeDensityProvider {
	
	/**
	 * A unique name to identify this {@link IBiomeDensityProvider}.
	 * It's recommended to use something like "modid:name"
	 * 
	 * @return
	 */
	public String getName();
	
	/**
	 * Used to determine which selector should run first.  Higher values are executed first.
	 * 
	 * @return priority number
	 */
	public int getPriority();
	
	/**
	 * Get the forest density of the {@link Biome}
	 * 
	 * @param biome The biome we are checking
	 * @param noiseDensity The density that the noise function returned(0.0 - 1.0)
	 * @return density 0.0(lowest) - 1.0(highest)  anything less than 0 will signal unhandled 
	 */
	double getDensity(Biome biome, double noiseDensity, Random random);
	
	/**
	 * Given the set of parameters determine if the tree should really be created
	 * 
	 * @param biome
	 * @param tree
	 * @param radius
	 * @param random
	 * @return true to produce a tree false otherwise
	 */
	public EnumChance chance(Biome biome, Species species, int radius, Random random);
	
	enum EnumChance {
		OK,
		CANCEL,
		UNHANDLED
	}
}
