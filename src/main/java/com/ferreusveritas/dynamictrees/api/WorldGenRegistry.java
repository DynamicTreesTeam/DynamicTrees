package com.ferreusveritas.dynamictrees.api;

import com.ferreusveritas.dynamictrees.DynamicTrees;
import com.ferreusveritas.dynamictrees.api.events.TreeCancelRegistryEvent;
import com.ferreusveritas.dynamictrees.api.events.PopulateDataBaseEvent;
import com.ferreusveritas.dynamictrees.api.worldgen.IBiomeDataBasePopulator;
import com.ferreusveritas.dynamictrees.worldgen.canceller.ITreeCanceller;
import com.ferreusveritas.dynamictrees.api.worldgen.JsonCapabilityRegistryEvent;
import com.ferreusveritas.dynamictrees.init.DTConfigs;
import com.ferreusveritas.dynamictrees.util.JsonHelper;
import com.ferreusveritas.dynamictrees.worldgen.*;
import com.ferreusveritas.dynamictrees.worldgen.canceller.TreeCancellerJson;
import com.ferreusveritas.dynamictrees.worldgen.json.IJsonBiomeApplier;
import com.ferreusveritas.dynamictrees.worldgen.json.IJsonBiomeSelector;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.ResourceLocationException;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.Event;

import java.io.BufferedWriter;
import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

public class WorldGenRegistry {

	//////////////////////////////
	// BIOME HANDLING FOR WORLDGEN
	//////////////////////////////

	public static final String RESOURCE_PATH = "worldgen/default.json";
	public static final String CONFIG_PATH = "/" + DynamicTrees.MOD_ID;
	public static final String WORLD_GEN_CONFIG_PATH = CONFIG_PATH + "/world_gen.json";
	public static final String DIM_GEN_CONFIG_PATH = CONFIG_PATH + "/dimensions.json";
	public static final String TREE_CANCELLER_CONFIG_PATH = CONFIG_PATH + "/tree_canceller.json";

	/**
	 * Mods should use this function to determine if worldgen is enabled for Dynamic Trees
	 *
	 * @return
	 */
	public static boolean isWorldGenEnabled() {
		return DTConfigs.worldGen.get();
	}

	private static IBiomeDataBasePopulator collectDataBasePopulators() {
		BiomeDataBasePopulatorRegistryEvent event = new BiomeDataBasePopulatorRegistryEvent();

		//This registers the main populator
		event.register(new BiomeDataBasePopulatorJson(new ResourceLocation(DynamicTrees.MOD_ID, RESOURCE_PATH)));

		//This loads populators from add-ons
		MinecraftForge.EVENT_BUS.post(event);

		//This loads the custom default populator from config
		loadCustomDefaultPopulator(event);

		return event.getPopulator();
	}

	private static void loadCustomDefaultPopulator(BiomeDataBasePopulatorRegistryEvent event) {
		File file = new File(DTConfigs.configDirectory.getAbsolutePath() + WORLD_GEN_CONFIG_PATH);

		if(!file.exists()) {
			writeBlankJsonArrayToFile(file);
		} else {
			event.register(new BiomeDataBasePopulatorJson(JsonHelper.load(file), file.getName()));
		}
	}

	private static void loadMultiDimensionalPopulator(IBiomeDataBasePopulator populator) {
		File file = new File(DTConfigs.configDirectory.getAbsolutePath() + DIM_GEN_CONFIG_PATH);

		if(!file.exists()) {
			writeBlankJsonArrayToFile(file);
		} else {
			new MultiDimensionalPopulator(JsonHelper.load(file), populator);
		}
	}

	private static File loadTreeCancellerFile() {
		File file = new File(DTConfigs.configDirectory.getAbsolutePath() + TREE_CANCELLER_CONFIG_PATH);

		if (!file.exists())
			writeDefaultTreeCanceller(file);

		return file;
	}

	private static void writeBlankJsonArrayToFile(File file) {
		try {
			new File(DTConfigs.configDirectory.getAbsolutePath() + CONFIG_PATH).mkdirs();
			BufferedWriter writer = Files.newBufferedWriter(file.toPath(), StandardCharsets.UTF_8);
			writer.write("[]");//Write the minimal amount of data for the file to be a valid json array.
			writer.close();
		} catch (Exception e) {
			DynamicTrees.getLogger().fatal("Error creating placeholder world gen Json files:");
			e.printStackTrace();
		}
	}

	private static void writeDefaultTreeCanceller (File file) {
		try {
			new File(DTConfigs.configDirectory.getAbsolutePath() + CONFIG_PATH).mkdirs();
			BufferedWriter writer = Files.newBufferedWriter(file.toPath(), StandardCharsets.UTF_8);
			writer.write("[\n" +
					"  {\n" +
					"    \"__comment\": \"If disabled, cancellations will come from this Json file only.\",\n" +
					"    \"__note\": \"This means that DT and its addons will not register their default tree cancellations.\",\n" +
					"    \"auto_cancel\": true\n" +
					"  }\n" +
					"]");
			writer.close();
		} catch (Exception e) {
			DynamicTrees.getLogger().fatal("Error creating tree canceller Json file:");
			e.printStackTrace();
		}
	}

	public static ITreeCanceller getJsonTreeCanceller() {
		if (TreeCancellerJson.INSTANCE == null)
			loadJsonTreeCanceller();

		return TreeCancellerJson.INSTANCE;
	}

	private static void loadJsonTreeCanceller () {
		TreeCancellerJsonCapabilityRegistryEvent capabilityEvent = new TreeCancellerJsonCapabilityRegistryEvent();

		// Create the tree canceller.
		TreeCancellerJson treeCanceller = new TreeCancellerJson(loadTreeCancellerFile(), capabilityEvent);

		// If auto cancel is enabled in the Json file, handle auto cancellers.
		if (treeCanceller.isAutoCancel()) {
			TreeCancelRegistryEvent treeCancelRegistryEvent = new TreeCancelRegistryEvent(treeCanceller);

			// Send event for tree cancellations to be registered.
			MinecraftForge.EVENT_BUS.post(treeCancelRegistryEvent);
		}

		// Cleanup unused static objects.
		BiomeSelectorJson.cleanupBiomeSelectors();
	}

	public static void populateDataBase() {
		if(!WorldGenRegistry.isWorldGenEnabled())
			return;

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
		DTConfigs.dimensionBlackList.get().forEach(dimLocString -> {
			try {
				TreeGenerator.getTreeGenerator().addBlacklistedDimension(new ResourceLocation(dimLocString));
			} catch (ResourceLocationException e) {
				DynamicTrees.getLogger().warn("Couldn't get resource location for dimension blacklist in config: " + e.getMessage());
			}
		});

		//Cleanup all of the unused static objects
		BiomeDataBasePopulatorJson.cleanup();
	}

	public static boolean validateBiomeDataBases() {
		if(WorldGenRegistry.isWorldGenEnabled()) {
			return TreeGenerator.getTreeGenerator().validateBiomeDataBases();
		}
		return true;
	}

	public static class BiomeDataBaseJsonCapabilityRegistryEvent extends JsonCapabilityRegistryEvent {

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

	public static class TreeCancellerJsonCapabilityRegistryEvent extends JsonCapabilityRegistryEvent {

		public void register(String name, IJsonBiomeSelector selector) {
			TreeCancellerJson.addJsonBiomeSelector(name, selector);
		}

		public void register(String name, IJsonBiomeApplier applier) { } // This is not needed here, so do nothing.

	}

}
