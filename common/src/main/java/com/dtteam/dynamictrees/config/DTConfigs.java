package com.dtteam.dynamictrees.config;

import com.dtteam.dynamictrees.DynamicTrees;
import com.dtteam.dynamictrees.block.branch.ThickBranchBlock;
import com.dtteam.dynamictrees.systems.season.SeasonCompatibilityHandler;
import com.dtteam.dynamictrees.tree.species.SwampSpecies;
import net.neoforged.neoforge.common.ModConfigSpec;
import org.apache.commons.lang3.tuple.Pair;

import java.util.ArrayList;
import java.util.List;

public class DTConfigs {

    public static final ModConfigSpec SERVER_CONFIG;
    public static final DTConfigs SERVER;
    public static final ModConfigSpec COMMON_CONFIG;
    public static final DTConfigs COMMON;
    public static final ModConfigSpec CLIENT_CONFIG;
    public static final DTConfigs CLIENT;

    static {
        Pair<DTConfigs, ModConfigSpec> serverPair = new ModConfigSpec.Builder().configure(DTConfigs::buildServerConfig);
        SERVER_CONFIG = serverPair.getRight();
        SERVER = serverPair.getLeft();

        Pair<DTConfigs, ModConfigSpec> commonPair = new ModConfigSpec.Builder().configure(DTConfigs::buildCommonConfig);
        COMMON_CONFIG = commonPair.getRight();
        COMMON = commonPair.getLeft();

        Pair<DTConfigs, ModConfigSpec> clientPair = new ModConfigSpec.Builder().configure(DTConfigs::buildClientConfig);
        CLIENT_CONFIG = clientPair.getRight();
        CLIENT = clientPair.getLeft();
    }

    public ModConfigSpec.DoubleValue leavesSeedDropRate;
    public ModConfigSpec.DoubleValue minSeasonalLeavesSeedDropRate;
    public ModConfigSpec.DoubleValue voluntarySeedDropRate;
    public ModConfigSpec.DoubleValue minSeasonalVoluntarySeedDropRate;
    public ModConfigSpec.DoubleValue seedPlantRate;
    public ModConfigSpec.IntValue seedTimeToLive;
    public ModConfigSpec.BooleanValue seedOnlyForest;
    public ModConfigSpec.DoubleValue seedMinForestness;
    public ModConfigSpec.BooleanValue climateAffectsFruitsAndPods;

    public ModConfigSpec.DoubleValue treeGrowthMultiplier;
    public ModConfigSpec.DoubleValue treeHarvestMultiplier;
    public ModConfigSpec.DoubleValue maxTreeHardness;
    public ModConfigSpec.DoubleValue treeHardnessMultiplier;
    public ModConfigSpec.BooleanValue dropSticks;
    public ModConfigSpec.DoubleValue scaleBiomeGrowthRate;
    public ModConfigSpec.DoubleValue diseaseChance;
    public ModConfigSpec.IntValue maxBranchRotRadius;
    public ModConfigSpec.DoubleValue rootyBlockHardnessMultiplier;
    public ModConfigSpec.EnumValue<SwampSpecies.WaterSurfaceGenerationState> swampOaksInWater;
    public ModConfigSpec.IntValue boneMealGrowthPulses;
    public ModConfigSpec.BooleanValue hideCreakingHeart;

    public ModConfigSpec.BooleanValue isLeavesPassable;
    public ModConfigSpec.BooleanValue vanillaLeavesCollision;
    public ModConfigSpec.BooleanValue enableBranchClimbing;
    public ModConfigSpec.BooleanValue enableCanopyCrash;
    public ModConfigSpec.EnumValue<DynamicTrees.AxeDamage> axeDamageMode;
    public ModConfigSpec.BooleanValue enableFallingTrees;
    public ModConfigSpec.BooleanValue enableFallingTreeDamage;
    public ModConfigSpec.DoubleValue fallingTreeDamageMultiplier;
    public ModConfigSpec.BooleanValue dirtBucketPlacesDirt;
    public ModConfigSpec.BooleanValue sloppyBreakDrops;
    public ModConfigSpec.IntValue minRadiusForStrip;
    public ModConfigSpec.BooleanValue enableStripRadiusReduction;
    public ModConfigSpec.BooleanValue canBoneMealFruit;
    public ModConfigSpec.BooleanValue canBoneMealPods;
    public ModConfigSpec.BooleanValue dynamicSaplingDrops;

    public ModConfigSpec.BooleanValue replaceVanillaSaplings;
    public ModConfigSpec.BooleanValue replaceNyliumFungi;
    public ModConfigSpec.BooleanValue cancelVanillaVillageTrees;
    public ModConfigSpec.IntValue maxFallingTreeLeavesParticles;

    public ModConfigSpec.BooleanValue generatePodzol;
    public ModConfigSpec.BooleanValue worldGen;
    public ModConfigSpec.ConfigValue<List<? extends String>> dimensionBlacklist;

    public ModConfigSpec.BooleanValue generateDirtBucketRecipes;
    public ModConfigSpec.BooleanValue generateMegaSeedRecipe;
    public ModConfigSpec.ConfigValue<String> biocharBrewingBase;

    public ModConfigSpec.ConfigValue<String> preferredSeasonMod;
    public ModConfigSpec.BooleanValue enableSeasonalSeedDrop;
    public ModConfigSpec.BooleanValue enableSeasonalGrowth;
    public ModConfigSpec.BooleanValue enableSeasonalFruitProduction;
    public ModConfigSpec.DoubleValue wetSeasonOffset;

    public ModConfigSpec.BooleanValue debug;

    private DTConfigs() {}

    private static DTConfigs buildServerConfig(ModConfigSpec.Builder builder) {
        DTConfigs config = new DTConfigs();

        builder.push("seeds");
        config.leavesSeedDropRate = builder.defineInRange("leavesSeedDropRate", 1.0, 0.0, 64.0);
        config.minSeasonalLeavesSeedDropRate = builder.defineInRange("minSeasonalLeavesSeedDropRate", 0.15, 0.0, 1.0);
        config.voluntarySeedDropRate = builder.defineInRange("voluntarySeedDropRate", 0.01, 0.0, 1.0);
        config.minSeasonalVoluntarySeedDropRate = builder.defineInRange("minSeasonalVoluntarySeedDropRate", 0.0, 0.0, 1.0);
        config.seedPlantRate = builder.defineInRange("seedPlantRate", 1.0 / 6.0, 0.0, 1.0);
        config.seedTimeToLive = builder.defineInRange("seedTimeToLive", 1200, 0, 6000);
        config.seedOnlyForest = builder.define("seedOnlyForest", true);
        config.seedMinForestness = builder.defineInRange("seedMinForestness", 0.0, 0.0, 1.0);
        config.climateAffectsFruitsAndPods = builder.define("climateAffectsFruitsAndPods", true);
        builder.pop();

        builder.push("trees");
        config.treeGrowthMultiplier = builder.defineInRange("treeGrowthMultiplier", 0.5, 0, 16.0);
        config.treeHarvestMultiplier = builder.defineInRange("treeHarvestMultiplier", 1.0, 0.0, 128.0);
        config.maxTreeHardness = builder.defineInRange("maxTreeHardness", 20.0, 1.0, 200.0);
        config.treeHardnessMultiplier = builder.defineInRange("treeHardnessMultiplier", 1.0, 1.0/128.0, 32.0);
        config.dropSticks = builder.define("dropSticks", true);
        config.scaleBiomeGrowthRate = builder.defineInRange("scaleBiomeGrowthRate", 0.5, 0.0, 1.0);
        config.diseaseChance = builder.defineInRange("diseaseChance", 0.0, 0.0, 1.0);
        config.maxBranchRotRadius = builder.defineInRange("maxBranchRotRadius", 7, 0, ThickBranchBlock.MAX_RADIUS_THICK);
        config.rootyBlockHardnessMultiplier = builder.defineInRange("rootyBlockHardnessMultiplier", 40.0, 0.0, 128.0);
        config.swampOaksInWater = builder.defineEnum("swampOaksInWater", SwampSpecies.WaterSurfaceGenerationState.ROOTED);
        config.boneMealGrowthPulses = builder.defineInRange("boneMealGrowthPulses", 1, 1, 512);
        config.hideCreakingHeart = builder.define("hideCreakingHeart", true);
        builder.pop();

        builder.push("interaction");
        config.isLeavesPassable = builder.define("isLeavesPassable", false);
        config.vanillaLeavesCollision = builder.define("vanillaLeavesCollision", false);
        config.enableBranchClimbing = builder.define("enableBranchClimbing", true);
        config.enableCanopyCrash = builder.define("enableCanopyCrash", true);
        config.axeDamageMode = builder.defineEnum("axeDamageMode", DynamicTrees.AxeDamage.THICKNESS);
        config.enableFallingTrees = builder.define("enableFallingTrees", true);
        config.enableFallingTreeDamage = builder.define("enableFallingTreeDamage", true);
        config.fallingTreeDamageMultiplier = builder.defineInRange("fallingTreeDamageMultiplier", 1.0, 0.0, 100.0);
        config.dirtBucketPlacesDirt = builder.define("dirtBucketPlacesDirt", true);
        config.sloppyBreakDrops = builder.define("sloppyBreakDrops", false);
        config.minRadiusForStrip = builder.defineInRange("minRadiusForStrip", 6, 0, 24);
        config.enableStripRadiusReduction = builder.define("enableStripRadiusReduction", true);
        config.canBoneMealFruit = builder.define("canBoneMealFruit", false);
        config.canBoneMealPods = builder.define("canBoneMealPods", true);
        config.dynamicSaplingDrops = builder.define("dynamicSaplingDrops", true);
        builder.pop();

        builder.push("world");
        config.generatePodzol = builder.define("generatePodzol", true);
        config.worldGen = builder.define("worldGen", true);
        config.dimensionBlacklist = builder.define("dimensionsBlacklist", new ArrayList<>());
        builder.pop();

        builder.push("debug");
        config.debug = builder.define("debug", false);
        builder.pop();

        return config;
    }

    private static DTConfigs buildCommonConfig(ModConfigSpec.Builder builder) {
        DTConfigs config = new DTConfigs();

        builder.push("vanilla");
        config.replaceVanillaSaplings = builder.define("replaceVanillaSaplings", false);
        config.replaceNyliumFungi = builder.define("replaceNyliumFungi", true);
        config.cancelVanillaVillageTrees = builder.define("cancelVanillaVillageTrees", true);
        config.maxFallingTreeLeavesParticles = builder.defineInRange("maxFallingTreeLeavesParticles", 400, 0, 4096);
        builder.pop();

        builder.push("misc");
        config.generateDirtBucketRecipes = builder.define("generateDirtBucketRecipes", true);
        config.generateMegaSeedRecipe = builder.define("generateMegaSeedRecipe", true);
        config.biocharBrewingBase = builder.define("biocharBrewingBase", "minecraft:thick");
        builder.pop();

        builder.push("integration");
        config.preferredSeasonMod = builder.define("preferredSeasonMod", SeasonCompatibilityHandler.ANY);
        config.enableSeasonalSeedDrop = builder.define("enableSeasonalSeedDropFactor", true);
        config.enableSeasonalGrowth = builder.define("enableSeasonalGrowthFactor", true);
        config.enableSeasonalFruitProduction = builder.define("enableSeasonalFruitProductionFactor", true);
        config.wetSeasonOffset = builder.defineInRange("wetSeasonOffset", 2.5, 0.0, 4.0);
        builder.pop();

        return config;
    }

    private static DTConfigs buildClientConfig(ModConfigSpec.Builder builder) {
        return new DTConfigs();
    }

}
