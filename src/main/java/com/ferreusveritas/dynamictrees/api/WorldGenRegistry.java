package com.ferreusveritas.dynamictrees.api;

import com.ferreusveritas.dynamictrees.ModConfigs;

public class WorldGenRegistry {

	//////////////////////////////
	// BIOME HANDLING FOR WORLDGEN
	//////////////////////////////
	
	/**
	 * Mods should use this function to determine if worldgen is enabled for Dynamic Trees
	 * 
	 * @return
	 */
	public static boolean isWorldGenEnabled() {
		return ModConfigs.worldGen;
	}
	
}
