package com.ferreusveritas.dynamictrees.resources.loader;

import com.ferreusveritas.dynamictrees.api.applier.ApplierRegistryEvent;
import com.ferreusveritas.dynamictrees.api.applier.PropertyApplierResult;
import com.ferreusveritas.dynamictrees.api.resource.ResourceAccessor;
import com.ferreusveritas.dynamictrees.api.resource.loading.AbstractResourceLoader;
import com.ferreusveritas.dynamictrees.api.resource.loading.ApplierResourceLoader;
import com.ferreusveritas.dynamictrees.api.resource.loading.preparation.MultiJsonResourcePreparer;
import com.ferreusveritas.dynamictrees.deserialisation.BiomeListDeserialiser;
import com.ferreusveritas.dynamictrees.deserialisation.DeserialisationException;
import com.ferreusveritas.dynamictrees.deserialisation.JsonDeserialisers;
import com.ferreusveritas.dynamictrees.deserialisation.JsonPropertyAppliers;
import com.ferreusveritas.dynamictrees.deserialisation.result.JsonResult;
import com.ferreusveritas.dynamictrees.deserialisation.result.Result;
import com.ferreusveritas.dynamictrees.init.DTConfigs;
import com.ferreusveritas.dynamictrees.resources.Resources;
import com.ferreusveritas.dynamictrees.util.CommonCollectors;
import com.ferreusveritas.dynamictrees.util.JsonMapWrapper;
import com.ferreusveritas.dynamictrees.util.holderset.DTBiomeHolderSet;
import com.ferreusveritas.dynamictrees.worldgen.BiomeDatabase;
import com.ferreusveritas.dynamictrees.worldgen.BiomeDatabases;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.world.level.biome.Biome;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Collection;
import java.util.Deque;
import java.util.LinkedList;
import java.util.Map;
import java.util.function.Consumer;
import java.util.stream.StreamSupport;

/**
 * @author Harley O'Connor
 */
public final class BiomePopulatorsResourceLoader extends AbstractResourceLoader<Iterable<JsonElement>>
        implements ApplierResourceLoader<Iterable<JsonElement>> {

    private static final MultiJsonResourcePreparer RESOURCE_PREPARER = new MultiJsonResourcePreparer("world_gen");

    private static final Logger LOGGER = LogManager.getLogger();

    private static final String DEFAULT_POPULATOR = "default";

    public static final String SELECT = "select";
    public static final String METHOD = "method";

    private static final String APPLY = "apply";
    private static final String WHITE = "white";

    private static final String CAVE_ROOTED = "cave_rooted";

    public static final String ENTRY_APPLIERS = "entries";

    static boolean isWorldGenDisabled() {
        return !DTConfigs.WORLD_GEN.get();
    }

    static LinkedList<JsonElement> toLinkedList(Iterable<JsonElement> elements) {
        return StreamSupport.stream(elements.spliterator(), false)
                .collect(CommonCollectors.toLinkedList());
    }

    static DTBiomeHolderSet collectBiomes(JsonObject json, Consumer<String> warningConsumer) throws DeserialisationException {
        return JsonResult.forInput(json)
                .mapIfContains(SELECT, DTBiomeHolderSet.class, list -> list)
                .forEachWarning(warningConsumer)
                .orElseThrow();
    }

    static void warnNoBiomesSelected(JsonObject json) {
        if (noBiomesSelectedWarningNotSuppressed(json)) {
            LOGGER.warn("Could not get any biomes from selector:\n" + json.get(SELECT));
        }
    }

    private static boolean noBiomesSelectedWarningNotSuppressed(JsonObject json) {
        final JsonElement suppress = json.get("suppress_none_selected");
        return suppress == null || !suppress.isJsonPrimitive() || !suppress.getAsJsonPrimitive().isBoolean() ||
                !suppress.getAsJsonPrimitive().getAsBoolean();
    }

    private final JsonPropertyAppliers<BiomeDatabase.JsonEntry> entryAppliers = new JsonPropertyAppliers<>(BiomeDatabase.JsonEntry.class);
    private final JsonPropertyAppliers<BiomeDatabase.CaveRootedData> caveRootedDataAppliers = new JsonPropertyAppliers<>(BiomeDatabase.CaveRootedData.class);

    public BiomePopulatorsResourceLoader() {
        super(RESOURCE_PREPARER);
    }

    @Override
    public void registerAppliers() {
        this.entryAppliers
                .register("species", JsonElement.class, this::applySpecies)
                .register("density", JsonElement.class, this::applyDensity)
                .register("chance", JsonElement.class, this::applyChance)
                .register("multipass", Boolean.class, this::applyMultipass)
                .register("multipass", JsonObject.class, BiomeDatabase.BaseEntry::setCustomMultipass)
                .register("blacklist", Boolean.class, BiomeDatabase.BaseEntry::setBlacklisted)
                .register("forestness", Float.class, BiomeDatabase.BaseEntry::setForestness)
                .register("heightmap", String.class, BiomeDatabase.BaseEntry::setHeightmap)
                .register("force", Boolean.class, BiomeDatabase.JsonEntry::setForce);

        this.caveRootedDataAppliers
                .register("generate_on_surface", Boolean.class, BiomeDatabase.CaveRootedData::setGenerateOnSurface)
                .register("max_dist_to_surface", Integer.class, BiomeDatabase.CaveRootedData::setMaxDistToSurface);

        ApplierResourceLoader.postApplierEvent(new EntryApplierRegistryEvent<>(this.entryAppliers, ENTRY_APPLIERS));
    }

    public static final class EntryApplierRegistryEvent<O> extends ApplierRegistryEvent<O, JsonElement> {
        public EntryApplierRegistryEvent(JsonPropertyAppliers<O> appliers, String identifier) {
            super(appliers, identifier);
        }
    }

    private PropertyApplierResult applySpecies(BiomeDatabase.BaseEntry entry, JsonElement jsonElement) {
        return PropertyApplierResult.from(JsonDeserialisers.SPECIES_SELECTOR.deserialise(jsonElement)
                .ifSuccess(speciesSelector ->
                        entry.setSpeciesSelector(speciesSelector, getOperationOrWarn(jsonElement))
                ));
    }

    private PropertyApplierResult applyDensity(BiomeDatabase.BaseEntry entry, JsonElement jsonElement) {
        return PropertyApplierResult.from(JsonDeserialisers.DENSITY_SELECTOR.deserialise(jsonElement)
                .ifSuccess(densitySelector ->
                        entry.setDensitySelector(densitySelector, getOperationOrWarn(jsonElement))
                ));
    }

    private PropertyApplierResult applyChance(BiomeDatabase.BaseEntry entry, JsonElement jsonElement) {
        return PropertyApplierResult.from(JsonDeserialisers.CHANCE_SELECTOR.deserialise(jsonElement)
                .ifSuccess(chanceSelector ->
                        entry.setChanceSelector(chanceSelector, getOperationOrWarn(jsonElement))
                ));
    }

    private void applyMultipass(BiomeDatabase.BaseEntry entry, Boolean multipass) {
        if (!multipass) {
            return;
        }
        entry.enableDefaultMultipass();
    }

    public static BiomeDatabase.Operation getOperationOrWarn(final JsonElement jsonElement) {
        return getOperation(jsonElement).orElse(BiomeDatabase.Operation.REPLACE, LOGGER::error, LOGGER::warn);
    }

    private static Result<BiomeDatabase.Operation, JsonElement> getOperation(final JsonElement input) {
        return JsonDeserialisers.JSON_OBJECT.deserialise(input)
                .removeError() // Remove error at this point as we don't want to warn when element is not Json object.
                .map(jsonObject -> jsonObject.has(METHOD) ? jsonObject.get(METHOD) : null)
                .map(JsonDeserialisers.OPERATION::deserialise)
                .orElseApply(error -> JsonResult.failure(input, "Error getting operation (defaulting to " +
                                "replace): " + error),
                        JsonResult.success(input, BiomeDatabase.Operation.REPLACE));
    }

    private void readCaveRootedPopulatorSection(BiomeDatabase database, ResourceLocation location, JsonObject json) throws DeserialisationException {
        final DTBiomeHolderSet biomes = collectBiomes(json, warning -> LOGGER.warn("Warning whilst loading cave rooted populator \"{}\": {}", location, warning));
        if (biomes != null)
            applyCaveRootedPopulatorSection(database, json.getAsJsonObject(APPLY), biomes);
    }

    @Override
    public void applyOnReload(ResourceAccessor<Iterable<JsonElement>> resourceAccessor, ResourceManager resourceManager) {
        BiomeDatabases.reset();
//        if (isWorldGenDisabled()) {
//            return;
//        }

        this.readPopulators(
                resourceAccessor.filtered(this::isDefaultPopulator).map(BiomePopulatorsResourceLoader::toLinkedList)
        );
        this.readDimensionalPopulators(
                resourceAccessor
                        .filtered(resource -> !this.isDefaultPopulator(resource) && !FeatureCancellationResourceLoader.isCancellationFile(resource))
                        .map(BiomePopulatorsResourceLoader::toLinkedList)
        );
    }

    private void readPopulators(ResourceAccessor<Deque<JsonElement>> resourceAccessor) {
        this.readModPopulators(BiomeDatabases.getDefault(), resourceAccessor);
        this.readTreePackPopulators(BiomeDatabases.getDefault(), resourceAccessor);
    }

    private void readModPopulators(BiomeDatabase database, ResourceAccessor<Deque<JsonElement>> resourceAccessor) {
        resourceAccessor.getAllResources().forEach(defaultPopulator ->
                this.readPopulator(database, defaultPopulator.getLocation(), defaultPopulator.getResource().pollFirst())
        );
    }

    private void readTreePackPopulators(BiomeDatabase database, ResourceAccessor<Deque<JsonElement>> resourceAccessor) {
        resourceAccessor.getAllResources().forEach(defaultPopulator ->
                defaultPopulator.getResource().forEach(jsonElement ->
                        this.readPopulator(database, defaultPopulator.getLocation(), jsonElement))
        );
    }

    private void readDimensionalPopulators(ResourceAccessor<Deque<JsonElement>> resourceAccessor) {
        this.readDimensionalModPopulators(resourceAccessor);
        this.readDimensionalTreePackPopulators(resourceAccessor);
    }

    private void readDimensionalModPopulators(ResourceAccessor<Deque<JsonElement>> resourceAccessor) {
        resourceAccessor.getAllResources().forEach(dimensionalPopulator ->
                this.readDimensionalPopulator(dimensionalPopulator.getLocation(), dimensionalPopulator.getResource().pollFirst())
        );
    }

    private void readDimensionalTreePackPopulators(ResourceAccessor<Deque<JsonElement>> resourceAccessor) {
        resourceAccessor.getAllResources().forEach(dimensionalPopulator ->
                dimensionalPopulator.getResource().forEach(json -> this.readDimensionalPopulator(dimensionalPopulator.getLocation(), json))
        );
    }

    private void readDimensionalPopulator(ResourceLocation dimensionLocation, JsonElement dimensionalPopulator) {
        this.readPopulator(BiomeDatabases.getOrCreateDimensional(dimensionLocation), dimensionLocation, dimensionalPopulator);
    }

    private void readPopulator(BiomeDatabase database, ResourceLocation location, JsonElement json) {
        LOGGER.debug("Loading Json biome populator \"{}\".", location);

        try {
            JsonResult.forInput(json)
                    .mapEachIfArray(JsonObject.class, object -> {
                        this.readPopulatorSection(database, location, object);
                        if (object.has(APPLY) && object.get(APPLY).isJsonObject() && object.get(APPLY).getAsJsonObject().has(CAVE_ROOTED)) {
                            this.readCaveRootedPopulatorSection(database, location, object);
                        }
                        return PropertyApplierResult.success();
                    }).forEachWarning(warning ->
                            LOGGER.warn("Warning whilst loading populator \"{}\": {}", location, warning)
                    ).orElseThrow();
        } catch (DeserialisationException e) {
            LOGGER.error("Error loading populator \"{}\": {}", location, e.getMessage());
        }
    }

    private void readPopulatorSection(BiomeDatabase database, ResourceLocation location, JsonObject json)
            throws DeserialisationException {

        final DTBiomeHolderSet biomes = collectBiomes(json, warning ->
                LOGGER.warn("Warning whilst loading populator \"{}\": {}", location, warning));
//        if (biomes == null || biomes.size() == 0) {
//            warnNoBiomesSelected(json);
//            return;
//        }

//        JsonResult.forInput(json)
//                .mapIfContains(APPLY, JsonObject.class, applyObject -> {
//                    if (BiomeDatabases.getDefault() == database) {
//                        applyCaveRootedPopulatorSection(database, applyObject, biomes);
//                    }
//                    biomes.getList().forEach(biome -> this.entryAppliers.applyAll(new JsonMapWrapper(applyObject), database.getJsonEntry(biomes)));
//                    return PropertyApplierResult.success();
//                }, PropertyApplierResult.success())
//                .elseMapIfContains(WHITE, String.class, type -> {
//                    this.applyWhite(database, location, biomes, type);
//                    return PropertyApplierResult.success();
//                }, PropertyApplierResult.success())
//                .forEachWarning(warning ->
//                        LOGGER.warn("Warning whilst loading populator \"{}\": {}", location, warning))
//                .orElseThrow();
        JsonResult.forInput(json)
                .mapIfContains(APPLY, JsonObject.class, applyObject -> {
                    var entry = database.getJsonEntry(biomes);
                    this.entryAppliers.applyAll(new JsonMapWrapper(applyObject), entry);
                    return PropertyApplierResult.success();
                }, PropertyApplierResult.success())
                .elseMapIfContains(WHITE, String.class, type -> {
                    this.applyWhite(database, location, biomes, type);
                    return PropertyApplierResult.success();
                }, PropertyApplierResult.success())
                .forEachWarning(warning ->
                        LOGGER.warn("Warning whilst loading populator \"{}\": {}", location, warning))
                .orElseThrow();
    }

    private void applyCaveRootedPopulatorSection(BiomeDatabase database, JsonObject json, DTBiomeHolderSet biomes) {
        if (json.has(CAVE_ROOTED) && json.get(CAVE_ROOTED).isJsonObject()) {
            JsonObject caveRootedJson = json.getAsJsonObject(CAVE_ROOTED);
            JsonMapWrapper applyData = new JsonMapWrapper(caveRootedJson);
            var entry = database.getJsonEntry(biomes);
            this.entryAppliers.applyAll(applyData, entry);
            this.caveRootedDataAppliers.applyAll(applyData, entry.getOrCreateCaveRootedData());
        }
    }

    private void applyWhite(BiomeDatabase database, ResourceLocation location, DTBiomeHolderSet biomes, String type)
            throws DeserialisationException {
        if (type.equalsIgnoreCase("all")) {
            database.getAllEntries().forEach(entry -> entry.setBlacklisted(false));
        } else if (type.equalsIgnoreCase("selected")) {
            biomes.forEach(biome -> database.getEntry(biome).setBlacklisted(false));
        } else {
            throw new DeserialisationException("Unknown type for whitelist in populator \"" +
                    location + "\": \"" + type + "\".");
        }
    }

    private boolean isDefaultPopulator(final ResourceLocation key) {
        return key.getPath().equals(DEFAULT_POPULATOR);
    }

}
