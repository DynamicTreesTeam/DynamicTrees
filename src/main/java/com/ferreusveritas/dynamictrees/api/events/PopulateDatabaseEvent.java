package com.ferreusveritas.dynamictrees.api.events;

import com.ferreusveritas.dynamictrees.api.worldgen.IBiomeDatabasePopulator;
import com.ferreusveritas.dynamictrees.worldgen.BiomeDatabase;
import net.minecraftforge.eventbus.api.Event;

public class PopulateDatabaseEvent extends Event {

	final private BiomeDatabase biomeDataBase;
	final private IBiomeDatabasePopulator defaultPopulator;

	public PopulateDatabaseEvent(BiomeDatabase biomeDataBase, IBiomeDatabasePopulator defaultPopulator) {
		this.biomeDataBase = biomeDataBase;
		this.defaultPopulator = defaultPopulator;
	}

	public BiomeDatabase getBiomeDataBase() {
		return biomeDataBase;
	}

	public IBiomeDatabasePopulator getDefaultPopulator() {
		return defaultPopulator;
	}

}
