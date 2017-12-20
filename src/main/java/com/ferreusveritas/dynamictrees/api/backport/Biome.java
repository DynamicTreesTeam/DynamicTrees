package com.ferreusveritas.dynamictrees.api.backport;

import net.minecraft.world.biome.BiomeGenBase;

public class Biome {

	private final BiomeGenBase biome;
	
	public Biome(BiomeGenBase biome) {
		this.biome = biome;
	}
	
	public BiomeGenBase getBiomeGenBase() {
		return biome;
	}
	
	public static int getIdForBiome(Biome biome) {
		return biome.getBiomeGenBase().biomeID;
	}
}
