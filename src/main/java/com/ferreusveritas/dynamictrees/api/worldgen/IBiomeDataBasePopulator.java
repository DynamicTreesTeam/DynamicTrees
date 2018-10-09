package com.ferreusveritas.dynamictrees.api.worldgen;

import com.ferreusveritas.dynamictrees.ModConfigs;
import com.ferreusveritas.dynamictrees.worldgen.BiomeDataBase;

import net.minecraft.world.biome.Biome;
import net.minecraftforge.common.BiomeDictionary;
import net.minecraftforge.common.BiomeDictionary.Type;

public interface IBiomeDataBasePopulator {

	@Deprecated
	public default void populate() { }
	
	public void populate(BiomeDataBase biomeDataBase);
	
	public default float identifyForestness(Biome biome) {
		float forestness = (BiomeDictionary.hasType(biome, Type.FOREST) || BiomeDictionary.hasType(biome, Type.JUNGLE) ) ? 1.0f : 0.0f;
		
		if(BiomeDictionary.hasType(biome, Type.SPARSE)) {
			forestness *= 0.25f;
		}
		
		return Math.max(forestness, ModConfigs.seedMinForestness);
	}
}
