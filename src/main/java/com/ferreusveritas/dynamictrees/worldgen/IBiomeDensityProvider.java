package com.ferreusveritas.dynamictrees.worldgen;

import java.util.Random;

import com.ferreusveritas.dynamictrees.trees.DynamicTree;

import net.minecraft.world.biome.Biome;

/**
 * Provides the forest density for a given biome
 * 
 * @author ferreusveritas
 *
 */
public interface IBiomeDensityProvider {
	
	/**
	 * Get the forest density of the biome
	 * 
	 * @param biome The biome we are checking
	 * @param noiseDensity The density that the noise function returned
	 * @return density 0.0(lowest) - 1.0(highest) 
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
	public boolean chance(Biome biome, DynamicTree tree, int radius, Random random);
	
}
