package com.dtteam.dynamictrees.treepack.loader;

import com.dtteam.dynamictrees.DynamicTrees;
import com.dtteam.dynamictrees.api.resource.loading.preparation.JsonRegistryResourceLoader;
import com.dtteam.dynamictrees.api.season.ClimateZoneType;
import com.dtteam.dynamictrees.block.fruit.Fruit;
import com.dtteam.dynamictrees.block.leaves.LeavesProperties;
import com.dtteam.dynamictrees.block.pod.Pod;
import com.dtteam.dynamictrees.block.soil.SoilHelper;
import com.dtteam.dynamictrees.block.soil.SoilProperties;
import com.dtteam.dynamictrees.deserialization.JsonDeserializers;
import com.dtteam.dynamictrees.deserialization.JsonMapWrapper;
import com.dtteam.dynamictrees.deserialization.JsonPropertyAppliers;
import com.dtteam.dynamictrees.deserialization.applier.Applier;
import com.dtteam.dynamictrees.deserialization.applier.PropertyApplierResult;
import com.dtteam.dynamictrees.item.Seed;
import com.dtteam.dynamictrees.systems.SeedSaplingRecipe;
import com.dtteam.dynamictrees.systems.genfeature.GenFeatureConfiguration;
import com.dtteam.dynamictrees.systems.growthlogic.GrowthLogicKitConfiguration;
import com.dtteam.dynamictrees.tree.species.Species;
import com.dtteam.dynamictrees.tree.species.UndergroundRootsSpecies;
import com.dtteam.dynamictrees.utility.ResourceLocationUtils;
import com.google.gson.JsonObject;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.ComposterBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Harley O'Connor
 */
public final class SpeciesResourceLoader extends JsonRegistryResourceLoader<Species> {

    private static final Logger LOGGER = LogManager.getLogger();

    /**
     * A {@link JsonPropertyAppliers} for applying environment factors to {@link Species} objects.
     * (based on Forge's BiomeType).
     */
    private final JsonPropertyAppliers<Species> seasonOffsetAppliers = new JsonPropertyAppliers<>(Species.class);

    private final Map<Species, Float> composterChanceCache = new HashMap<>();

    public SpeciesResourceLoader() {
        super(Species.REGISTRY, SPECIES);
    }

    @Override
    public void registerAppliers() {
        this.seasonOffsetAppliers
                .registerMapApplier("fruiting", Float.class, Species::setSeasonalFruitingOffset)
                .registerMapApplier("seed_drop", Float.class, Species::setSeasonalSeedDropOffset)
        .registerMapApplier("growth", Float.class, Species::setSeasonalGrowthOffset);;

        JsonDeserializers.register(Species.CommonOverride.class, input ->
                JsonDeserializers.BIOME_PREDICATE.deserialize(input)
                        .map(biomePredicate -> (world, pos) -> world instanceof LevelReader &&
                                biomePredicate.test(((LevelReader) world).getBiome(pos)))
        );

        this.loadAppliers
                .register("seed", ResourceLocation.class, this::setSeed)
                .register("generate_seed", Boolean.class, Species::setShouldGenerateSeed)
                .register("generate_sapling", Boolean.class, Species::setShouldGenerateSapling)
                .register("sapling_name", String.class, Species::setSaplingName)
                .register("seed_name", String.class, Species::setSeedName);

        // Needed in common so sound and shape is known to clients joining servers.
        // TODO: Consider marking certain reload appliers as being needed client-side and loaded when joining a server.
        this.commonAppliers
                .register("always_show_on_waila", Boolean.class, Species::setAlwaysShowOnWaila)
                .register("sapling_sound", SoundType.class, Species::setSaplingSound)
                .register("sapling_shape", VoxelShape.class, Species::setSaplingShape);

        this.gatherDataAppliers
                .register("sapling_shape", VoxelShape.class, Species::setSaplingShape)
                .register("only_if_loaded", String.class, Species::setOnlyIfLoaded)
                .registerArrayApplier("only_if_loaded", String.class, Species::setOnlyIfLoaded)
                .registerMapApplier("model_overrides", ResourceLocation.class, Species::setModelOverrides)
                .registerMapApplier("texture_overrides", ResourceLocation.class, Species::setTextureOverrides)
                .registerMapApplier("lang_overrides", String.class, Species::setLangOverrides);

        this.reloadAppliers
                .register("tapering", Float.class, Species::setTapering)
                .register("up_probability", Integer.class, Species::setUpProbability)
                .register("lowest_branch_height", Integer.class, Species::setLowestBranchHeight)
                .register("signal_energy", Float.class, Species::setSignalEnergy)
                .register("growth_rate", Float.class, Species::setGrowthRate)
                .register("soil_longevity", Integer.class, Species::setSoilLongevity)
                .register("max_branch_radius", Integer.class, Species::setMaxBranchRadius)
                .register("growth_logic_kit", GrowthLogicKitConfiguration.class, Species::setGrowthLogicKit)
                .register("leaves_properties", LeavesProperties.class, Species::setLeavesProperties)
                .register("world_gen_leaf_map_height", Integer.class, Species::setWorldGenLeafMapHeight)
                .register("mega_species", ResourceLocation.class, this::setMegaSpecies)
                .register("can_craft_mega_seed", Boolean.class, Species::setCanCraftMegaSeed)
                .register("seed", Seed.class, (species, seed) -> species.setSeed(() -> seed))
                .register("seed_composter_chance", Float.class, this.composterChanceCache::put)
                .register("tint_sapling", Boolean.class, Species::setTintSapling)
                .register("sapling_grows_naturally", Boolean.class, Species::setCanSaplingGrowNaturally)
                .register("primitive_sapling", SeedSaplingRecipe.class, Species::addPrimitiveSaplingRecipe)
                .registerArrayApplier("primitive_saplings", SeedSaplingRecipe.class, Species::addPrimitiveSaplingRecipe)
                .register("common_override", Species.CommonOverride.class, Species::setCommonOverride)
                .register("can_bone_meal_tree", Boolean.class, Species::setCanBoneMealTree)
                .registerArrayApplier("acceptable_growth_blocks", Block.class, Species::addAcceptableBlockForGrowth)
                .registerArrayApplier("acceptable_soils", String.class, (Applier<Species, String>) this::addAcceptableSoil)
                .registerArrayApplier("world_gen_acceptable_soils", String.class, (Applier<Species, String>) this::addAcceptableSoilForWorldGen)
                .register("force_soil", SoilProperties.class, Species::setForceSoil)
                .registerListApplier("fruits", Fruit.class, Species::addFruits)
                .registerListApplier("pods", Pod.class, Species::addPods)
                .registerArrayApplier("features", GenFeatureConfiguration.class, Species::addGenFeature)
                .register("does_rot", Boolean.class, Species::setDoesRot)
                .register("drop_seeds", Boolean.class, Species::setDropSeeds)
                .register("preferred_climate", ClimateZoneType.class, Species::setPreferredClimate)
                .register("seasonal_offsets", JsonObject.class, this::applySeasonalOffsets)
                .register("climate_tolerance_floor", Float.class, Species::setClimateToleranceFloor)
                .register("big_tree_sound_threshold", Float.class, Species::setBigTreeSoundThreshold)
                .register("plantable_on_fluid", Boolean.class, Species::setPlantableOnFluid)
                .register("allowed_water_height_for_world_gen", Integer.class, Species::setAllowedWaterHeightForWorldgen)

                .register("seasonal_seed_drop_offset", Float.class, (s,o)->
                        LOGGER.error("The \"seasonal_seed_drop_offset\" property has been removed. Use \"seasonal_offsets\" instead! Species {}.", s.getRegistryName())
                )
                .register("seasonal_growth_offset", Float.class, (s,o)->
                        LOGGER.error("The \"seasonal_growth_offset\" property has been removed. Use \"seasonal_offsets\" instead! Species {}.", s.getRegistryName())
                )
                .register("seasonal_fruiting_offset", Float.class, (s,o)->
                        LOGGER.error("The \"seasonal_fruiting_offset\" property has been removed. Use \"seasonal_offsets\" instead! Species {}.", s.getRegistryName())
                )
                .register("environment_factors", JsonObject.class, (s,o)->
                        LOGGER.error("The \"environment_factors\" property has been removed. Use \"preferred_climate\" instead! Species {}.", s.getRegistryName())
                );

        registerMangroveAppliers();

        super.registerAppliers();
    }

    private void registerMangroveAppliers(){
        this.reloadAppliers
                .register("min_world_gen_height_offset", UndergroundRootsSpecies.class, Integer.class, UndergroundRootsSpecies::setMinWorldGenHeightOffset)
                .register("max_world_gen_height_offset", UndergroundRootsSpecies.class, Integer.class, UndergroundRootsSpecies::setMaxWorldGenHeightOffset)
                .register("roots_growth_logic_kit", UndergroundRootsSpecies.class, GrowthLogicKitConfiguration.class, UndergroundRootsSpecies::setRootsGrowthLogicKit)
                .register("root_growth_multiplier", UndergroundRootsSpecies.class, Integer.class, UndergroundRootsSpecies::setRootGrowthMultiplier)
                .register("root_tapering", UndergroundRootsSpecies.class, Float.class, UndergroundRootsSpecies::setRootTapering)
                .register("root_signal_energy", UndergroundRootsSpecies.class, Float.class, UndergroundRootsSpecies::setRootSignalEnergy)
                .register("update_soil_on_water_radius", UndergroundRootsSpecies.class, Integer.class, UndergroundRootsSpecies::setUpdateSoilOnWaterRadius);
    }

    private void setSeed(Species species, ResourceLocation seedName) {
        final ResourceLocation processedSeedName = ResourceLocationUtils.parseDTLocation(seedName);
        species.setShouldGenerateSeed(false);
        species.setShouldGenerateSapling(false);
        DynamicTrees.runOnCommonSetup(() -> {
            final Item seed = BuiltInRegistries.ITEM.get(processedSeedName);
            if (seed instanceof Seed) {
                species.setSeed(() -> (Seed) seed);
            } else {
                LOGGER.warn("Could not find valid seed item from registry name \"{}\".", seedName);
            }
        });
    }

    private void applySeasonalOffsets(Species species, JsonObject jsonObject) {
        this.seasonOffsetAppliers.applyAll(new JsonMapWrapper(jsonObject), species)
                .forEachErrorWarning(
                        error -> LOGGER.error("Error applying seasonal offsets for " +
                                "species '{}': {}", species.getRegistryName(), error),
                        warning -> LOGGER.warn("Warning applying seasonal offsets for " +
                                "species '{}': {}", species.getRegistryName(), warning)
                );
    }

    private void setMegaSpecies(Species species, ResourceLocation registryName) {
        final ResourceLocation processedRegName = ResourceLocationUtils.parseDTLocation(registryName);
        Species.REGISTRY.runOnNextLock(Species.REGISTRY.generateIfValidRunnable(processedRegName, species::setMegaSpecies, () -> LOGGER.warn("Could not set mega species for '{}' as Species '{}' was not found.", species, processedRegName)));
    }

    private PropertyApplierResult addAcceptableSoil(Species species, String acceptableSoil) {
        return SoilHelper.applyIfSoilIsAcceptable(species, acceptableSoil, Species::addAcceptableSoils);
    }
    private PropertyApplierResult addAcceptableSoilForWorldGen(Species species, String acceptableSoil) {
        return SoilHelper.applyIfSoilIsAcceptable(species, acceptableSoil, Species::addAcceptableSoilsForWorldGen);
    }

    @Override
    protected void postLoadOnLoad(LoadData loadData, JsonObject json) {
        super.postLoadOnLoad(loadData, json);
        loadData.getResource().generateSeed().generateSapling();
        Species.REGISTRY.forEach(species -> {
            if (species.hasFruits()) Species.REGISTRY.runOnNextLock(species::inheritSeasonalFruitingParametersToFruits);
            if (species.hasPods()) Species.REGISTRY.runOnNextLock(species::inheritSeasonalFruitingParametersToPods);
        });
    }

    @Override
    protected void postLoadOnReload(LoadData loadData, JsonObject json) {
        final Species species = loadData.getResource();
        this.composterChanceCache.put(species, species.defaultSeedComposterChance());
        super.postLoadOnReload(loadData, json);
        this.registerComposterChances();
    }

    private void registerComposterChances() {
        this.composterChanceCache.forEach((species, chance) -> {
            if (species.getSeed().isPresent() && chance > 0) {
                ComposterBlock.COMPOSTABLES.put(species.getSeed().get(), chance.floatValue());
            }
        });
        this.composterChanceCache.clear();
    }

}
