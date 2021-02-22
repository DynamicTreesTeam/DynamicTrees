package com.ferreusveritas.dynamictrees.trees;

import com.ferreusveritas.dynamictrees.api.datapacks.IPropertyApplier;
import com.ferreusveritas.dynamictrees.api.datapacks.IVoidPropertyApplier;
import com.ferreusveritas.dynamictrees.api.datapacks.JsonPropertyApplier;
import com.ferreusveritas.dynamictrees.api.datapacks.PropertyApplierResult;
import com.ferreusveritas.dynamictrees.api.treedata.ILeavesProperties;
import com.ferreusveritas.dynamictrees.growthlogic.GrowthLogicKit;
import com.ferreusveritas.dynamictrees.util.json.JsonHelper;
import com.ferreusveritas.dynamictrees.util.json.JsonObjectGetters;
import com.ferreusveritas.dynamictrees.util.json.ObjectFetchResult;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.client.resources.JsonReloadListener;
import net.minecraft.profiler.IProfiler;
import net.minecraft.resources.IResourceManager;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.BiomeDictionary;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author Harley O'Connor
 */
public final class SpeciesManager extends JsonReloadListener {

    private static final Gson GSON = (new GsonBuilder()).setPrettyPrinting().disableHtmlEscaping().create();
    private static final Logger LOGGER = LogManager.getLogger();

    private static final class PropertyApplier<V> extends JsonPropertyApplier<Species, V> {
        public PropertyApplier(final String identifier, final Class<V> valueClass, final IVoidPropertyApplier<Species, V> propertyApplier) {
            super(identifier, Species.class, valueClass, propertyApplier);
        }

        public PropertyApplier(final String identifier, final Class<V> valueClass, final IPropertyApplier<Species, V> propertyApplier) {
            super(identifier, Species.class, valueClass, propertyApplier);
        }
    }

    /** A list of {@link JsonPropertyApplier} objects for applying environment factors to species (based on biome type). */
    public static final List<PropertyApplier<?>> ENVIRONMENT_FACTOR_APPLIERS = BiomeDictionary.Type.getAll().stream().map(type ->
            new PropertyApplier<>(type.toString().toLowerCase(), Float.class, (species, factor) -> species.envFactor(type, factor))).collect(Collectors.toList());

    public static final PropertyApplier<TreeFamily> FAMILY = new PropertyApplier<>("family", TreeFamily.class, Species::setFamily);
    public static final PropertyApplier<Float> TAPERING = new PropertyApplier<>("tapering", Float.class, Species::setTapering);
    public static final PropertyApplier<Integer> UP_PROBABILITY = new PropertyApplier<>("up_probability", Integer.class, Species::setUpProbability);
    public static final PropertyApplier<Integer> LOWEST_BRANCH_HEIGHT = new PropertyApplier<>("lowest_branch_height", Integer.class, Species::setLowestBranchHeight);
    public static final PropertyApplier<Float> SIGNAL_ENERGY = new PropertyApplier<>("signal_energy", Float.class, Species::setSignalEnergy);
    public static final PropertyApplier<Float> GROWTH_RATE = new PropertyApplier<>("growth_rate", Float.class, Species::setGrowthRate);
    public static final PropertyApplier<GrowthLogicKit> GROWTH_LOGIC_KIT = new PropertyApplier<>("growth_logic_kit", GrowthLogicKit.class, Species::setGrowthLogicKit);
    public static final PropertyApplier<ILeavesProperties> LEAVES_PROPERTIES = new PropertyApplier<>("leaves_properties", ILeavesProperties.class, Species::setLeavesProperties);

    public static final PropertyApplier<JsonObject> ENVIRONMENT_FACTORS = new PropertyApplier<>("environment_factors", JsonObject.class, (species, jsonObject) ->
            jsonObject.entrySet().forEach(entry -> readEntry(ENVIRONMENT_FACTOR_APPLIERS, species, entry.getKey(), entry.getValue())));

    public static final PropertyApplier<Float> SEED_DROP_RARITY = new PropertyApplier<>("seed_drop_rarity", Float.class, Species::setupStandardSeedDropping);
    public static final PropertyApplier<Float> STICK_DROP_RARITY = new PropertyApplier<>("stick_drop_rarity", Float.class, Species::setupStandardStickDropping);

    public static final List<PropertyApplier<?>> PROPERTY_APPLIERS = Arrays.asList(FAMILY, TAPERING, UP_PROBABILITY, LOWEST_BRANCH_HEIGHT,
            SIGNAL_ENERGY, GROWTH_RATE, GROWTH_LOGIC_KIT, LEAVES_PROPERTIES, ENVIRONMENT_FACTORS, SEED_DROP_RARITY, STICK_DROP_RARITY);

    public SpeciesManager() {
        super(GSON, "trees/species");
    }

    @Override
    protected void apply(Map<ResourceLocation, JsonElement> jsonFiles, IResourceManager resourceManager, IProfiler profiler) {
        jsonFiles.forEach((resourceLocation, jsonElement) -> {
            final ObjectFetchResult<JsonObject> jsonObjectFetchResult = JsonObjectGetters.JSON_OBJECT_GETTER.get(jsonElement);

            if (!jsonObjectFetchResult.wasSuccessful()) {
                LOGGER.warn("Skipping loading species {} due to error: {}", resourceLocation, jsonObjectFetchResult.getErrorMessage());
                return;
            }

            final Species species = new Species();
            final JsonObject jsonObject = jsonObjectFetchResult.getValue();

            jsonObject.entrySet().forEach(entry -> readEntry(PROPERTY_APPLIERS, species, entry.getKey(), entry.getValue()));

            LOGGER.debug("Registered species: " + species.getDisplayInfo() + ".");
        });
    }

    private static void readEntry (final List<PropertyApplier<?>> propertyAppliers, final Species species, final String identifier, final JsonElement jsonElement) {
        // If the element is a comment, ignore it and move onto next entry.
        if (JsonHelper.isComment(jsonElement))
            return;

        for (final JsonPropertyApplier<?, ?> applier : propertyAppliers) {
            final PropertyApplierResult result = applier.applyIfShould(identifier, species, jsonElement);

            // If the result is null, it's not the right applier, so move onto the next one.
            if (result == null)
                continue;

            // If the application wasn't successful, print the error.
            if (!result.wasSuccessful())
                LOGGER.warn(result.getErrorMessage());

            break; // We have read (or tried to read) this entry, so move onto the next.
        }
    }

}
