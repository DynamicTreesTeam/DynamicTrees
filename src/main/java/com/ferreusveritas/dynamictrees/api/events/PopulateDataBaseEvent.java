package com.ferreusveritas.dynamictrees.api.events;

import com.ferreusveritas.dynamictrees.api.worldgen.IBiomeDataBasePopulator;
import com.ferreusveritas.dynamictrees.worldgen.BiomeDataBase;
import net.minecraftforge.fml.common.eventhandler.Event;

public class PopulateDataBaseEvent extends Event {

	final private BiomeDataBase biomeDataBase;
	final private IBiomeDataBasePopulator defaultPopulator;

	public PopulateDataBaseEvent(BiomeDataBase biomeDataBase, IBiomeDataBasePopulator defaultPopulator) {
		this.biomeDataBase = biomeDataBase;
		this.defaultPopulator = defaultPopulator;
	}

	public BiomeDataBase getBiomeDataBase() {
		return biomeDataBase;
	}

	public IBiomeDataBasePopulator getDefaultPopulator() {
		return defaultPopulator;
	}

}
