package com.ferreusveritas.dynamictrees.trees;

import com.ferreusveritas.dynamictrees.api.datapacks.*;
import com.ferreusveritas.dynamictrees.blocks.leaves.LeavesProperties;
import com.ferreusveritas.dynamictrees.growthlogic.GrowthLogicKit;
import com.ferreusveritas.dynamictrees.systems.genfeatures.GenFeature;
import com.ferreusveritas.dynamictrees.systems.genfeatures.config.ConfiguredGenFeature;
import com.ferreusveritas.dynamictrees.util.json.JsonHelper;
import com.ferreusveritas.dynamictrees.util.json.JsonObjectGetters;
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

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author Harley O'Connor
 */
public final class SpeciesManager extends JsonReloadListener {

    private static final Gson GSON = (new GsonBuilder()).setPrettyPrinting().disableHtmlEscaping().create();
    private static final Logger LOGGER = LogManager.getLogger();

    /** A list of {@link JsonPropertyApplier} objects for applying environment factors to species (based on biome type). */
    public static final List<JsonPropertyApplier<Species, ?>> ENVIRONMENT_FACTOR_APPLIERS = BiomeDictionary.Type.getAll().stream().map(type ->
            new JsonPropertyApplier<>(type.toString().toLowerCase(), Species.class, Float.class, (species, factor) -> species.envFactor(type, factor))).collect(Collectors.toList());

    /** A list of {@link JsonPropertyApplier} objects for applying properties to species. */
    public static final List<JsonPropertyApplier<Species, ?>> PROPERTY_APPLIERS = new ArrayList<>();

    static {
        register("family", TreeFamily.class, Species::setFamily);
        register("tapering", Float.class, Species::setTapering);
        register("up_probability", Integer.class, Species::setUpProbability);
        register("lowest_branch_height", Integer.class, Species::setLowestBranchHeight);
        register("signal_energy", Float.class, Species::setSignalEnergy);
        register("growth_rate", Float.class, Species::setGrowthRate);
        register("soil_longevity", Integer.class, Species::setSoilLongevity);
        register("growth_logic_kit", GrowthLogicKit.class, Species::setGrowthLogicKit);
        register("leaves_properties", LeavesProperties.class, Species::setLeavesProperties);
        register("environment_factors", JsonObject.class, (species, jsonObject) ->
                jsonObject.entrySet().forEach(entry -> readEntry(ENVIRONMENT_FACTOR_APPLIERS, species, entry.getKey(), entry.getValue())));
        register("seed_drop_rarity", Float.class, Species::setupStandardSeedDropping);
        register("stick_drop_rarity", Float.class, Species::setupStandardStickDropping);
        register("primitive_sapling", Block.class, Species::setPrimitiveSapling);
        registerArrayApplier("features", ConfiguredGenFeature.NULL_CONFIGURED_FEATURE.getClass(), Species::addGenFeature);
    }

    public static <V> void register(final String key, final Class<V> valueClass, final IVoidPropertyApplier<Species, V> applier) {
        PROPERTY_APPLIERS.add(new JsonPropertyApplier<>(key, Species.class, valueClass, applier));
    }

    public static <V> void register(final String key, final Class<V> valueClass, final IPropertyApplier<Species, V> applier) {
        PROPERTY_APPLIERS.add(new JsonPropertyApplier<>(key, Species.class, valueClass, applier));
    }

    public static <V> void registerArrayApplier(final String key, final Class<V> valueClass, final IVoidPropertyApplier<Species, V> applier) {
        PROPERTY_APPLIERS.add(new JsonArrayPropertyApplier<>(key, Species.class, valueClass, new JsonPropertyApplier<>("", Species.class, valueClass , applier)));
    }

    public SpeciesManager() {
        super(GSON, "trees/species");
    }

    @Override
    protected void apply(Map<ResourceLocation, JsonElement> jsonFiles, IResourceManager resourceManager, IProfiler profiler) {
        jsonFiles.forEach((resourceLocation, jsonElement) -> {
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

            final JsonObject jsonObject = jsonObjectFetchResult.getValue();
            jsonObject.entrySet().forEach(entry -> readEntry(PROPERTY_APPLIERS, species, entry.getKey(), entry.getValue()));

            // Species will default to require a tile entity if it's not the common species.
            species.setRequiresTileEntity(!species.getFamily().getCommonSpecies().equals(species));

            LOGGER.debug("Injected species data: " + species.getDisplayInfo());
        });
    }

    private static void readEntry (final List<JsonPropertyApplier<Species, ?>> propertyAppliers, final Species species, final String identifier, final JsonElement jsonElement) {
        // If the element is a comment, ignore it and move onto next entry.
        if (JsonHelper.isComment(jsonElement))
            return;

        for (final JsonPropertyApplier<Species, ?> applier : propertyAppliers) {
            final PropertyApplierResult result = applier.applyIfShould(identifier, species, jsonElement);

            // If the result is null, it's not the right applier, so move onto the next one.
            if (result == null)
                continue;

            // If the application wasn't successful, print the error.
            if (!result.wasSuccessful())
                LOGGER.warn("Error whilst loading data for species '{}': {}", species.getRegistryName(), result.getErrorMessage());

            break; // We have read (or tried to read) this entry, so move onto the next.
        }
    }

}
