package com.ferreusveritas.dynamictrees.trees;

import com.ferreusveritas.dynamictrees.api.datapacks.*;
import com.ferreusveritas.dynamictrees.blocks.leaves.LeavesProperties;
import com.ferreusveritas.dynamictrees.growthlogic.GrowthLogicKit;
import com.ferreusveritas.dynamictrees.items.Seed;
import com.ferreusveritas.dynamictrees.systems.DirtHelper;
import com.ferreusveritas.dynamictrees.systems.genfeatures.config.ConfiguredGenFeature;
import com.ferreusveritas.dynamictrees.util.json.JsonObjectGetters;
import com.ferreusveritas.dynamictrees.util.json.JsonPropertyApplierList;
import com.ferreusveritas.dynamictrees.util.json.ObjectFetchResult;
import com.google.gson.*;
import net.minecraft.block.Block;
import net.minecraft.client.resources.JsonReloadListener;
import net.minecraft.profiler.IProfiler;
import net.minecraft.resources.IResourceManager;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.BiomeDictionary;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Map;

/**
 * @author Harley O'Connor
 */
public final class SpeciesManager extends JsonReloadListener implements IJsonApplierManager {

    private static final Gson GSON = (new GsonBuilder()).setPrettyPrinting().disableHtmlEscaping().create();
    private static final Logger LOGGER = LogManager.getLogger();

    /** A {@link JsonPropertyApplierList} for applying environment factors to {@link Species} objects. (based on biome type). */
    private final JsonPropertyApplierList<Species> environmentFactorAppliers = new JsonPropertyApplierList<>(Species.class);

    /** A {@link JsonPropertyApplierList} for applying properties to {@link Species} objects. */
    private final JsonPropertyApplierList<Species> appliers = new JsonPropertyApplierList<>(Species.class);

    /** A {@link JsonApplierRegistryEvent} so that add-ons can registry custom appliers. This is given {@link #appliers}. */
    public final JsonApplierRegistryEvent<Species> applierRegistryEvent = new JsonApplierRegistryEvent<>(this.appliers, JsonApplierRegistryEvent.SPECIES);

    public SpeciesManager() {
        super(GSON, "trees/species");
        this.registerAppliers();
    }

    @Override
    public void registerAppliers() {
        BiomeDictionary.Type.getAll().stream().map(type -> new JsonPropertyApplier<>(type.toString().toLowerCase(), Species.class, Float.class, (species, factor) -> species.envFactor(type, factor)))
                .forEach(environmentFactorAppliers::register);

        this.appliers.register("family", TreeFamily.class, Species::setFamily)
                .register("tapering", Float.class, Species::setTapering)
                .register("up_probability", Integer.class, Species::setUpProbability)
                .register("lowest_branch_height", Integer.class, Species::setLowestBranchHeight)
                .register("signal_energy", Float.class, Species::setSignalEnergy)
                .register("growth_rate", Float.class, Species::setGrowthRate)
                .register("soil_longevity", Integer.class, Species::setSoilLongevity)
                .register("max_branch_radius", Integer.class, Species::setMaxBranchRadius)
                .register("transformable", Boolean.class, Species::setTransformable)
                .register("growth_logic_kit", GrowthLogicKit.class, Species::setGrowthLogicKit)
                .register("leaves_properties", LeavesProperties.class, Species::setLeavesProperties)
                .register("environment_factors", JsonObject.class, (species, jsonObject) ->
                        jsonObject.entrySet().forEach(entry -> readEntry(environmentFactorAppliers, species, entry.getKey(), entry.getValue())))
                .register("seed_drop_rarity", Float.class, Species::setupStandardSeedDropping)
                .register("stick_drop_rarity", Float.class, Species::setupStandardStickDropping)
                .register("primitive_sapling", Block.class, Species::setPrimitiveSapling)
                .register("seed", Seed.class, Species::setSeed)
                .registerArrayApplier("acceptable_soils", String.class, (species, acceptableSoil) -> {
                    if (DirtHelper.getSoilFlags(acceptableSoil) == 0)
                        return new PropertyApplierResult("Could not find acceptable soil '" + acceptableSoil + "'.");

                    species.addAcceptableSoils(acceptableSoil);
                    return PropertyApplierResult.SUCCESS;
                })
                .registerArrayApplier("features", ConfiguredGenFeature.NULL_CONFIGURED_FEATURE.getClass(), Species::addGenFeature);

        this.fireEvent(this.applierRegistryEvent);
    }

    @Override
    protected void apply(Map<ResourceLocation, JsonElement> jsonFiles, IResourceManager resourceManager, IProfiler profiler) {
        jsonFiles.forEach((resourceLocation, jsonElement) -> {
            LOGGER.debug("Attempting to load species data for {}.", resourceLocation);
            final Species species = Species.REGISTRY.getValue(resourceLocation);

            if (species == null || !species.isValid()) {
                LOGGER.warn("Skipping loading data for species '{}' due to it not being registered or null.", resourceLocation);
                return;
            }

            final ObjectFetchResult<JsonObject> jsonObjectFetchResult = JsonObjectGetters.JSON_OBJECT_GETTER.get(jsonElement);

            if (!jsonObjectFetchResult.wasSuccessful()) {
                LOGGER.warn("Skipping loading data for species '{}' due to error: {}", resourceLocation, jsonObjectFetchResult.getErrorMessage());
                return;
            }

            species.clearGenFeatures();
            species.clearAcceptableSoils();

            final JsonObject jsonObject = jsonObjectFetchResult.getValue();
            jsonObject.entrySet().forEach(entry -> readEntry(this.appliers, species, entry.getKey(), entry.getValue()));

            // If no acceptable soils were set, default to DIRT_LIKE.
            if (!species.hasAcceptableSoil())
                species.addAcceptableSoils(DirtHelper.DIRT_LIKE);

            LOGGER.debug("Injected species data: " + species.getDisplayInfo());
        });
    }

    private static void readEntry (final JsonPropertyApplierList<Species> propertyAppliers, final Species species, final String key, final JsonElement jsonElement) {
        final PropertyApplierResult result = propertyAppliers.apply(species, key, jsonElement);

        if (!result.wasSuccessful())
            LOGGER.warn("Failure applying key '{}': {}", key, result.getErrorMessage());
    }

}
