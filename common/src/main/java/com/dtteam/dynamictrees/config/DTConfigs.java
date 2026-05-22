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
    public ModConfigSpec.BooleanValue sampleNoiseBiome;

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

        builder.comment("Seed Settings").push("seeds");
        config.leavesSeedDropRate = builder.comment("The rate at which seeds drop from leaves.")
                .defineInRange("leavesSeedDropRate", 1.0, 0.0, 64.0);
        config.minSeasonalLeavesSeedDropRate = builder.comment("The minimum chance for seed dropping from leaves when a seasonal mod is installed. 0 = during the off season seeds never drop from leaves, 1 = seeds will drop at maximum rate during the entire year. Can be fractional.")
                .defineInRange("minSeasonalLeavesSeedDropRate", 0.15, 0.0, 1.0);
        config.voluntarySeedDropRate = builder.comment("The rate at which seeds voluntarily drop from branches")
                .defineInRange("voluntarySeedDropRate", 0.01, 0.0, 1.0);
        config.minSeasonalVoluntarySeedDropRate = builder.comment("The minimum chance for seed dropping voluntarily when a seasonal mod is installed. 0 = during the off season seeds never drop voluntarily, 1 = seeds will drop at maximum rate during the entire year. Can be fractional.")
                .defineInRange("minSeasonalVoluntarySeedDropRate", 0.0, 0.0, 1.0);
        config.seedPlantRate = builder.comment("The rate at which seeds voluntarily plant themselves in their ideal biomes")
                .defineInRange("seedPlantRate", 1.0 / 6.0, 0.0, 1.0);
        config.seedTimeToLive = builder.comment("Ticks before a seed in the world attempts to plant itself or despawn. 1200 = 1 minute")
                .defineInRange("seedTimeToLive", 1200, 0, 6000);
        config.seedOnlyForest = builder.comment("If enabled then seeds will only voluntarily plant themselves in forest-like biomes.")
                .define("seedOnlyForest", true);
        config.seedMinForestness = builder.comment("The minimum forestness that non-forest-like biomes can have. 0 = is not at all a forest, 1 = may as well be a forest. Can be fractional.")
                .defineInRange("seedMinForestness", 0.0, 0.0, 1.0);
        config.climateAffectsFruitsAndPods = builder.comment("If enabled, fruit and pod production will be affected by the current biome's climate.")
                .define("climateAffectsFruitsAndPods", true);
        builder.pop();

        builder.comment("Tree Settings").push("trees");
        config.treeGrowthMultiplier = builder.comment("Factor that multiplies the rate at which trees grow. Use at own risk")
                .defineInRange("treeGrowthMultiplier", 0.5, 0, 16.0);
        config.treeHarvestMultiplier = builder.comment("Factor that multiplies the wood returned from harvesting a tree.  You cheat.")
                .defineInRange("treeHarvestMultiplier", 1.0, 0.0, 128.0);
        config.maxTreeHardness = builder.comment("Maximum harvesting hardness that can be calculated. Regardless of tree thickness.")
                .defineInRange("maxTreeHardness", 20.0, 1.0, 200.0);
        config.treeHardnessMultiplier = builder.comment("A multiplier of tree hardness. Higher values make trees slower to chop, lower values makes them faster to chop.")
                .defineInRange("treeHardnessMultiplier", 1.0, 1.0/128.0, 32.0);
        config.dropSticks = builder.comment("If enabled then sticks will be dropped for partial logs")
                .define("dropSticks", true);
        config.scaleBiomeGrowthRate = builder.comment("Scales the growth for the environment.  0.5f is nominal. 0.0 trees only grow in their native biome. 1.0 trees grow anywhere like they are in their native biome")
                .defineInRange("scaleBiomeGrowthRate", 0.5, 0.0, 1.0);
        config.diseaseChance = builder.comment("The chance of a tree on depleted soil to die. 1/256(~0.004) averages to about 1 death every 16 minecraft days")
                .defineInRange("diseaseChance", 0.0, 0.0, 1.0);
        config.maxBranchRotRadius = builder.comment("The maximum radius of a branch that is allowed to postRot away. 8 = Full block size. 24 = Full 3x3 thick size. Set to 0 to prevent rotting")
                .defineInRange("maxBranchRotRadius", 7, 0, ThickBranchBlock.MAX_RADIUS_THICK);
        config.rootyBlockHardnessMultiplier = builder.comment("How much harder it is to destroy a rooty block compared to its non-rooty state")
                .defineInRange("rootyBlockHardnessMultiplier", 40.0, 0.0, 128.0);
        config.swampOaksInWater = builder.comment("Options for how oak trees generate in swamps. ROOTED: Swamp oak trees will generate on shallow water with mangrove-like roots. SUNK: Swamp oak trees will generate on shallow water one block under the surface. DISABLED: Swamp oaks will not generate on water.")
                .defineEnum("swampOaksInWater", SwampSpecies.WaterSurfaceGenerationState.ROOTED);
        config.boneMealGrowthPulses = builder.comment("The amount of growth pulses to send when bone meal is applied to a tree. Warning: setting values higher than 64 is not recommended other than for testing purposes. ")
                .defineInRange("boneMealGrowthPulses", 1, 1, 512);
        config.hideCreakingHeart = builder.comment("If enabled, creaking hearts will camouflage themselves as regular branches until they are stripped by the player. This helps make them less obvious when walking through a Pale Garden.")
                .define("hideCreakingHeart", true);
        builder.pop();

        builder.comment("Interaction Settings").push("interaction");
        config.isLeavesPassable = builder.comment("If enabled all leaves will be passable. If the Passable Foliage mod is installed this config is overridden")
                .define("isLeavesPassable", false);
        config.vanillaLeavesCollision = builder.comment("If enabled player movement on leaves will not be enhanced")
                .define("vanillaLeavesCollision", false);
        config.enableBranchClimbing = builder.comment("If enabled then thinner branches can be climbed")
                .define("enableBranchClimbing", true);
        config.enableCanopyCrash = builder.comment("If enabled players receive reduced fall damage on leaves at the expense of the block(s) destruction")
                .define("enableCanopyCrash", true);
        config.axeDamageMode = builder.comment("Damage dealt to the axe item when cutting a tree down. VANILLA: Standard 1 Damage. THICKNESS: By Branch/Trunk Thickness. VOLUME: By Tree Volume.")
                .defineEnum("axeDamageMode", DynamicTrees.AxeDamage.THICKNESS);
        config.enableFallingTrees = builder.comment("If enabled then trees will fall over when harvested")
                .define("enableFallingTrees", true);
        config.enableFallingTreeDamage = builder.comment("If enabled then trees will harm living entities when falling")
                .define("enableFallingTreeDamage", true);
        config.fallingTreeDamageMultiplier = builder.comment("Multiplier for damage incurred by a falling tree")
                .defineInRange("fallingTreeDamageMultiplier", 1.0, 0.0, 100.0);
        config.dirtBucketPlacesDirt = builder.comment("If enabled the Dirt Bucket will place a dirt block on right-click")
                .define("dirtBucketPlacesDirt", true);
        config.sloppyBreakDrops = builder.comment("If enabled then improperly broken trees(not by an entity) will still drop wood.")
                .define("sloppyBreakDrops", false);
        config.minRadiusForStrip = builder.comment("The minimum radius a branch must have before its able to be stripped. 8 = Full block size. Set to 0 to disable stripping trees")
                .defineInRange("minRadiusForStrip", 6, 0, 24);
        config.enableStripRadiusReduction = builder.comment("If enabled, stripping a branch will decrease its radius by one")
                .define("enableStripRadiusReduction", true);
        config.canBoneMealFruit = builder.comment("Sets the default for whether or not fruit growing from dynamic trees can be bone-mealed. Note that this is a default; it can be overridden by the individual fruit.")
                .define("canBoneMealFruit", false);
        config.canBoneMealPods = builder.comment("Sets the default for whether or not pods growing from dynamic trees can be bone-mealed. Note that this is a default; it can be overridden by the individual pod.")
                .define("canBoneMealPods", true);
        config.dynamicSaplingDrops = builder.comment("If enabled, dynamic sapling blocks will drop their seed when broken.")
                .define("dynamicSaplingDrops", true);
        builder.pop();

        builder.comment("World Generation Settings").push("world");
        config.generatePodzol = builder.comment("Randomly generate podzol under select trees like spruce.")
                .define("generatePodzol", true);
        config.worldGen = builder.comment("World Generation produces Dynamic Trees instead of Vanilla trees.")
                .define("worldGen", true);
        config.dimensionBlacklist = builder.comment("Blacklist of dimension registry names for disabling Dynamic Tree worldgen")
                .define("dimensionsBlacklist", new ArrayList<>());
        config.sampleNoiseBiome = builder.comment("Dynamic Trees sample the biome noise map instead of the actual biome when placing trees. Sampling the noise biome may cause issues with tools like world painter. Sampling the real biome may cause freezing during world generation.")
                .define("sampleNoiseBiome", true);
        builder.pop();

        builder.comment("Debug Settings").push("debug");
        config.debug = builder.comment("Enable to mark tree spawn locations with concrete circles.")
                .define("debug", false);
        builder.pop();

        return config;
    }

    private static DTConfigs buildCommonConfig(ModConfigSpec.Builder builder) {
        DTConfigs config = new DTConfigs();

        builder.comment("Vanilla Trees Settings").push("vanilla");
        config.replaceVanillaSaplings = builder.comment("Right clicking with a vanilla sapling places a dynamic sapling instead.")
                .define("replaceVanillaSaplings", false);
        config.replaceNyliumFungi = builder.comment("Crimson Fungus and Warped Fungus that sprout from nylium will be dynamic instead.")
                .define("replaceNyliumFungi", true);
        config.cancelVanillaVillageTrees = builder.comment("If enabled, cancels the non-dynamic trees that spawn with vanilla villages.")
                .define("cancelVanillaVillageTrees", true);
        config.maxFallingTreeLeavesParticles = builder.comment("The maximum number of leaves blocks that will fling particles when a falling tree crashes into the ground. Higher values might have a performance impact.")
                .defineInRange("maxFallingTreeLeavesParticles", 400, 0, 4096);
        builder.pop();

        builder.comment("Miscellaneous Settings").push("misc");
        config.generateDirtBucketRecipes = builder.comment("If enabled, dirt bucket recipes will be automatically generated.")
                .define("generateDirtBucketRecipes", true);
        config.generateMegaSeedRecipe = builder.comment("If enabled, seeds for mega species can be crafted with four regular seeds.")
                .define("generateMegaSeedRecipe", true);
        config.biocharBrewingBase = builder.comment("The base potion the Biochar Base is brewed from. Minecraft potions use 'awkward'. If you change this, don't forget to update the patchouli manual page too.")
                .define("biocharBrewingBase", "minecraft:thick");
        builder.pop();

        builder.comment("Mod Integration Settings").push("integration");
        config.preferredSeasonMod = builder.comment("The mod ID of preferred season mod. If a season provider for this mod ID is present, it will be used for integration with seasons. Set this to \"!\" to disable integration or \"*\" to accept the any integration (the first available).")
                .define("preferredSeasonMod", SeasonCompatibilityHandler.ANY);
        config.enableSeasonalSeedDrop = builder.comment("If enabled, seed drop rates will be multiplied based on the current season (requires serene seasons).")
                .define("enableSeasonalSeedDropFactor", true);
        config.enableSeasonalGrowth = builder.comment("If enabled, growth rates will be multiplied based on the current season (requires serene seasons).")
                .define("enableSeasonalGrowthFactor", true);
        config.enableSeasonalFruitProduction = builder.comment("If enabled, fruit production rates will be multiplied based on the current season (requires serene seasons).")
                .define("enableSeasonalFruitProductionFactor", true);
        config.wetSeasonOffset = builder.comment("The seasonal offset of the wet season relative to summer. Tropical and arid climates use wet/dry seasons instead of regular summer/fall/winter/spring seasons. Tree growth and fruit production usually peak during the wet season. If set to 0.0 the wet season happens at the same time as summer. The default of 2.5 means it happens between fall and winter.")
                .defineInRange("wetSeasonOffset", 2.5, 0.0, 4.0);
        builder.pop();

        return config;
    }

    private static DTConfigs buildClientConfig(ModConfigSpec.Builder builder) {
        return new DTConfigs();
    }

}
