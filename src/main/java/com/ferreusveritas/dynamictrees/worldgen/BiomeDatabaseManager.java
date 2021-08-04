package com.ferreusveritas.dynamictrees.worldgen;

import com.ferreusveritas.dynamictrees.api.events.AddFeatureCancellersEvent;
import com.ferreusveritas.dynamictrees.api.events.PopulateDefaultDatabaseEvent;
import com.ferreusveritas.dynamictrees.api.events.PopulateDimensionalDatabaseEvent;
import com.ferreusveritas.dynamictrees.api.treepacks.PropertyApplierResult;
import com.ferreusveritas.dynamictrees.api.worldgen.BiomePropertySelectors;
import com.ferreusveritas.dynamictrees.api.worldgen.FeatureCanceller;
import com.ferreusveritas.dynamictrees.init.DTConfigs;
import com.ferreusveritas.dynamictrees.resources.DTResourceRegistries;
import com.ferreusveritas.dynamictrees.resources.MultiJsonReloadListener;
import com.ferreusveritas.dynamictrees.resources.TreesResourceManager;
import com.ferreusveritas.dynamictrees.util.BiomeList;
import com.ferreusveritas.dynamictrees.util.json.JsonHelper;
import com.ferreusveritas.dynamictrees.util.json.JsonObjectGetters;
import com.ferreusveritas.dynamictrees.util.json.JsonPropertyApplierList;
import com.ferreusveritas.dynamictrees.util.json.ObjectFetchResult;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.resources.IResourceManager;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.ResourceLocationException;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.gen.GenerationStage;
import net.minecraftforge.common.MinecraftForge;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

/**
 * Manages {@link BiomeDatabase} objects, reading from tree packs. Main instance stored in {@link
 * DTResourceRegistries}.
 *
 * @author Harley O'Connor
 */
public final class BiomeDatabaseManager extends MultiJsonReloadListener<Object> {

    private static final Logger LOGGER = LogManager.getLogger();

    private static final String DEFAULT_POPULATOR = "default";

    private BiomeDatabase defaultDatabase = new BiomeDatabase();
    private final Map<ResourceLocation, BiomeDatabase> dimensionDatabases = Maps.newHashMap();

    private final JsonPropertyApplierList<BiomeDatabase.Entry> biomeDatabaseAppliers = new JsonPropertyApplierList<>(BiomeDatabase.Entry.class);
    private final JsonPropertyApplierList<BiomePropertySelectors.FeatureCancellations> featureCancellationAppliers = new JsonPropertyApplierList<>(BiomePropertySelectors.FeatureCancellations.class);

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
    public void registerAppliers() {
        this.biomeDatabaseAppliers
                .register("species", JsonElement.class, (entry, jsonElement) -> {
                    return PropertyApplierResult.from(JsonObjectGetters.SPECIES_SELECTOR.get(jsonElement)
                            .ifSuccessful(speciesSelector ->
                                    entry.getDatabase().setSpeciesSelector(entry.getBiome(), speciesSelector, getOperationOrWarn(jsonElement))
                            ));
                })
                .register("density", JsonElement.class, (entry, jsonElement) -> {
                    return PropertyApplierResult.from(JsonObjectGetters.DENSITY_SELECTOR.get(jsonElement)
                            .ifSuccessful(densitySelector ->
                                    entry.getDatabase().setDensitySelector(entry.getBiome(), densitySelector, getOperationOrWarn(jsonElement))
                            ));
                })
                .register("chance", JsonElement.class, (entry, jsonElement) -> {
                    return PropertyApplierResult.from(JsonObjectGetters.CHANCE_SELECTOR.get(jsonElement)
                            .ifSuccessful(chanceSelector ->
                                    entry.getDatabase().setChanceSelector(entry.getBiome(), chanceSelector, getOperationOrWarn(jsonElement))
                            ));
                })
                .register("multipass", Boolean.class, (entry, multipass) -> {
                    if (!multipass) {
                        return;
                    }

                    entry.getDatabase().setMultipass(entry.getBiome(), pass -> {
                        switch (pass) {
                            case 0:
                                return 0; // Zero means to run as normal.
                            case 1:
                                return 5; // Return only radius 5 on pass 1.
                            case 2:
                                return 3; // Return only radius 3 on pass 2.
                            default:
                                return -1; // A negative number means to terminate.
                        }
                    });
                })
                .register("multipass", JsonObject.class, (entry, multipass) -> {
                    final Map<Integer, Integer> passMap = Maps.newHashMap();

                    for (final Map.Entry<String, JsonElement> passEntry : multipass.entrySet()) {
                        try {
                            final int pass = Integer.parseInt(passEntry.getKey());
                            final int radius = JsonObjectGetters.INTEGER.get(passEntry.getValue()).orDefault(-1);

                            // Terminate when radius is -1.
                            if (radius == -1) {
                                break;
                            }

                            passMap.put(pass, radius);
                        } catch (NumberFormatException ignored) {
                        }
                    }

                    entry.getDatabase().setMultipass(entry.getBiome(), pass -> passMap.getOrDefault(pass, -1));
                })
                .register("blacklist", Boolean.class, BiomeDatabase.Entry::setBlacklisted)
                .register("forestness", Float.class, BiomeDatabase.Entry::setForestness)
                .register("subterranean", Boolean.class, BiomeDatabase.Entry::setSubterraneanBiome)
                .registerIfTrueApplier("reset", entry -> {
                    final BiomeDatabase database = entry.getDatabase();
                    final Biome biome = entry.getBiome();

                    database.setSpeciesSelector(biome, (pos, dirt, rnd) -> new BiomePropertySelectors.SpeciesSelection(), BiomeDatabase.Operation.REPLACE)
                            .setDensitySelector(biome, (rnd, nd) -> -1, BiomeDatabase.Operation.REPLACE)
                            .setChanceSelector(biome, (rnd, spc, rad) -> BiomePropertySelectors.Chance.UNHANDLED, BiomeDatabase.Operation.REPLACE)
                            .setForestness(biome, 0.0f)
                            .setIsSubterranean(biome, false)
                            .setMultipass(biome, pass -> (pass == 0 ? 0 : -1));
                });

        this.featureCancellationAppliers.register("namespace", String.class, BiomePropertySelectors.FeatureCancellations::putNamespace)
                .registerArrayApplier("namespaces", String.class, BiomePropertySelectors.FeatureCancellations::putNamespace)
                .register("type", FeatureCanceller.class, BiomePropertySelectors.FeatureCancellations::putCanceller)
                .registerArrayApplier("types", FeatureCanceller.class, BiomePropertySelectors.FeatureCancellations::putCanceller)
                .register("stage", GenerationStage.Decoration.class, BiomePropertySelectors.FeatureCancellations::putStage)
                .registerArrayApplier("stages", GenerationStage.Decoration.class, BiomePropertySelectors.FeatureCancellations::putStage);

        this.postApplierEvent(this.biomeDatabaseAppliers, "entry_appliers");
        this.postApplierEvent(this.featureCancellationAppliers, "feature_cancellations");
    }

    private static ObjectFetchResult<BiomeDatabase.Operation> getOperation(final JsonElement jsonElement) {
        return JsonObjectGetters.JSON_OBJECT.get(jsonElement)
                .map(jsonObject -> jsonObject.has(METHOD) ? jsonObject.get(METHOD) : null).map(JsonObjectGetters.OPERATION::get)
                // If there was no Json object or method element, default to replace.
                .orDefault(ObjectFetchResult.success(BiomeDatabase.Operation.REPLACE));
    }

    public static BiomeDatabase.Operation getOperationOrWarn(final JsonElement jsonElement) {
        return getOperation(jsonElement).otherwiseWarn("Error getting operation (defaulting to replace): ").orDefault(BiomeDatabase.Operation.REPLACE);
    }

    @Override
    protected void apply(Map<ResourceLocation, List<JsonElement>> preparedObject, TreesResourceManager resourceManager, ApplicationType applicationType) {
        if (applicationType == ApplicationType.SETUP) {
            // Ensure default database is reset.
            this.defaultDatabase = new BiomeDatabase();

            // If world gen is disabled don't waste processing power reading these.
            if (!DTConfigs.WORLD_GEN.get()) {
                return;
            }

            MinecraftForge.EVENT_BUS.post(new AddFeatureCancellersEvent(this.defaultDatabase));

            this.readDefaultPopulators(preparedObject.entrySet().stream().filter(this::isDefaultPopulator).collect(Collectors.toList()), true);
            return;
        }

        // Ensure databases are reset.
        this.defaultDatabase = new BiomeDatabase();
        this.dimensionDatabases.clear();
        this.blacklistedDimensions.clear();

        // If world gen is disabled don't waste processing power reading these.
        if (!DTConfigs.WORLD_GEN.get()) {
            return;
        }

        MinecraftForge.EVENT_BUS.post(new AddFeatureCancellersEvent(this.defaultDatabase));
        MinecraftForge.EVENT_BUS.post(new PopulateDefaultDatabaseEvent(this.defaultDatabase));

        // Read the default populators.
        this.readDefaultPopulators(preparedObject.entrySet().stream().filter(this::isDefaultPopulator).collect(Collectors.toList()), false);

        final List<Map.Entry<ResourceLocation, List<JsonElement>>> dimensionalPopulators =
                preparedObject.entrySet().stream().filter(entry -> !this.isDefaultPopulator(entry)).collect(Collectors.toList());

        MinecraftForge.EVENT_BUS.post(new PopulateDimensionalDatabaseEvent(this.dimensionDatabases, this.defaultDatabase));

        // Reads all dimensional populators added by base DT/add-ons first.
        dimensionalPopulators.forEach(dimensionalPopulator -> {
            final BiomeDatabase dimensionalDatabase = BiomeDatabase.copyOf(this.defaultDatabase);
            this.dimensionDatabases.put(dimensionalPopulator.getKey(), dimensionalDatabase);

            this.readPopulator(dimensionalDatabase, dimensionalPopulator.getKey(), dimensionalPopulator.getValue().get(0), false);
            dimensionalPopulator.getValue().remove(0);
        });

        // Read any dimensional populators added by tree packs after.
        dimensionalPopulators.forEach(dimensionalPopulator -> dimensionalPopulator.getValue().forEach(jsonElement ->
                this.readPopulator(this.dimensionDatabases.get(dimensionalPopulator.getKey()), dimensionalPopulator.getKey(), jsonElement, false)));

        // Blacklist certain dimensions according to the config.
        DTConfigs.DIMENSION_BLACKLIST.get().forEach(resourceLocationString -> {
            try {
                this.blacklistedDimensions.add(new ResourceLocation(resourceLocationString));
            } catch (ResourceLocationException e) {
                LOGGER.warn("Couldn't get resource location for dimension blacklist in config: " + e.getMessage());
            }
        });
    }

    private void readDefaultPopulators(final List<Map.Entry<ResourceLocation, List<JsonElement>>> defaultPopulators, final boolean readCancellerOnly) {
        // Reads all populators added by base DT/add-ons first.
        defaultPopulators.forEach(defaultPopulator -> {
            this.readPopulator(this.defaultDatabase, defaultPopulator.getKey(), defaultPopulator.getValue().get(0), readCancellerOnly);
            defaultPopulator.getValue().remove(0);
        });

        // Read any populators added by tree packs after.
        defaultPopulators.forEach(defaultPopulator -> defaultPopulator.getValue().forEach(jsonElement ->
                this.readPopulator(this.defaultDatabase, defaultPopulator.getKey(), jsonElement, readCancellerOnly)));
    }

    private boolean isDefaultPopulator(final Map.Entry<ResourceLocation, List<JsonElement>> entry) {
        return entry.getKey().getPath().equals(DEFAULT_POPULATOR);
    }

    private void readPopulator(final BiomeDatabase database, final ResourceLocation resourceLocation, final JsonElement jsonElement, final boolean readCancellerOnly) {
        LOGGER.debug("Loading Json biome populator '{}'.", resourceLocation);
        JsonHelper.JsonElementReader.of(jsonElement).ifArrayForEach(JsonObject.class, jsonObject ->
                        this.readPopulatorSection(database, resourceLocation, jsonObject, readCancellerOnly))
                .elseWarn("Root element of populator '" + resourceLocation + "' was not a Json array.");
    }

    private void readPopulatorSection(final BiomeDatabase database, final ResourceLocation resourceLocation, final JsonObject jsonObject, final boolean readCancellerOnly) {
        final AtomicReference<BiomeList> biomeList = new AtomicReference<>();

        if (!this.shouldLoad(jsonObject, error -> LOGGER.error("Error loading populator '{}': {}", resourceLocation, error))) {
            return;
        }

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
                                if (biomeList.get() == null || biomeList.get().size() == 0) {
                                    LogManager.getLogger().warn("Tried to apply to null or empty biome list in '" + resourceLocation + "' populator.");
                                } else {
                                    biomeList.get().forEach(biome -> this.biomeDatabaseAppliers.applyAll(applyObject, database.getEntry(biome)));
                                }
                            }))
                    .ifContains(WHITE, String.class, str -> {
                        if (str.equalsIgnoreCase("all")) {
                            database.getAllEntries().forEach(entry -> entry.setBlacklisted(false));
                        } else if (str.equalsIgnoreCase("selected")) {
                            biomeList.get().forEach(biome -> database.getEntry(biome).setBlacklisted(false));
                        } else {
                            LOGGER.warn("Unknown value for key 'white' in populator '" + resourceLocation + "': '" + str + "'.");
                        }
                    }).elseWarn("Error parsing key 'white' in populator '" + resourceLocation + "': ");
        }

        if (database == this.defaultDatabase) {
            reader.ifContains(CANCELLERS, JsonObject.class, cancellerObject -> {
                final BiomePropertySelectors.FeatureCancellations featureCancellations = new BiomePropertySelectors.FeatureCancellations();

                this.featureCancellationAppliers.applyAll(cancellerObject, featureCancellations)
                        .forEach(failureResult -> LOGGER.error("Error whilst applying feature cancellations in '{}' populator: {}", resourceLocation, failureResult.getErrorMessage()));

                featureCancellations.putDefaultStagesIfEmpty();

                final AtomicReference<BiomeDatabase.Operation> operation = new AtomicReference<>(BiomeDatabase.Operation.SPLICE_AFTER);
                JsonHelper.JsonObjectReader.of(jsonObject).ifContains(METHOD, BiomeDatabase.Operation.class, operation::set)
                        .elseWarn("Error getting method in '" + resourceLocation + "' populator (defaulting to splice after): ");

                biomeList.get().forEach(biome -> {
                    BiomePropertySelectors.FeatureCancellations currentFeatureCancellations = database.getEntry(biome).getFeatureCancellations();

                    if (operation.get() == BiomeDatabase.Operation.REPLACE) {
                        currentFeatureCancellations.reset();
                    }

                    currentFeatureCancellations.copyFrom(featureCancellations);
                });
            });
        } else {
            // If we detect a canceller in a dimensional database, log a warning.
            reader.ifContains(CANCELLERS, JsonObject.class, cancellerObject -> {
                LOGGER.warn("Feature canceller entry found in biome database for dimension '{}'! " +
                        "It will be ignored as feature cancellers only work in the default populator.", resourceLocation);
            });
        }
    }

    public BiomeDatabase getDefaultDatabase() {
        return this.defaultDatabase;
    }

    /**
     * Returns the {@link BiomeDatabase} object for the given dimension registry name, or the default database if one
     * did not exist for the dimension.
     *
     * @param dimensionRegistryName The dimension registry name.
     * @return The dimension's {@link BiomeDatabase} object, or the default one.
     */
    public BiomeDatabase getDimensionDatabase(ResourceLocation dimensionRegistryName) {
        return this.dimensionDatabases.getOrDefault(dimensionRegistryName, this.defaultDatabase);
    }

    public boolean validateDatabases() {
        return this.defaultDatabase.isValid() && this.dimensionDatabases.values().stream().allMatch(BiomeDatabase::isValid);
    }

    public boolean isDimensionBlacklisted(ResourceLocation resourceLocation) {
        return this.blacklistedDimensions.contains(resourceLocation);
    }

    /**
     * Do nothing on load, since we don't need to set anything up on game setup here.
     *
     * @param resourceManager The {@link IResourceManager} object.
     * @return A {@link CompletableFuture} that does nothing.
     */
    @Override
    public CompletableFuture<Void> load(TreesResourceManager resourceManager) {
        return CompletableFuture.runAsync(() -> {
        });
    }

}
