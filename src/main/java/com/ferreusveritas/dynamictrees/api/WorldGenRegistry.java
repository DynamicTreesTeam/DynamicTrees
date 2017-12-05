package com.ferreusveritas.dynamictrees.api;

import com.ferreusveritas.dynamictrees.ConfigHandler;
import com.ferreusveritas.dynamictrees.api.worldgen.IBiomeDensityProvider;
import com.ferreusveritas.dynamictrees.api.worldgen.IBiomeTreeSelector;
import com.ferreusveritas.dynamictrees.worldgen.TreeGenerator;

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
		if(TreeGenerator.getTreeGenerator() != null) {
			TreeGenerator.getTreeGenerator().biomeTreeHandler.addTreeSelector(treeSelector);
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
		if(TreeGenerator.getTreeGenerator() != null) {
			TreeGenerator.getTreeGenerator().biomeTreeHandler.addDensityProvider(densityProvider);
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
