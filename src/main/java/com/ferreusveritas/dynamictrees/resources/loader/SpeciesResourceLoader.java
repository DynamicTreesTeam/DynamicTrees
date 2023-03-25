package com.ferreusveritas.dynamictrees.resources.loader;

import com.ferreusveritas.dynamictrees.api.TreeRegistry;
import com.ferreusveritas.dynamictrees.api.applier.Applier;
import com.ferreusveritas.dynamictrees.api.applier.ApplierRegistryEvent;
import com.ferreusveritas.dynamictrees.api.applier.JsonPropertyApplier;
import com.ferreusveritas.dynamictrees.api.applier.PropertyApplierResult;
import com.ferreusveritas.dynamictrees.api.resource.loading.preparation.JsonRegistryResourceLoader;
import com.ferreusveritas.dynamictrees.block.leaves.LeavesProperties;
import com.ferreusveritas.dynamictrees.block.rooty.SoilHelper;
import com.ferreusveritas.dynamictrees.deserialisation.JsonDeserialisers;
import com.ferreusveritas.dynamictrees.deserialisation.JsonPropertyAppliers;
import com.ferreusveritas.dynamictrees.growthlogic.GrowthLogicKitConfiguration;
import com.ferreusveritas.dynamictrees.item.Seed;
import com.ferreusveritas.dynamictrees.systems.SeedSaplingRecipe;
import com.ferreusveritas.dynamictrees.systems.fruit.Fruit;
import com.ferreusveritas.dynamictrees.systems.genfeature.GenFeatureConfiguration;
import com.ferreusveritas.dynamictrees.systems.pod.Pod;
import com.ferreusveritas.dynamictrees.tree.species.Species;
import com.ferreusveritas.dynamictrees.util.BiomeList;
import com.ferreusveritas.dynamictrees.util.CommonSetup;
import com.ferreusveritas.dynamictrees.util.JsonMapWrapper;
import com.google.gson.JsonObject;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.ComposterBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.common.BiomeDictionary;
import net.minecraftforge.registries.ForgeRegistries;
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
     * A {@link JsonPropertyAppliers} for applying environment factors to {@link Species} objects. (based on {@link
     * net.minecraftforge.common.BiomeManager.BiomeType}).
     */
    private final JsonPropertyAppliers<Species> environmentFactorAppliers = new JsonPropertyAppliers<>(Species.class);

    private final Map<Species, Float> composterChanceCache = new HashMap<>();

    public SpeciesResourceLoader() {
        super(Species.REGISTRY, ApplierRegistryEvent.SPECIES);
    }

    @Override
    public void registerAppliers() {
        BiomeDictionary.Type.getAll().stream().map(type -> new JsonPropertyApplier<>(type.toString().toLowerCase(), Species.class, Float.class, (species, factor) -> species.envFactor(type, factor)))
                .forEach(this.environmentFactorAppliers::register);

        JsonDeserialisers.register(Species.CommonOverride.class, input ->
                JsonDeserialisers.BIOME_PREDICATE.deserialise(input)
                        .map(biomePredicate -> (world, pos) -> world instanceof LevelReader &&
                                biomePredicate.test(((LevelReader) world).getBiome(pos).value()))
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

        // We need the sapling shape to know which parent smartmodel the sapling model should use.
        this.gatherDataAppliers
                .register("sapling_shape", VoxelShape.class, Species::setSaplingShape);

        this.reloadAppliers
                .register("tapering", Float.class, Species::setTapering)
                .register("up_probability", Integer.class, Species::setUpProbability)
                .register("lowest_branch_height", Integer.class, Species::setLowestBranchHeight)
                .register("signal_energy", Float.class, Species::setSignalEnergy)
                .register("growth_rate", Float.class, Species::setGrowthRate)
                .register("soil_longevity", Integer.class, Species::setSoilLongevity)
                .register("max_branch_radius", Integer.class, Species::setMaxBranchRadius)
                .register("transformable", Boolean.class, Species::setTransformable)
                .register("growth_logic_kit", GrowthLogicKitConfiguration.class, Species::setGrowthLogicKit)
                .register("leaves_properties", LeavesProperties.class, Species::setLeavesProperties)
                .register("world_gen_leaf_map_height", Integer.class, Species::setWorldGenLeafMapHeight)
                .register("environment_factors", JsonObject.class, this::applyEnvironmentFactors)
                .register("mega_species", ResourceLocation.class, this::setMegaSpecies)
                .register("seed", Seed.class, (species, seed) -> species.setSeed(() -> seed))
                .register("seed_composter_chance", Float.class, this.composterChanceCache::put)
                .register("sapling_grows_naturally", Boolean.class, Species::setCanSaplingGrowNaturally)
                .register("primitive_sapling", SeedSaplingRecipe.class, Species::addPrimitiveSaplingRecipe)
                .registerArrayApplier("primitive_saplings", SeedSaplingRecipe.class, Species::addPrimitiveSaplingRecipe)
                .register("common_override", Species.CommonOverride.class, Species::setCommonOverride)
                .register("perfect_biomes", BiomeList.class, (species, biomeList) -> species.getPerfectBiomes().addAll(biomeList))
                .register("can_bone_meal_tree", Boolean.class, Species::setCanBoneMealTree)
                .registerArrayApplier("acceptable_growth_blocks", Block.class, Species::addAcceptableBlockForGrowth)
                .registerArrayApplier("acceptable_soils", String.class, (Applier<Species, String>) this::addAcceptableSoil)
                .registerListApplier("fruits", Fruit.class, Species::addFruits)
                .registerListApplier("pods", Pod.class, Species::addPods)
                .registerArrayApplier("features", GenFeatureConfiguration.class, Species::addGenFeature)
                .register("does_rot", Boolean.class, Species::setDoesRot)
                .register("drop_seeds", Boolean.class, Species::setDropSeeds)
                .register("seasonal_seed_drop_offset", Float.class, Species::setSeasonalSeedDropOffset)
                .register("seasonal_growth_offset", Float.class, Species::setSeasonalGrowthOffset)
                .register("seasonal_fruiting_offset", Float.class, Species::setSeasonalFruitingOffset)
                .register("inherit_fruiting_offset_to_fruits", Boolean.class, (species, doInherit)->{
                    if (doInherit) Species.REGISTRY.runOnNextLock(species::inheritSeasonalFruitingOffsetToFruits); })
                .register("inherit_fruiting_offset_to_pods", Boolean.class, (species, doInherit)->{
                    if (doInherit) Species.REGISTRY.runOnNextLock(species::inheritSeasonalFruitingOffsetToPods); });;

        super.registerAppliers();
    }

    private void setSeed(Species species, ResourceLocation seedName) {
        final ResourceLocation processedSeedName = TreeRegistry.processResLoc(seedName);
        species.setShouldGenerateSeed(false);
        species.setShouldGenerateSapling(false);
        CommonSetup.runOnCommonSetup(event -> {
            final Item seed = ForgeRegistries.ITEMS.getValue(processedSeedName);
            if (seed instanceof Seed) {
                species.setSeed(() -> (Seed) seed);
            } else {
                LOGGER.warn("Could not find valid seed item from registry name \"" + seedName + "\".");
            }
        });
    }

    private void applyEnvironmentFactors(Species species, JsonObject jsonObject) {
        this.environmentFactorAppliers.applyAll(new JsonMapWrapper(jsonObject), species)
                .forEachErrorWarning(
                        error -> LOGGER.error("Error applying environment factor for " +
                                "species '{}': {}", species.getRegistryName(), error),
                        warning -> LOGGER.warn("Warning applying environment factor for " +
                                "species '{}': {}", species.getRegistryName(), warning)
                );
    }

    private void setMegaSpecies(Species species, ResourceLocation registryName) {
        final ResourceLocation processedRegName = TreeRegistry.processResLoc(registryName);
        Species.REGISTRY.runOnNextLock(Species.REGISTRY.generateIfValidRunnable(processedRegName, species::setMegaSpecies, () -> LOGGER.warn("Could not set mega species for '" +
                species + "' as Species '" + processedRegName + "' was not found.")));
    }

    private PropertyApplierResult addAcceptableSoil(Species species, String acceptableSoil) {
        if (SoilHelper.getSoilFlags(acceptableSoil) == 0) {
            return PropertyApplierResult.failure("Could not find acceptable soil '" + acceptableSoil + "'.");
        }

        species.addAcceptableSoils(acceptableSoil);
        return PropertyApplierResult.success();
    }

    @Override
    protected void postLoadOnLoad(LoadData loadData, JsonObject json) {
        super.postLoadOnLoad(loadData, json);
        loadData.getResource().generateSeed().generateSapling();
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
                ComposterBlock.add(chance, species.getSeed().get());
            }
        });
        this.composterChanceCache.clear();
    }

}
