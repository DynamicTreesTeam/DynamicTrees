package com.ferreusveritas.dynamictrees.trees;

import com.ferreusveritas.dynamictrees.api.TreeRegistry;
import com.ferreusveritas.dynamictrees.api.treepacks.JsonApplierRegistryEvent;
import com.ferreusveritas.dynamictrees.api.treepacks.JsonPropertyApplier;
import com.ferreusveritas.dynamictrees.api.treepacks.PropertyApplierResult;
import com.ferreusveritas.dynamictrees.blocks.leaves.LeavesProperties;
import com.ferreusveritas.dynamictrees.growthlogic.GrowthLogicKit;
import com.ferreusveritas.dynamictrees.items.Seed;
import com.ferreusveritas.dynamictrees.resources.JsonReloadListener;
import com.ferreusveritas.dynamictrees.systems.DirtHelper;
import com.ferreusveritas.dynamictrees.systems.genfeatures.config.ConfiguredGenFeature;
import com.ferreusveritas.dynamictrees.util.BiomeList;
import com.ferreusveritas.dynamictrees.util.BiomePredicate;
import com.ferreusveritas.dynamictrees.util.json.JsonHelper;
import com.ferreusveritas.dynamictrees.util.json.JsonObjectGetters;
import com.ferreusveritas.dynamictrees.util.json.JsonPropertyApplierList;
import com.ferreusveritas.dynamictrees.util.json.ObjectFetchResult;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;
import net.minecraft.block.Block;
import net.minecraft.resources.IResourceManager;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.BiomeDictionary;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.ModLoader;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Map;

/**
 * @author Harley O'Connor
 */
public final class SpeciesManager extends JsonReloadListener<Species> {

    private static final Logger LOGGER = LogManager.getLogger();

    /** A {@link JsonPropertyApplierList} for applying environment factors to {@link Species} objects. (based on biome type). */
    private JsonPropertyApplierList<Species> environmentFactorAppliers;

    public static final String TYPE = "type";
    public static final String FAMILY = "family";

    public SpeciesManager() {
        super("species", Species.class, JsonApplierRegistryEvent.SPECIES);
    }

    @Override
    public void registerAppliers(final String applierListIdentifier) {
        this.environmentFactorAppliers = new JsonPropertyApplierList<>(Species.class);

        BiomeDictionary.Type.getAll().stream().map(type -> new JsonPropertyApplier<>(type.toString().toLowerCase(), Species.class, Float.class, (species, factor) -> species.envFactor(type, factor)))
                .forEach(this.environmentFactorAppliers::register);

        JsonObjectGetters.register(Species.ICommonOverride.class, jsonElement -> {
            final ObjectFetchResult<BiomePredicate> biomePredicateFetchResult = JsonObjectGetters.BIOME_PREDICATE_GETTER.get(jsonElement);

            if (!biomePredicateFetchResult.wasSuccessful())
                return ObjectFetchResult.failureFromOther(biomePredicateFetchResult);

            return ObjectFetchResult.success((world, pos) -> biomePredicateFetchResult.getValue().test(world.getBiome(pos)));
        });

        this.loadAppliers.registerIfTrueApplier("generate_seed", Species::generateSeed)
                .registerIfTrueApplier("generate_sapling", Species::generateSapling);

        this.reloadAppliers.register("tapering", Float.class, Species::setTapering)
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
                .register("mega_species", ResourceLocation.class, (species, registryName) -> {
                    Species.REGISTRY.runOnNextLock(Species.REGISTRY.generateIfValidRunnable(registryName, species::setMegaSpecies, () -> LOGGER.warn("Could not set mega species for '" + species + "' as Species '" + registryName + "' was not found.")));
                })
                .register("seed", Seed.class, Species::setSeed)
                .register("primitive_sapling", Block.class, (species, block) -> TreeRegistry.registerSaplingReplacer(block.getDefaultState(), species))
                .register("common_override", Species.ICommonOverride.class, Species::setCommonOverride)
                .register("perfect_biomes", BiomeList.class, (species, biomeList) -> species.getPerfectBiomes().addAll(biomeList))
                .registerArrayApplier("acceptable_growth_blocks", Block.class, Species::addAcceptableBlockForGrowth)
                .registerArrayApplier("features", ConfiguredGenFeature.NULL_CONFIGURED_FEATURE.getClass(), Species::addGenFeature)
                .registerArrayApplier("acceptable_soils", String.class, (species, acceptableSoil) -> {
                    if (DirtHelper.getSoilFlags(acceptableSoil) == 0)
                        return new PropertyApplierResult("Could not find acceptable soil '" + acceptableSoil + "'.");

                    species.addAcceptableSoils(acceptableSoil);
                    return PropertyApplierResult.SUCCESS;
                });

        super.registerAppliers(applierListIdentifier);
    }

    @Override
    protected void apply(final Map<ResourceLocation, JsonElement> preparedObject, final IResourceManager resourceManager, final boolean firstLoad) {
        Species.REGISTRY.unlock(); // Ensure registry is unlocked.

        preparedObject.forEach((registryName, jsonElement) -> {
            final ObjectFetchResult<JsonObject> jsonObjectFetchResult = JsonObjectGetters.JSON_OBJECT_GETTER.get(jsonElement);

            if (!jsonObjectFetchResult.wasSuccessful()) {
                LOGGER.warn("Skipping loading data for species '{}' due to error: {}", registryName, jsonObjectFetchResult.getErrorMessage());
                return;
            }

            final JsonObject jsonObject = jsonObjectFetchResult.getValue();

            // Skip the current entry if it shouldn't load.
            if (!this.shouldLoad(jsonObject, "Error loading data for species '" + registryName + "': "))
                return;

            final Species species;
            final boolean newRegistry = !Species.REGISTRY.has(registryName);

            if (newRegistry) {
                Species.Type speciesType = JsonHelper.getFromObjectOrWarn(jsonObject, TYPE, Species.Type.class,
                        "Error loading species type for species '" + registryName + "' (defaulting to tree species) :", false);
                final Family family = JsonHelper.getFromObjectOrWarn(jsonObject, FAMILY, Family.class,
                        "Skipping loading tree family for species '" + registryName + "' due to error:", true);

                // If the family was not set we skip loading the species.
                if (family == null)
                    return;

                // Default to tree species if it wasn't set or couldn't be found.
                if (speciesType == null)
                    speciesType = Species.REGISTRY.getDefaultType();

                // Construct the species class from initial setup properties.
                species = speciesType.construct(registryName, family);

                if (firstLoad) {
                    // Apply load appliers for things like generating seeds and saplings.
                    jsonObject.entrySet().forEach(entry -> readEntry(this.loadAppliers, species, entry.getKey(), entry.getValue()));
                } else species.setPreReloadDefaults();
            } else {
                // Species is already registered, so reset it and apply pre-reload defaults.
                species = Species.REGISTRY.get(registryName).reset().setPreReloadDefaults();
            }

            if (!firstLoad) {
                // Apply reload appliers.
                jsonObject.entrySet().forEach(entry -> readEntry(this.reloadAppliers, species, entry.getKey(), entry.getValue()));
            }

            // Apply universal appliers for both load and reload.
            jsonObject.entrySet().forEach(entry -> readEntry(this.appliers, species, entry.getKey(), entry.getValue()));

            if (!firstLoad)
                species.setPostReloadDefaults();

            if (newRegistry) {
                Species.REGISTRY.register(species);
                LOGGER.debug("Successfully registered species '{}' with data: {}", registryName, species.getDisplayInfo());
            } else {
                LOGGER.debug("Loaded data for species '{}': {}.", registryName, species.getDisplayInfo());
            }
        });

        if (firstLoad)
            return;

        // Lock registry (don't lock on first load as registry events are fired after).
        Species.REGISTRY.lock();
    }

    private static void readEntry (final JsonPropertyApplierList<Species> propertyAppliers, final Species species, final String key, final JsonElement jsonElement) {
        final PropertyApplierResult result = propertyAppliers.apply(species, key, jsonElement);

        if (!result.wasSuccessful())
            LOGGER.warn("Failure applying key '{}': {}", key, result.getErrorMessage());
    }

}
