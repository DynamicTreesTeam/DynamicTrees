package com.ferreusveritas.dynamictrees.api.events;

import com.ferreusveritas.dynamictrees.worldgen.BiomeDataBase;

import net.minecraftforge.fml.common.eventhandler.Event;

public class PopulateDataBaseEvent extends Event {
	
	final private BiomeDataBase biomeDataBase;
	
	public PopulateDataBaseEvent(BiomeDataBase biomeDataBase) {
		this.biomeDataBase = biomeDataBase;
	}

	public BiomeDataBase getBiomeDataBase() {
		return biomeDataBase;
	}
	
}
