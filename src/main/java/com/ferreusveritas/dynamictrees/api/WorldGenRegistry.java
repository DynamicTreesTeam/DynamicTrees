package com.ferreusveritas.dynamictrees.api;

import com.ferreusveritas.dynamictrees.init.DTConfigs;
import com.ferreusveritas.dynamictrees.worldgen.BiomeDatabaseManager;

/**
 * NOTICE: Database population has moved to {@link BiomeDatabaseManager}.
 */
public class WorldGenRegistry {

	// TODO: Just remove this class. It's no longer needed.

	/**
	 * Mods should use this function to determine if worldgen is enabled for Dynamic Trees
	 *
	 * @return
	 */
	public static boolean isWorldGenEnabled() {
		return DTConfigs.worldGen.get();
	}

}
