package com.dtteam.dynamictrees.platform.services;

import com.dtteam.dynamictrees.DynamicTrees;
import com.dtteam.dynamictrees.block.branch.ThickBranchBlock;
import com.dtteam.dynamictrees.systems.season.SeasonCompatibilityHandler;
import com.dtteam.dynamictrees.tree.species.SwampSpecies;

import java.util.ArrayList;
import java.util.List;

public interface IConfigHelper {

    public static final String LEAVES_SEED_DROP_RATE = "leavesSeedDropRate";
    public static final String VOLUNTARY_SEED_DROP_RATE = "voluntarySeedDropRate";
    public static final String SEED_PLANT_RATE = "seedPlantRate";
    public static final String SEED_TIME_TO_LIVE = "seedTimeToLive";
    public static final String SEED_ONLY_FOREST = "seedOnlyForest";
    public static final String SEED_MIN_FORESTNESS = "seedMinForestness";

    public static final String TREE_GROWTH_MULTIPLIER = "treeGrowthMultiplier";
    public static final String TREE_HARVEST_MULTIPLIER = "treeHarvestMultiplier";
    public static final String MAX_TREE_HARDNESS = "maxTreeHardness";
    public static final String TREE_HARDNESS_MULTIPLIER = "treeHardnessMultiplier";
    public static final String DROP_STICKS = "dropSticks";
    public static final String SCALE_BIOME_GROWTH_RATE = "scaleBiomeGrowthRate";
    public static final String DESEASE_CHANCE = "diseaseChance";
    public static final String MAX_BRANCH_ROT_RADIUS = "maxBranchRotRadius";
    public static final String ROOTY_BLOCK_HARDNESS_MULTIPLIER = "rootyBlockHardnessMultiplier";
    public static final String SWAMP_OAKS_IN_WATER = "swampOaksInWater";
    public static final String BONE_MEAL_GROWTH_PULSES = "boneMealGrowthPulses";

    public static final String IS_LEAVES_PASSABLE = "isLeavesPassable";
    public static final String VANILLA_LEAVES_COLLISION = "vanillaLeavesCollision";
    public static final String ENABLE_BRANCH_CLIMBING = "enableBranchClimbing";
    public static final String ENABLE_CANOPY_CRASH = "enableCanopyCrash";
    public static final String AXE_DAMAGE_MODE = "axeDamageMode";
    public static final String ENABLE_FALLING_TREES = "enableFallingTrees";
    public static final String ENABLE_FALLING_TREE_DAMAGE = "enableFallingTreeDamage";
    public static final String FALLING_TREE_DAMAGE_MULTIPLIER = "fallingTreeDamageMultiplier";
    public static final String DIRT_BUCKET_PLACES_DIRT = "dirtBucketPlacesDirt";
    public static final String SLOPPY_BREAK_DROPS = "sloppyBreakDrops";
    public static final String MIN_RADIUS_FOR_STRIP = "minRadiusForStrip";
    public static final String ENABLE_STRIP_RADIUS_REDUCTION = "enableStripRadiusReduction";
    public static final String CAN_BONE_MEAL_FRUIT = "canBoneMealFruit";
    public static final String CAN_BONE_MEAL_PODS = "canBoneMealPods";
    public static final String DYNAMIC_SAPLING_DROPS = "dynamicSaplingDrops";

    public static final String REPLACE_VANILLA_SAPLINGS = "replaceVanillaSaplings";
    public static final String REPLACE_NYLIUM_FUNGI = "replaceNyliumFungi";
    public static final String CANCEL_VANILLA_VILLAGE_TREES = "cancelVanillaVillageTrees";
    public static final String MAX_FALLING_TREE_LEAVES_PARTICLES = "maxFallingTreeLeavesParticles";

    public static final String GENERATE_PODZOL = "generatePodzol";
    public static final String WORLD_GEN = "worldGen";
    public static final String DIMENSION_BLACK_LIST = "dimensionsBlacklist";

    public static final String GENERATE_DIRT_BUCKET_RECIPES = "generateDirtBucketRecipes";
    public static final String BIOCHAR_BREWING_BASE = "biocharBrewingBase";

    public static final String PREFERRED_SEASON_MOD = "preferredSeasonMod";
    public static final String ENABLE_SEASONAL_SEED_DROP_FACTOR = "enableSeasonalSeedDropFactor";
    public static final String ENABLE_SEASONAL_SEED_GROWTH_FACTOR = "enableSeasonalGrowthFactor";
    public static final String ENABLE_SEASONAL_SEED_FRUIT_PRODUCTION_FACTOR = "enableSeasonalFruitProductionFactor";

    public static final String DEBUG = "debug";

    <T> T getConfig (String config, Class<T> tClass);
    <T extends Enum<T>> T getEnumConfig (String config, Class<T> tClass);

    default Boolean getBoolConfig(String config){
        return getConfig(config, Boolean.class);
    }

    default Integer getIntConfig(String config){
        return getConfig(config, Integer.class);
    }
    default Double getDoubleConfig(String config){
        return getConfig(config, Double.class);
    }
    default String getStringConfig(String config){
        return getConfig(config, String.class);
    }
    @SuppressWarnings("unchecked")
    default List<String> getStringListConfig(String config){
        return getConfig(config, List.class);
    }

    boolean isServerConfigLoaded ();
    boolean isCommonConfigLoaded ();
    boolean isClientConfigLoaded ();

}
