package com.ferreusveritas.dynamictrees.api;

import java.io.BufferedWriter;
import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

import com.ferreusveritas.dynamictrees.ModConfigs;
import com.ferreusveritas.dynamictrees.ModConstants;
import com.ferreusveritas.dynamictrees.api.events.PopulateDataBaseEvent;
import com.ferreusveritas.dynamictrees.api.worldgen.IBiomeDataBasePopulator;
import com.ferreusveritas.dynamictrees.util.JsonHelper;
import com.ferreusveritas.dynamictrees.worldgen.BiomeDataBase;
import com.ferreusveritas.dynamictrees.worldgen.BiomeDataBasePopulatorJson;
import com.ferreusveritas.dynamictrees.worldgen.MultiDimensionalPopulator;
import com.ferreusveritas.dynamictrees.worldgen.TreeGenerator;
import com.ferreusveritas.dynamictrees.worldgen.json.IJsonBiomeApplier;
import com.ferreusveritas.dynamictrees.worldgen.json.IJsonBiomeSelector;

import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.Event;

public class WorldGenRegistry {
	
	//////////////////////////////
	// BIOME HANDLING FOR WORLDGEN
	//////////////////////////////
	
	private static final String RESOURCEPATH = "worldgen/default.json";
	private static final String CONFIGPATH = "/" + ModConstants.MODID;
	private static final String WORLDGENCONFIGPATH = CONFIGPATH + "/worldgen.json";
	private static final String DIMGENCONFIGPATH = CONFIGPATH + "/dimensions.json";
	
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
		
		File file = new File(ModConfigs.configDirectory.getAbsolutePath() + WORLDGENCONFIGPATH);
		
		if(!file.exists()) {
			writeBlankJsonArrayToFile(file);
		} else {
			event.register(new BiomeDataBasePopulatorJson(JsonHelper.load(file)));
		}
	}
	
	private static void loadMultiDimensionalPopulator(IBiomeDataBasePopulator populator) {
		
		File file = new File(ModConfigs.configDirectory.getAbsolutePath() + DIMGENCONFIGPATH);
		
		if(!file.exists()) {
			writeBlankJsonArrayToFile(file);
		} else {
			new MultiDimensionalPopulator(JsonHelper.load(file), populator);
		}
	}
	
	private static void writeBlankJsonArrayToFile(File file) {
		try {
			new File(ModConfigs.configDirectory.getAbsolutePath() + CONFIGPATH).mkdirs();
			BufferedWriter writer = Files.newBufferedWriter(file.toPath(), StandardCharsets.UTF_8);
			writer.write("[]");//Write the minimal amount of data for the file to be a valid json array.
			writer.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public static void populateDataBase() {
		if(WorldGenRegistry.isWorldGenEnabled()) {
			
			BiomeDataBaseJsonCapabilityRegistryEvent capabilityEvent = new BiomeDataBaseJsonCapabilityRegistryEvent();
			
			//Register the main Json capabilities
			BiomeDataBasePopulatorJson.registerJsonCapabilities(capabilityEvent);
			
			//Send out an event asking for Json Capabilities to be registered
			MinecraftForge.EVENT_BUS.post(capabilityEvent);
			
			//Prep the databases by clearing them out
			TreeGenerator.getTreeGenerator().clearAllBiomeDataBases();
			BiomeDataBase database = TreeGenerator.getTreeGenerator().getDefaultBiomeDataBase();
			
			//This collects all available populators and returns an aggregate populator for the lot
			IBiomeDataBasePopulator biomePopulator = collectDataBasePopulators();
			
			//This is where the main population occurs
			biomePopulator.populate(database);
			
			//Send out an event after the database has been populated
			MinecraftForge.EVENT_BUS.post(new PopulateDataBaseEvent(database, biomePopulator));
			
			//Populate custom dimensions if available
			loadMultiDimensionalPopulator(biomePopulator);
			
			//Blacklist certain dimensions according to the base config
			ModConfigs.dimensionBlacklist.forEach(d -> TreeGenerator.getTreeGenerator().BlackListDimension(d));
			
			//Cleanup all of the unused static objects
			BiomeDataBasePopulatorJson.cleanup();
		}
	}
	
	public static boolean validateBiomeDataBases() {
		if(WorldGenRegistry.isWorldGenEnabled()) {
			return TreeGenerator.getTreeGenerator().validateBiomeDataBases();
		}
		return true;
	}
	
	public static class BiomeDataBaseJsonCapabilityRegistryEvent extends Event {
		
		public void register(String name, IJsonBiomeSelector selector) {
			BiomeDataBasePopulatorJson.addJsonBiomeSelector(name, selector);
		}
		
		public void register(String name, IJsonBiomeApplier applier) {
			BiomeDataBasePopulatorJson.addJsonBiomeApplier(name, applier);
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
