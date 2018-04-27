package com.ferreusveritas.dynamictrees.api.worldgen;

import java.util.Random;

import com.ferreusveritas.dynamictrees.api.TreeRegistry;
import com.ferreusveritas.dynamictrees.trees.Species;

import net.minecraft.util.ResourceLocation;
import net.minecraft.world.biome.Biome;

/**
 * Provides the forest density for a given biome.
 * Mods should implement this interface and register it via the {@link TreeRegistry} to control how densely populated a {@link Biome} is.
 * 
 * @author ferreusveritas
 */
public interface IBiomeDensityProvider {
	
	public interface IChanceSelector {
		EnumChance getChance(Random random, Species species, int radius);
	}

	public interface IDensitySelector {
		double getDensity(Random random, double noiseDensity);
	}

	@Deprecated
	public class DensityData {
		private final IChanceSelector chance;
		private final IDensitySelector density;
		
		public DensityData(IChanceSelector chance, IDensitySelector density) {
			this.chance = chance;
			this.density = density;
		}
		
		public IDensitySelector getDensitySelector() {
			return density;
		}
		
		public IChanceSelector getChanceSelector() {
			return chance;
		}
	}
	
	/**
	 * A unique name to identify this {@link IBiomeDensityProvider}.
	 * 
	 * @return
	 */
	public ResourceLocation getName();
	
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
	double density(Biome biome, double noiseDensity, Random random);
	
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
