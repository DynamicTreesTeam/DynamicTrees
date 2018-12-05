package com.ferreusveritas.dynamictrees.api;

import java.util.ArrayList;
import java.util.List;

import com.ferreusveritas.dynamictrees.ModConfigs;
import com.ferreusveritas.dynamictrees.api.events.PopulateDataBaseEvent;
import com.ferreusveritas.dynamictrees.api.worldgen.IBiomeDataBasePopulator;
import com.ferreusveritas.dynamictrees.worldgen.BiomeDataBase;
import com.ferreusveritas.dynamictrees.worldgen.TreeGenerator;

import net.minecraftforge.common.MinecraftForge;

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
	
	public static List<IBiomeDataBasePopulator> biomePopulators = new ArrayList<>();
	
	public static void registerBiomeDataBasePopulator( IBiomeDataBasePopulator populator ) {
		if(WorldGenRegistry.isWorldGenEnabled()) {
			biomePopulators.add(populator);
		}
	}
	
	public static BiomeDataBase populateAsDefaultDataBase(BiomeDataBase dbase) {
		biomePopulators.forEach(p -> p.populate(dbase));
		return dbase;
	}
	
	public static void populateDataBase() {
		if(WorldGenRegistry.isWorldGenEnabled()) {
			populateAsDefaultDataBase(TreeGenerator.getTreeGenerator().getDefaultBiomeDataBase());
			MinecraftForge.EVENT_BUS.post(new PopulateDataBaseEvent());
			ModConfigs.dimensionBlacklist.forEach(d -> TreeGenerator.getTreeGenerator().BlackListDimension(d));
		}
		
		biomePopulators = null;//Free up the populators since they are no long used.
	}
	
}
