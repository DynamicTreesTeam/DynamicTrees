package com.ferreusveritas.dynamictrees.api;

import com.ferreusveritas.dynamictrees.DynamicTrees;
import com.ferreusveritas.dynamictrees.api.events.TreeCancelRegistryEvent;
import com.ferreusveritas.dynamictrees.resources.DTDataPackRegistries;
import com.ferreusveritas.dynamictrees.worldgen.canceller.ITreeCanceller;
import com.ferreusveritas.dynamictrees.api.events.JsonCapabilityRegistryEvent;
import com.ferreusveritas.dynamictrees.init.DTConfigs;
import com.ferreusveritas.dynamictrees.worldgen.*;
import com.ferreusveritas.dynamictrees.worldgen.canceller.TreeCancellerJson;
import com.ferreusveritas.dynamictrees.worldgen.json.IJsonBiomeApplier;
import com.ferreusveritas.dynamictrees.worldgen.json.IJsonBiomeSelector;
import net.minecraftforge.common.MinecraftForge;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.BufferedWriter;
import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

/**
 * NOTICE: Database population has moved to {@link BiomeDatabaseManager}.
 */
public class WorldGenRegistry {

	private static final Logger LOGGER = LogManager.getLogger();

	//////////////////////////////
	// BIOME HANDLING FOR WORLDGEN
	//////////////////////////////

	public static final String CONFIG_DIR_PATH = "/" + DynamicTrees.MOD_ID;
	public static final String WORLD_GEN_DIR_PATH = CONFIG_DIR_PATH + "/world_gen";
	public static final String TREE_CANCELLER_CONFIG_PATH = WORLD_GEN_DIR_PATH + "/tree_canceller.json";

	/**
	 * Mods should use this function to determine if worldgen is enabled for Dynamic Trees
	 *
	 * @return
	 */
	public static boolean isWorldGenEnabled() {
		return DTConfigs.worldGen.get();
	}

	private static File loadTreeCancellerFile() {
		File file = new File(DTConfigs.CONFIG_DIRECTORY.getAbsolutePath() + TREE_CANCELLER_CONFIG_PATH);

		if (!file.exists())
			writeDefaultTreeCanceller(file);

		return file;
	}

	private static void writeDefaultTreeCanceller (File file) {
		try {
			new File(DTConfigs.CONFIG_DIRECTORY.getAbsolutePath() + WORLD_GEN_DIR_PATH).mkdirs();
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
			LOGGER.fatal("Error creating tree canceller Json file:");
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

	public static boolean validateBiomeDataBases() {
		if (WorldGenRegistry.isWorldGenEnabled()) {
			return DTDataPackRegistries.BIOME_DATABASE_MANAGER.validateDatabases();
		}
		return true;
	}

	public static class TreeCancellerJsonCapabilityRegistryEvent extends JsonCapabilityRegistryEvent {

		public void register(String name, IJsonBiomeSelector selector) {
			TreeCancellerJson.addJsonBiomeSelector(name, selector);
		}

		public void register(String name, IJsonBiomeApplier applier) { } // This is not needed here, so do nothing.

	}

}
