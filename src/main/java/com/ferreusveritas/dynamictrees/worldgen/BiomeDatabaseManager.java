package com.ferreusveritas.dynamictrees.worldgen;

import com.ferreusveritas.dynamictrees.api.WorldGenRegistry;
import com.ferreusveritas.dynamictrees.api.events.AddFeatureCancellersEvent;
import com.ferreusveritas.dynamictrees.api.events.PopulateDefaultDatabaseEvent;
import com.ferreusveritas.dynamictrees.api.events.PopulateDimensionalDatabaseEvent;
import com.ferreusveritas.dynamictrees.api.treepacks.PropertyApplierResult;
import com.ferreusveritas.dynamictrees.api.worldgen.BiomePropertySelectors;
import com.ferreusveritas.dynamictrees.api.worldgen.FeatureCanceller;
import com.ferreusveritas.dynamictrees.init.DTConfigs;
import com.ferreusveritas.dynamictrees.resources.DTResourceRegistries;
import com.ferreusveritas.dynamictrees.resources.MultiJsonReloadListener;
import com.ferreusveritas.dynamictrees.util.BiomeList;
import com.ferreusveritas.dynamictrees.util.json.JsonHelper;
import com.ferreusveritas.dynamictrees.util.json.JsonObjectGetters;
import com.ferreusveritas.dynamictrees.util.json.JsonPropertyApplierList;
import com.ferreusveritas.dynamictrees.util.json.ObjectFetchResult;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.resources.IResourceManager;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.ResourceLocationException;
import net.minecraft.world.biome.Biome;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.Event;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Manages {@link BiomeDatabase} objects, reading from tree packs. Main instance stored in
 * {@link DTResourceRegistries}.
 *
 * @author Harley O'Connor
 */
public final class BiomeDatabaseManager extends MultiJsonReloadListener<Object> {

    private static final Logger LOGGER = LogManager.getLogger();

    private static final String DEFAULT_POPULATOR_NAME = "default";

    private static final String REPLACE = "replace";
    private static final String ENTRIES = "entries";

    private BiomeDatabase defaultDatabase = new BiomeDatabase();
    private final Map<ResourceLocation, BiomeDatabase> dimensionDatabases = Maps.newHashMap();

    private JsonPropertyApplierList<BiomeDatabase.Entry> biomeDatabaseAppliers;
    private JsonPropertyApplierList<BiomePropertySelectors.FeatureCancellations> featureCancellationAppliers;

    private final Set<ResourceLocation> blacklistedDimensions = Sets.newHashSet();

    public static final String SELECT = "select";
    public static final String APPLY = "apply";
    public static final String WHITE = "white";
    public static final String CANCELLERS = "cancellers";

    private static final String METHOD = "method";

    public BiomeDatabaseManager() {
        super("world_gen", Object.class, "null");
    }

    @Override
    public void registerAppliers(final String applierListIdentifier) {
        this.biomeDatabaseAppliers = new JsonPropertyApplierList<>(BiomeDatabase.Entry.class)
                .register("species", JsonElement.class, (entry, jsonElement) -> {
                    final ObjectFetchResult<BiomePropertySelectors.ISpeciesSelector> selectorFetchResult = JsonObjectGetters.SPECIES_SELECTOR_GETTER.get(jsonElement);

                    if (!selectorFetchResult.wasSuccessful())
                        return new PropertyApplierResult(selectorFetchResult.getErrorMessage());

                    final AtomicReference<BiomeDatabase.Operation> operation = new AtomicReference<>(BiomeDatabase.Operation.REPLACE);
                    getOperation(jsonElement).ifSuccessful(operation::set).otherwiseWarn("Error getting operation (defaulting to replace): ");

                    entry.getDatabase().setSpeciesSelector(entry.getBiome(), selectorFetchResult.getValue(), operation.get());
                    return PropertyApplierResult.SUCCESS;
                })
                .register("density", JsonElement.class, (entry, jsonElement) -> {
                    final ObjectFetchResult<BiomePropertySelectors.IDensitySelector> selectorFetchResult = JsonObjectGetters.DENSITY_SELECTOR_GETTER.get(jsonElement);

                    if (!selectorFetchResult.wasSuccessful())
                        return new PropertyApplierResult(selectorFetchResult.getErrorMessage());

                    final AtomicReference<BiomeDatabase.Operation> operation = new AtomicReference<>(BiomeDatabase.Operation.REPLACE);
                    getOperation(jsonElement).ifSuccessful(operation::set).otherwiseWarn("Error getting operation (defaulting to replace): ");

                    entry.getDatabase().setDensitySelector(entry.getBiome(), selectorFetchResult.getValue(), operation.get());
                    return PropertyApplierResult.SUCCESS;
                })
                .register("chance", JsonElement.class, (entry, jsonElement) -> {
                    final ObjectFetchResult<BiomePropertySelectors.IChanceSelector> selectorFetchResult = JsonObjectGetters.CHANCE_SELECTOR_GETTER.get(jsonElement);

                    if (!selectorFetchResult.wasSuccessful())
                        return new PropertyApplierResult(selectorFetchResult.getErrorMessage());

                    final AtomicReference<BiomeDatabase.Operation> operation = new AtomicReference<>(BiomeDatabase.Operation.REPLACE);
                    getOperation(jsonElement).ifSuccessful(operation::set).otherwiseWarn("Error getting operation (defaulting to replace): ");

                    entry.getDatabase().setChanceSelector(entry.getBiome(), selectorFetchResult.getValue(), operation.get());
                    return PropertyApplierResult.SUCCESS;
                })
                .register("multipass", Boolean.class, (entry, multipass) -> {
                    if (!multipass)
                        return;

                    entry.getDatabase().setMultipass(entry.getBiome(), pass -> {
                        switch(pass) {
                            case 0: return 0; // Zero means to run as normal.
                            case 1: return 5; // Return only radius 5 on pass 1.
                            case 2: return 3; // Return only radius 3 on pass 2.
                            default: return -1; // A negative number means to terminate.
                        }
                    });
                })
                .register("blacklist", Boolean.class, BiomeDatabase.Entry::setBlacklisted)
                .register("forestness", Float.class, BiomeDatabase.Entry::setForestness)
                .register("subterranean", Boolean.class, BiomeDatabase.Entry::setSubterraneanBiome)
                .registerIfTrueApplier("reset", entry -> {
                    final BiomeDatabase database = entry.getDatabase();
                    final Biome biome = entry.getBiome();

                    database.setSpeciesSelector(biome, (pos, dirt, rnd) -> new BiomePropertySelectors.SpeciesSelection(), BiomeDatabase.Operation.REPLACE);
                    database.setDensitySelector(biome, (rnd, nd) -> -1, BiomeDatabase.Operation.REPLACE);
                    database.setChanceSelector(biome, (rnd, spc, rad) -> BiomePropertySelectors.EnumChance.UNHANDLED, BiomeDatabase.Operation.REPLACE);
                    database.setForestness(biome, 0.0f);
                    database.setIsSubterranean(biome, false);
                    database.setMultipass(biome, pass -> (pass == 0 ? 0 : -1));
                });

        this.featureCancellationAppliers = new JsonPropertyApplierList<>(BiomePropertySelectors.FeatureCancellations.class)
                .register("namespace", String.class, BiomePropertySelectors.FeatureCancellations::putNamespace)
                .registerArrayApplier("namespaces", String.class, BiomePropertySelectors.FeatureCancellations::putNamespace)
                .register("type", FeatureCanceller.class, BiomePropertySelectors.FeatureCancellations::putCanceller)
                .registerArrayApplier("types", FeatureCanceller.class, BiomePropertySelectors.FeatureCancellations::putCanceller);

        this.postApplierEvent(this.biomeDatabaseAppliers, "entry_appliers");
        this.postApplierEvent(this.featureCancellationAppliers, "feature_cancellations");
    }

    private static ObjectFetchResult<BiomeDatabase.Operation> getOperation (final JsonElement jsonElement) {
        final ObjectFetchResult<JsonObject> jsonObjectFetchResult = JsonObjectGetters.JSON_OBJECT_GETTER.get(jsonElement);

        // If there was no Json object or method element, default to replace.
        if (!jsonObjectFetchResult.wasSuccessful() || !jsonObjectFetchResult.getValue().has(METHOD))
            return ObjectFetchResult.success(BiomeDatabase.Operation.REPLACE);

        final ObjectFetchResult<BiomeDatabase.Operation> operationFetchResult = JsonObjectGetters.OPERATION_GETTER.get(jsonObjectFetchResult.getValue().get(METHOD));

        if (!operationFetchResult.wasSuccessful())
            return ObjectFetchResult.failureFromOther(operationFetchResult);

        return ObjectFetchResult.success(operationFetchResult.getValue());
    }

    @Override
    protected void apply(Map<ResourceLocation, List<JsonElement>> preparedObject, IResourceManager resourceManager, boolean firstLoad) {
        // Ensure databases are reset.
        this.defaultDatabase = new BiomeDatabase();
        this.dimensionDatabases.clear();
        this.blacklistedDimensions.clear();

        // If world gen is disabled don't waste processing power reading these.
        if (!WorldGenRegistry.isWorldGenEnabled())
            return;

        final Event addFeatureCancellersEvent = new AddFeatureCancellersEvent(this.defaultDatabase);
        MinecraftForge.EVENT_BUS.post(addFeatureCancellersEvent);

        final Event populateDefaultDatabaseEvent = new PopulateDefaultDatabaseEvent(this.defaultDatabase);
        MinecraftForge.EVENT_BUS.post(populateDefaultDatabaseEvent);

        // TODO: Ordering should be checked.
        preparedObject.entrySet().stream().filter(this::isDefaultPopulator).forEach(defaultPopulator ->
                defaultPopulator.getValue().forEach(jsonElement -> this.readPopulator(this.defaultDatabase, defaultPopulator.getKey(), jsonElement, false)));

        final Event populateDimensionalDatabaseEvent = new PopulateDimensionalDatabaseEvent(this.dimensionDatabases, this.defaultDatabase);
        MinecraftForge.EVENT_BUS.post(populateDimensionalDatabaseEvent);

        preparedObject.entrySet().stream().filter(entry -> !this.isDefaultPopulator(entry)).forEach(dimensionalPopulator -> {
            final BiomeDatabase dimensionalDatabase = BiomeDatabase.copyOf(this.defaultDatabase);

            this.dimensionDatabases.put(dimensionalPopulator.getKey(), dimensionalDatabase);

            dimensionalPopulator.getValue().forEach(jsonElement -> {
                this.readPopulator(dimensionalDatabase, dimensionalPopulator.getKey(), jsonElement, false);
            });
        });

        // Blacklist certain dimensions according to the config.
        DTConfigs.dimensionBlacklist.get().forEach(resourceLocationString -> {
            try {
                this.blacklistedDimensions.add(new ResourceLocation(resourceLocationString));
            } catch (ResourceLocationException e) {
                LOGGER.warn("Couldn't get resource location for dimension blacklist in config: " + e.getMessage());
            }
        });
    }

    public void onCommonSetup() {
        final Map<ResourceLocation, List<JsonElement>> preparedObject = this.prepare(DTResourceRegistries.TREES_RESOURCE_MANAGER);

        // Ensure default database is reset.
        this.defaultDatabase = new BiomeDatabase();

        // If world gen is disabled don't waste processing power reading these.
        if (!WorldGenRegistry.isWorldGenEnabled())
            return;

        final Event addFeatureCancellersEvent = new AddFeatureCancellersEvent(this.defaultDatabase);
        MinecraftForge.EVENT_BUS.post(addFeatureCancellersEvent);

        // TODO: Ordering should be checked.
        preparedObject.entrySet().stream().filter(this::isDefaultPopulator).forEach(defaultPopulator ->
                defaultPopulator.getValue().forEach(jsonElement -> this.readPopulator(this.defaultDatabase, defaultPopulator.getKey(), jsonElement, true)));
    }

    private boolean isDefaultPopulator (final Map.Entry<ResourceLocation, List<JsonElement>> entry) {
        return entry.getKey().getPath().equals(DEFAULT_POPULATOR_NAME);
    }

    private void readPopulator (final BiomeDatabase database, final ResourceLocation resourceLocation, final JsonElement jsonElement, final boolean readCancellerOnly) {
        JsonHelper.JsonElementReader.of(jsonElement)
                .ifOfType(JsonArray.class, jsonArray ->
                        jsonArray.forEach(element ->
                                JsonHelper.JsonElementReader.of(element).ifOfType(JsonObject.class, jsonObject ->
                                        this.readPopulatorSection(database, resourceLocation, jsonObject, readCancellerOnly)
                                )))
                .elseWarn("Root element of populator '" + resourceLocation + "' was not a Json array.");
    }

    private void readPopulatorSection (final BiomeDatabase database, final ResourceLocation resourceLocation, final JsonObject jsonObject, final boolean readCancellerOnly) {
        final AtomicReference<BiomeList> biomeList = new AtomicReference<>();

        final JsonHelper.JsonObjectReader reader = JsonHelper.JsonObjectReader.of(jsonObject).ifContains(SELECT, selectElement ->
                JsonHelper.JsonElementReader.of(selectElement).ifOfType(BiomeList.class, biomeList::set));

        // Warn and don't reading the entry if we didn't get any biomes from it.
        if (biomeList.get() == null || biomeList.get().size() < 1) {
            LOGGER.warn("Couldn't get any biomes from Json object '{}' in '{}' populator.", jsonObject, resourceLocation);
            return;
        }

        if (!readCancellerOnly) {
            reader.ifContains(APPLY, applyElement ->
                    JsonHelper.JsonElementReader.of(applyElement).ifOfType(JsonObject.class, applyObject -> {
                        if (biomeList.get() == null || biomeList.get().size() == 0)
                            LogManager.getLogger().warn("Tried to apply to null or empty biome list in '" + resourceLocation + "' populator.");
                        else {
                            biomeList.get().forEach(biome -> this.biomeDatabaseAppliers.applyAll(applyObject, database.getEntry(biome)));
                        }
                    }))
            .ifContains(WHITE, String.class, str -> {
                if (str.equalsIgnoreCase("all")) {
                    database.getAllEntries().forEach(entry -> entry.setBlacklisted(false));
                } else if (str.equalsIgnoreCase("selected")) {
                    biomeList.get().forEach(biome -> database.getEntry(biome).setBlacklisted(false));
                } else
                    LOGGER.warn("Unknown value for key 'white' in populator '" + resourceLocation + "': '" + str + "'.");
            }).elseWarn("Error parsing key 'white' in populator '" + resourceLocation + "': ");
        }

        if (database == this.defaultDatabase) {
            reader.ifContains(CANCELLERS, JsonObject.class, cancellerObject -> {
                final BiomePropertySelectors.FeatureCancellations featureCancellations = new BiomePropertySelectors.FeatureCancellations();

                this.featureCancellationAppliers.applyAll(cancellerObject, featureCancellations)
                        .forEach(failureResult -> LOGGER.warn("Error whilst applying feature cancellations in '{}' populator: {}", resourceLocation, failureResult.getErrorMessage()));

                final AtomicReference<BiomeDatabase.Operation> operation = new AtomicReference<>(BiomeDatabase.Operation.SPLICE_AFTER);
                JsonHelper.JsonObjectReader.of(jsonObject).ifContains(METHOD, BiomeDatabase.Operation.class, operation::set)
                        .elseWarn("Error getting method in '" + resourceLocation + "' populator (defaulting to splice after): ");

                biomeList.get().forEach(biome -> {
                    BiomePropertySelectors.FeatureCancellations currentFeatureCancellations = database.getEntry(biome).getFeatureCancellations();

                    if (operation.get() == BiomeDatabase.Operation.REPLACE)
                        currentFeatureCancellations.reset();

                    currentFeatureCancellations.copyFrom(featureCancellations);
                });
            });
        }
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

    @Override
    public void load(IResourceManager resourceManager) { }

}
