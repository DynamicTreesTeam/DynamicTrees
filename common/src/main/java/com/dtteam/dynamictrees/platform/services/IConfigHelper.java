package com.dtteam.dynamictrees.platform.services;

import java.util.List;

public interface IConfigHelper {

    String LEAVES_SEED_DROP_RATE = "leavesSeedDropRate";
    String VOLUNTARY_SEED_DROP_RATE = "voluntarySeedDropRate";
    String SEED_PLANT_RATE = "seedPlantRate";
    String SEED_TIME_TO_LIVE = "seedTimeToLive";
    String SEED_ONLY_FOREST = "seedOnlyForest";
    String SEED_MIN_FORESTNESS = "seedMinForestness";

    String TREE_GROWTH_MULTIPLIER = "treeGrowthMultiplier";
    String TREE_HARVEST_MULTIPLIER = "treeHarvestMultiplier";
    String MAX_TREE_HARDNESS = "maxTreeHardness";
    String TREE_HARDNESS_MULTIPLIER = "treeHardnessMultiplier";
    String DROP_STICKS = "dropSticks";
    String SCALE_BIOME_GROWTH_RATE = "scaleBiomeGrowthRate";
    String DESEASE_CHANCE = "diseaseChance";
    String MAX_BRANCH_ROT_RADIUS = "maxBranchRotRadius";
    String ROOTY_BLOCK_HARDNESS_MULTIPLIER = "rootyBlockHardnessMultiplier";
    String SWAMP_OAKS_IN_WATER = "swampOaksInWater";
    String BONE_MEAL_GROWTH_PULSES = "boneMealGrowthPulses";

    String IS_LEAVES_PASSABLE = "isLeavesPassable";
    String VANILLA_LEAVES_COLLISION = "vanillaLeavesCollision";
    String ENABLE_BRANCH_CLIMBING = "enableBranchClimbing";
    String ENABLE_CANOPY_CRASH = "enableCanopyCrash";
    String AXE_DAMAGE_MODE = "axeDamageMode";
    String ENABLE_FALLING_TREES = "enableFallingTrees";
    String ENABLE_FALLING_TREE_DAMAGE = "enableFallingTreeDamage";
    String FALLING_TREE_DAMAGE_MULTIPLIER = "fallingTreeDamageMultiplier";
    String DIRT_BUCKET_PLACES_DIRT = "dirtBucketPlacesDirt";
    String SLOPPY_BREAK_DROPS = "sloppyBreakDrops";
    String MIN_RADIUS_FOR_STRIP = "minRadiusForStrip";
    String ENABLE_STRIP_RADIUS_REDUCTION = "enableStripRadiusReduction";
    String CAN_BONE_MEAL_FRUIT = "canBoneMealFruit";
    String CAN_BONE_MEAL_PODS = "canBoneMealPods";
    String DYNAMIC_SAPLING_DROPS = "dynamicSaplingDrops";

    String REPLACE_VANILLA_SAPLINGS = "replaceVanillaSaplings";
    String REPLACE_NYLIUM_FUNGI = "replaceNyliumFungi";
    String CANCEL_VANILLA_VILLAGE_TREES = "cancelVanillaVillageTrees";
    String MAX_FALLING_TREE_LEAVES_PARTICLES = "maxFallingTreeLeavesParticles";

    String GENERATE_PODZOL = "generatePodzol";
    String WORLD_GEN = "worldGen";
    String DIMENSION_BLACK_LIST = "dimensionsBlacklist";

    String GENERATE_DIRT_BUCKET_RECIPES = "generateDirtBucketRecipes";
    String GENERATE_MEGA_SEED_RECIPE = "generateMegaSeedRecipe";
    String BIOCHAR_BREWING_BASE = "biocharBrewingBase";

    String PREFERRED_SEASON_MOD = "preferredSeasonMod";
    String ENABLE_SEASONAL_SEED_DROP_FACTOR = "enableSeasonalSeedDropFactor";
    String ENABLE_SEASONAL_SEED_GROWTH_FACTOR = "enableSeasonalGrowthFactor";
    String ENABLE_SEASONAL_SEED_FRUIT_PRODUCTION_FACTOR = "enableSeasonalFruitProductionFactor";

    String DEBUG = "debug";

    Boolean getBoolConfig(String config);
    Integer getIntConfig(String config);
    Double getDoubleConfig(String config);
    String getStringConfig(String config);
    <T extends Enum<T>> T getEnumConfig(String config, Class<T> tClass);
    List<String> getStringListConfig(String config);

    boolean isServerConfigLoaded ();
    boolean isCommonConfigLoaded ();
    boolean isClientConfigLoaded ();

}
