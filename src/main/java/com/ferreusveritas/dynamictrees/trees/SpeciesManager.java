package com.ferreusveritas.dynamictrees.trees;

import com.ferreusveritas.dynamictrees.api.datapacks.JsonApplierRegistryEvent;
import com.ferreusveritas.dynamictrees.api.datapacks.JsonPropertyApplier;
import com.ferreusveritas.dynamictrees.api.datapacks.PropertyApplierResult;
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
import net.minecraft.block.Block;
import net.minecraft.resources.IResourceManager;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.BiomeDictionary;
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

//    private Map<Species, ResourceLocation> megaSpeciesCache = new HashMap<>();

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

        // TODO: Make common species set in family Json.
        this.loadAppliers.registerIfTrueApplier("common", species -> species.getFamily().setupCommonSpecies(species))
                .registerIfTrueApplier("generate_seed", Species::generateSeed)
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
                .register("mega_species", Species.class, Species::setMegaSpecies)
                .register("seed", Seed.class, Species::setSeed)
                .register("primitive_sapling", Block.class, Species::setPrimitiveSapling)
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
        preparedObject.forEach((registryName, jsonElement) -> {
            final ObjectFetchResult<JsonObject> jsonObjectFetchResult = JsonObjectGetters.JSON_OBJECT_GETTER.get(jsonElement);

            if (!jsonObjectFetchResult.wasSuccessful()) {
                LOGGER.warn("Skipping loading data for species '{}' due to error: {}", registryName, jsonObjectFetchResult.getErrorMessage());
                return;
            }

            final JsonObject jsonObject = jsonObjectFetchResult.getValue();
            final Species species;

            if (firstLoad) {
                if (Species.REGISTRY.containsKey(registryName)) {
                    LOGGER.warn("Skipping loading species '{}' due to it already being registered.", registryName);
                    return;
                }

                SpeciesType<Species> speciesType = JsonHelper.getFromObjectOrWarn(jsonObject, TYPE, TreeSpecies.CLASS,
                        "Error loading species type for species '" + registryName + "' (defaulting to tree species) :", false);
                final Family family = JsonHelper.getFromObjectOrWarn(jsonObject, FAMILY, Family.class,
                        "Skipping loading tree family for species '" + registryName + "' due to error:", true);

                // If the family was not set we skip loading the species.
                if (family == null)
                    return;

                // Default to tree species if it wasn't set or couldn't be found.
                if (speciesType == null)
                    speciesType = TreeSpecies.TREE_SPECIES;

                // Construct the species class from initial setup properties.
                species = speciesType.construct(registryName, family);

                // Apply load appliers for things like generating seeds and saplings.
                jsonObject.entrySet().forEach(entry -> readEntry(this.loadAppliers, species, entry.getKey(), entry.getValue()));
            } else {
                species = Species.REGISTRY.getValue(registryName);

                if (species == null || !species.isValid()) {
                    LOGGER.warn("Skipping loading data for species '{}' due to it not being registered.", registryName);
                    return;
                }

                species.clearGenFeatures();
                species.clearAcceptableSoils();

                // Apply reload appliers.
                jsonObject.entrySet().forEach(entry -> readEntry(this.reloadAppliers, species, entry.getKey(), entry.getValue()));
            }

            // Apply universal appliers for both load and reload.
            jsonObject.entrySet().forEach(entry -> readEntry(this.appliers, species, entry.getKey(), entry.getValue()));

            // If no acceptable soils were set, default to the Species' standard soils.
            if (!species.hasAcceptableSoil())
                species.setStandardSoils();

            if (firstLoad) {
                Species.REGISTRY.register(species);
                LOGGER.debug("Successfully registered species '{}' with data: {}", registryName, species.getDisplayInfo());
            } else {
                LOGGER.debug("Loaded data for species '{}': {}.", registryName, species.getDisplayInfo());
            }
        });

        // The below is code for once our own registries are made.

//        // Once all species are loaded, apply mega species.
//        this.megaSpeciesCache.forEach((species, megaSpeciesRegName) -> {
//            final Species megaSpecies = Species.REGISTRY.getValue(megaSpeciesRegName);
//
//            if (megaSpecies == null)
//                LOGGER.warn("Could not find species '{}' to apply as mega species for '{}'.", megaSpeciesRegName, species.getRegistryName());
//            else species.setMegaSpecies(megaSpecies);
//        });
//
//        this.megaSpeciesCache.clear();
    }

    private static void readEntry (final JsonPropertyApplierList<Species> propertyAppliers, final Species species, final String key, final JsonElement jsonElement) {
        final PropertyApplierResult result = propertyAppliers.apply(species, key, jsonElement);

        if (!result.wasSuccessful())
            LOGGER.warn("Failure applying key '{}': {}", key, result.getErrorMessage());
    }

}
