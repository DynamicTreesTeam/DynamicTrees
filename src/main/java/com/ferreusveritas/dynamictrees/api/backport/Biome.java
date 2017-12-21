package com.ferreusveritas.dynamictrees.api.backport;

import net.minecraft.world.biome.BiomeGenBase;

public class Biome {

	private final BiomeGenBase biome;
	public final int biomeID;
	
	public Biome(BiomeGenBase biome) {
		this.biome = biome;
		this.biomeID = biome.biomeID;
	}
	
	public BiomeGenBase base() {
		return biome;
	}
	
	public static int getIdForBiome(Biome biome) {
		return biome.base().biomeID;
	}
}
