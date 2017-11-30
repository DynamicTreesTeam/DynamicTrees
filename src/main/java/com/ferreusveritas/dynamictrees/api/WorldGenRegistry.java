package com.ferreusveritas.dynamictrees.api;

import com.ferreusveritas.dynamictrees.ConfigHandler;
import com.ferreusveritas.dynamictrees.DynamicTrees;
import com.ferreusveritas.dynamictrees.api.worldgen.IBiomeDensityProvider;
import com.ferreusveritas.dynamictrees.api.worldgen.IBiomeTreeSelector;

public class WorldGenRegistry {

	//////////////////////////////
	// BIOME HANDLING FOR WORLDGEN
	//////////////////////////////
	
	/**
	 * Mods should call this to register an {@link IBiomeTreeSelector}.
	 * 
	 * @param treeSelector The tree selector being registered
	 * @return
	 */
	public static boolean registerBiomeTreeSelector(IBiomeTreeSelector treeSelector) {
		if(DynamicTrees.treeGenerator != null) {
			DynamicTrees.treeGenerator.biomeTreeHandler.addTreeSelector(treeSelector);
			return true;
		}
		return false;
	}
	
	/**
	 * Mods should call this to register an {@link IBiomeDensityProvider}
	 * 
	 * @param densityProvider The density provider being registered
	 * @return
	 */
	public static boolean registerBiomeDensityProvider(IBiomeDensityProvider densityProvider) {
		if(DynamicTrees.treeGenerator != null) {
			DynamicTrees.treeGenerator.biomeTreeHandler.addDensityProvider(densityProvider);
			return true;
		}
		return false;
	}
	
	/**
	 * Mods should use this function to determine if worldgen is enabled for Dynamic Trees
	 * 
	 * @return
	 */
	public static boolean isWorldGenEnabled() {
		return ConfigHandler.worldGen;
	}
	
}
