package com.ferreusveritas.dynamictrees.worldgen;

import com.ferreusveritas.dynamictrees.DynamicTrees;
import com.ferreusveritas.dynamictrees.api.WorldGenRegistry;
import com.ferreusveritas.dynamictrees.api.events.BiomeDatabaseJsonCapabilityRegistryEvent;
import com.ferreusveritas.dynamictrees.api.events.BiomeDatabasePopulatorRegistryEvent;
import com.ferreusveritas.dynamictrees.api.events.PopulateDatabaseEvent;
import com.ferreusveritas.dynamictrees.api.worldgen.IBiomeDatabasePopulator;
import com.ferreusveritas.dynamictrees.init.DTConfigs;
import com.ferreusveritas.dynamictrees.init.DTDataPackRegistries;
import com.ferreusveritas.dynamictrees.util.JsonHelper;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.gson.*;
import net.minecraft.client.resources.ReloadListener;
import net.minecraft.profiler.IProfiler;
import net.minecraft.resources.IResourceManager;
import net.minecraft.util.JSONUtils;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.ResourceLocationException;
import net.minecraftforge.common.MinecraftForge;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * Manages {@link BiomeDatabase} objects, reading from datapacks. Main instance stored in
 * {@link DTDataPackRegistries}.
 *
 * @author Harley O'Connor
 */
public final class BiomeDatabaseManager extends ReloadListener<Map<ResourceLocation, Map<String, JsonElement>>> {

    private static final Logger LOGGER = LogManager.getLogger();

    private static final String JSON_EXTENSION = ".json";
    private static final int JSON_EXTENSION_LENGTH = JSON_EXTENSION.length();

    private static final String DEFAULT_POPULATOR_NAME = "default";

    private static final String REPLACE = "replace";
    private static final String ENTRIES = "entries";

    private final Gson gson = (new GsonBuilder()).setPrettyPrinting().disableHtmlEscaping().create();
    private final String folderName = "trees/world_gen";

    private BiomeDatabase defaultDatabase = new BiomeDatabase();
    private final Map<ResourceLocation, BiomeDatabase> dimensionDatabases = Maps.newHashMap();

    protected final Set<ResourceLocation> blacklistedDimensions = Sets.newHashSet();

    @Override
    protected Map<ResourceLocation, Map<String, JsonElement>> prepare(IResourceManager resourceManager, IProfiler profiler) {
        Map<ResourceLocation, Map<String, JsonElement>> databases = Maps.newHashMap();
        int i = folderName.length() + 1;

        for(ResourceLocation resourceLocationIn : resourceManager.getAllResourceLocations(this.folderName, (fileName) -> fileName.endsWith(JSON_EXTENSION))) {
            String resourcePath = resourceLocationIn.getPath();
            ResourceLocation resourceLocation = new ResourceLocation(resourceLocationIn.getNamespace(),
                    resourcePath.substring(i, resourcePath.length() - JSON_EXTENSION_LENGTH));

            try {
                resourceManager.getAllResources(resourceLocationIn).forEach(resource -> {
                    InputStream inputstream = resource.getInputStream();
                    Reader reader = new BufferedReader(new InputStreamReader(inputstream, StandardCharsets.UTF_8));

                    JsonElement jsonElement = JSONUtils.fromJson(this.gson, reader, JsonElement.class);

                    if (jsonElement != null) {
                        databases.computeIfAbsent(resourceLocation, l -> Maps.newHashMap()).put(resourceLocationIn.getPath(), jsonElement);
                    } else {
                        LOGGER.error("Couldn't load data file {} from {} as it's null or empty", resourceLocation, resourceLocationIn);
                    }
                });
            } catch (IllegalArgumentException | IOException | JsonParseException e) {
                LOGGER.error("Couldn't parse data file {} from {}", resourceLocation, resourceLocationIn, e);
            }
        }

        return databases;
    }

    @Override
    protected void apply(Map<ResourceLocation, Map<String, JsonElement>> databases, IResourceManager resourceManager, IProfiler profiler) {
        // Ensure databases are reset.
        this.defaultDatabase = new BiomeDatabase();
        this.dimensionDatabases.clear();
        this.blacklistedDimensions.clear();

        // If world gen is disabled, we don't populate the databases.
        if (!WorldGenRegistry.isWorldGenEnabled()) {
            return;
        }

        this.registerJsonCapabilities(); // Registers Json capabilities for Populator Jsons.

        BiomeDatabasePopulatorRegistryEvent event = new BiomeDatabasePopulatorRegistryEvent();
        MinecraftForge.EVENT_BUS.post(event); // Allows add-ons to register custom populators.

        // Stores the dimension database files, as they must be populated after the default database.
        final Map<ResourceLocation, Map<String, JsonElement>> dimensionDatabasesData = Maps.newHashMap();

        // Register the default populators and fetch the dimension populator files.
        databases.forEach((resourceLocation, jsonFiles) ->
            jsonFiles.forEach((fileName, jsonElement) -> {
                if (resourceLocation.getPath().equals(DEFAULT_POPULATOR_NAME)) {
                    this.registerDefaultPopulator(event, resourceLocation, fileName, jsonElement);
                } else {
                    dimensionDatabasesData.put(resourceLocation, jsonFiles);
                }
            })
        );

        IBiomeDatabasePopulator defaultPopulator = event.getPopulator();
        defaultPopulator.populate(this.defaultDatabase); // Populate the default database.

        // Send out an event after the database has been populated.
		MinecraftForge.EVENT_BUS.post(new PopulateDatabaseEvent(this.defaultDatabase, defaultPopulator));

		// Register dimension populators.
        dimensionDatabasesData.forEach((resourceLocation, jsonFiles) ->
                this.registerDimensionPopulators(defaultPopulator, resourceLocation, jsonFiles));

        // Blacklist certain dimensions according to the base config.
        DTConfigs.dimensionBlacklist.get().forEach(resourceLocationString -> {
            try {
                this.blacklistedDimensions.add(new ResourceLocation(resourceLocationString));
            } catch (ResourceLocationException e) {
                LOGGER.warn("Couldn't get resource location for dimension blacklist in config: " + e.getMessage());
            }
        });

        // Cleanup all of the unused static objects.
        JsonBiomeDatabasePopulator.cleanup();
    }

    private void registerJsonCapabilities () {
        BiomeDatabaseJsonCapabilityRegistryEvent capabilityEvent = new BiomeDatabaseJsonCapabilityRegistryEvent();

        //Register the main Json capabilities
        JsonBiomeDatabasePopulator.registerJsonCapabilities(capabilityEvent);

        //Send out an event asking for Json Capabilities to be registered
        MinecraftForge.EVENT_BUS.post(capabilityEvent);
    }

    private void registerDefaultPopulator(BiomeDatabasePopulatorRegistryEvent event, ResourceLocation resourceLocation, String fileName, JsonElement jsonElement) {
        if (!jsonElement.isJsonObject()) {
            LOGGER.warn("Skipping loading default populator {} from {} as its root element is not a Json object.", resourceLocation, fileName);
            return;
        }

        JsonObject jsonObject = jsonElement.getAsJsonObject();

        boolean replace = this.shouldReplace(jsonObject);
        JsonElement entries = jsonObject.get(ENTRIES);

        if (entries == null) {
            LOGGER.warn("Skipping loading default populator {} from {} as it had no entries.", resourceLocation, fileName);
            return;
        }

        IBiomeDatabasePopulator populator = new JsonBiomeDatabasePopulator(entries, fileName);

        if (replace) {
            event.replaceAll(populator);
        } else {
            if (resourceLocation.getNamespace().equals(DynamicTrees.MOD_ID)) {
                event.registerAsFirst(populator);
            } else {
                event.register(populator);
            }
        }
    }

    private void registerDimensionPopulators(IBiomeDatabasePopulator defaultPopulator, ResourceLocation resourceLocation, Map<String, JsonElement> jsonFiles) {
        BiomeDatabase dimensionDatabase = new BiomeDatabase();

        // Populate with default entries.
        defaultPopulator.populate(dimensionDatabase);

        jsonFiles.forEach((fileName, jsonElement) -> {
            if (!jsonElement.isJsonObject()) {
                LOGGER.warn("Skipping loading dimension populator for {} from {} as its root element is not a Json object.", resourceLocation, fileName);
                return;
            }

            JsonObject jsonObject = jsonElement.getAsJsonObject();

            boolean replace = this.shouldReplace(jsonObject);
            JsonElement entries = jsonObject.get(ENTRIES);

            if (entries == null) {
                LOGGER.warn("Skipping loading default populator for {} from {} as it had no entries.", resourceLocation, fileName);
                return;
            }

            if (replace) {
                // If we are replacing all generation for this dimension, reset the database.
                dimensionDatabase.clear();
            }

            // Populate with entries from current file.
            new JsonBiomeDatabasePopulator(entries, fileName).populate(dimensionDatabase);
        });

        this.dimensionDatabases.put(resourceLocation, dimensionDatabase);
    }

    private boolean shouldReplace (JsonObject jsonObject) {
        return JsonHelper.getOrDefault(jsonObject, REPLACE, false);
    }

    public BiomeDatabase getDefaultDatabase() {
        return this.defaultDatabase;
    }

    /**
     * Returns the {@link BiomeDatabase} object for the given dimension registry name,
     * or the default database if one did not exist for the dimension.
     *
     * @param dimensionRegistryName The dimension registry name.
     * @return The dimension's {@link BiomeDatabase} object, or the default one.
     */
    public BiomeDatabase getDimensionDatabase (ResourceLocation dimensionRegistryName) {
        return this.dimensionDatabases.getOrDefault(dimensionRegistryName, this.defaultDatabase);
    }

    public boolean validateDatabases () {
        return this.defaultDatabase.isValid() && this.dimensionDatabases.values().stream().allMatch(BiomeDatabase::isValid);
    }

    public boolean isDimensionBlacklisted (ResourceLocation resourceLocation) {
        return this.blacklistedDimensions.contains(resourceLocation);
    }

}
