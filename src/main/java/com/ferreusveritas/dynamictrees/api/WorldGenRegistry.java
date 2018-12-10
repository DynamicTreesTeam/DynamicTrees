package com.ferreusveritas.dynamictrees.api;

import java.util.ArrayList;
import java.util.List;

import com.ferreusveritas.dynamictrees.ModConfigs;
import com.ferreusveritas.dynamictrees.ModConstants;
import com.ferreusveritas.dynamictrees.api.events.PopulateDataBaseEvent;
import com.ferreusveritas.dynamictrees.api.worldgen.IBiomeDataBasePopulator;
import com.ferreusveritas.dynamictrees.worldgen.BiomeDataBase;
import com.ferreusveritas.dynamictrees.worldgen.BiomeDataBasePopulatorJson;
import com.ferreusveritas.dynamictrees.worldgen.MultiDimensionalPopulator;
import com.ferreusveritas.dynamictrees.worldgen.TreeGenerator;

import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.Event;

public class WorldGenRegistry {
	
	//////////////////////////////
	// BIOME HANDLING FOR WORLDGEN
	//////////////////////////////
	
	public static final String RESOURCEPATH = "worldgen/default.json";
	
	/**
	 * Mods should use this function to determine if worldgen is enabled for Dynamic Trees
	 * 
	 * @return
	 */
	public static boolean isWorldGenEnabled() {
		return ModConfigs.worldGen;
	}
	
	private static IBiomeDataBasePopulator collectDataBasePopulators() {
		BiomeDataBasePopulatorRegistryEvent event = new BiomeDataBasePopulatorRegistryEvent();
		
		//This registers the main populator
		event.register(new BiomeDataBasePopulatorJson(new ResourceLocation(ModConstants.MODID, RESOURCEPATH)));
		
		//This loads populators from add-ons
		MinecraftForge.EVENT_BUS.post(event);
		
		//This loads the custom default populator from config
		loadCustomDefaultPopulator(event);
		
		return event.getPopulator();
	}
	
	private static void loadCustomDefaultPopulator(BiomeDataBasePopulatorRegistryEvent event) {
		//TODO: Load custom default populator from config here
	}
	
	public static void populateDataBase() {
		if(WorldGenRegistry.isWorldGenEnabled()) {
			
			TreeGenerator.getTreeGenerator().clearAllBiomeDataBases();
			BiomeDataBase database = TreeGenerator.getTreeGenerator().getDefaultBiomeDataBase();
			
			//This collects all available populators and returns an aggregate populator for the lot
			IBiomeDataBasePopulator biomePopulator = collectDataBasePopulators();
			
			//This is where the main population occurs
			biomePopulator.populate(database);
			
			//Send out an event after the database has been populated
			MinecraftForge.EVENT_BUS.post(new PopulateDataBaseEvent(database, biomePopulator));
			
			//Populate custom dimensions if available
			new MultiDimensionalPopulator(new ResourceLocation(ModConstants.MODID, "worldgen/dimensions.json"), biomePopulator);
			
			//Blacklist certain dimensions according to the base config
			ModConfigs.dimensionBlacklist.forEach(d -> TreeGenerator.getTreeGenerator().BlackListDimension(d));
			
			BiomeDataBasePopulatorJson.cleanup();
		}
	}
	
	public static class BiomeDataBasePopulatorRegistryEvent extends Event {
		
		private final List<IBiomeDataBasePopulator> biomePopulators = new ArrayList<>();
		
		public void register(IBiomeDataBasePopulator populator) {
			biomePopulators.add(populator);
		}
		
		private IBiomeDataBasePopulator getPopulator() {
			return biomeDataBase -> biomePopulators.forEach(p -> p.populate(biomeDataBase));
		};
		
	}
}
