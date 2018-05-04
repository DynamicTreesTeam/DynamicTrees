package com.ferreusveritas.dynamictrees.worldgen;

import com.ferreusveritas.dynamictrees.api.worldgen.IBiomeDataBasePopulator;
import com.ferreusveritas.dynamictrees.worldgen.BiomeDataBase;

public class JsonBiomeDataBasePopulator implements IBiomeDataBasePopulator {

	protected final BiomeDataBase dbase;
	
	public JsonBiomeDataBasePopulator(BiomeDataBase db) {
		dbase = db;
	}

	public void populate () {
		//Super important Json loading stuff goes here
	}
	
}
