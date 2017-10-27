package com.ferreusveritas.dynamictrees.api;

import com.ferreusveritas.dynamictrees.ConfigHandler;
import com.ferreusveritas.dynamictrees.DynamicTrees;
import com.ferreusveritas.dynamictrees.api.worldgen.IBiomeDensityProvider;
import com.ferreusveritas.dynamictrees.api.worldgen.IBiomeTreeSelector;
import com.ferreusveritas.dynamictrees.trees.DynamicTree;

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
	
	
	//////////////////////////////
	// JOCODES FOR WORLDGEN
	//////////////////////////////
	
	/**
	 * Mods should call this to register JoCodes for their trees
	 * 
	 * @param tree The tree for which the code is being registered
	 * @param radius The radius of the model.  Model should not have branches that exceed this boundary
	 * @param code The code being registered
	 */
	public static void addJoCode(DynamicTree tree, int radius, String code) {
		if(DynamicTrees.treeGenerator != null) {
			DynamicTrees.treeGenerator.codeStore.addCode( tree,  radius,  code);
		}
	}

	/**
	 * Mods should call this to register JoCodes for their trees from a file
	 * 
	 * @param tree The tree for which the code is being registered
	 * @param filename The filename of a file with a list of JoCodes
	 */
	public static void addJoCodesFromFile(DynamicTree tree, String filename) {
		if(DynamicTrees.treeGenerator != null) {
			DynamicTrees.treeGenerator.codeStore.addCodesFromFile(tree, filename);
		}
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
