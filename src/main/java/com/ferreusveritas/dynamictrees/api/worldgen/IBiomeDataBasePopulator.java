package com.ferreusveritas.dynamictrees.api.worldgen;

import com.ferreusveritas.dynamictrees.worldgen.BiomeDataBase;

public interface IBiomeDataBasePopulator {

	@Deprecated
	public default void populate() { }
	
	public void populate(BiomeDataBase biomeDataBase);
}
