package com.dtteam.dynamictrees.config;

import com.dtteam.dynamictrees.DynamicTrees;
import com.dtteam.dynamictrees.block.branch.ThickBranchBlock;
import com.dtteam.dynamictrees.platform.services.IConfigHelper;
import com.dtteam.dynamictrees.systems.season.SeasonCompatibilityHandler;
import com.dtteam.dynamictrees.tree.species.SwampSpecies;
import com.mojang.datafixers.util.Pair;

public class DTConfigs {
    public static SimpleConfig CONFIG;
    private static DTConfigProvider configs;
    public static boolean isLoaded = false;

    public static void registerConfigs() {
        configs = new DTConfigProvider();
        createConfigs();

        CONFIG = SimpleConfig.of(DynamicTrees.MOD_ID).provider(configs).request();
        isLoaded = true;
    }

    private static void createConfigs() {
        configs.addSection("seeds");
        createConfig("The rate at which seeds drop from leaves.",
                IConfigHelper.LEAVES_SEED_DROP_RATE, 1D, 0D, 64D);
        createConfig("The rate at which seeds voluntarily drop from branches",
                IConfigHelper.VOLUNTARY_SEED_DROP_RATE, 0.01, 0.0, 1.0);
        createConfig("The rate at which seeds voluntarily plant themselves in their ideal biomes",
                IConfigHelper.SEED_PLANT_RATE, 1f / 6f, 0.0, 1.0);
        createConfig("Ticks before a seed in the world attempts to plant itself or despawn. 1200 = 1 minute",
                IConfigHelper.SEED_TIME_TO_LIVE, 1200, 0, 6000);
        createConfig("If enabled then seeds will only voluntarily plant themselves in forest-like biomes.",
                IConfigHelper.SEED_ONLY_FOREST, true);
        createConfig("The minimum forestness that non-forest-like biomes can have. 0 = is not at all a forest, 1 = may as well be a forest. Can be fractional.",
                IConfigHelper.SEED_MIN_FORESTNESS, 0.0, 0.0, 1.0);

        configs.addSection("trees");
        createConfig("Factor that multiplies the rate at which trees grow. Use at own risk",
                IConfigHelper.TREE_GROWTH_MULTIPLIER, 0.5f, 0, 16f);
        createConfig("Factor that multiplies the wood returned from harvesting a tree.  You cheat.",
                IConfigHelper.TREE_HARVEST_MULTIPLIER, 1f, 0f, 128f);
        createConfig("Maximum harvesting hardness that can be calculated. Regardless of tree thickness.",
                IConfigHelper.MAX_TREE_HARDNESS, 20f, 1f, 200f);
        createConfig("A multiplier of tree hardness. Higher values make trees slower to chop, lower values makes them faster to chop.",
                IConfigHelper.TREE_HARDNESS_MULTIPLIER, 1, (1/128f), 32f);
        createConfig("If enabled then sticks will be dropped for partial logs",
                IConfigHelper.DROP_STICKS, true);
        createConfig("Scales the growth for the environment.  0.5f is nominal. 0.0 trees only grow in their native biome. 1.0 trees grow anywhere like they are in their native biome",
                IConfigHelper.SCALE_BIOME_GROWTH_RATE, 0.5f, 0.0f, 1.0f);
        createConfig("The chance of a tree on depleted soil to die. 1/256(~0.004) averages to about 1 death every 16 minecraft days",
                IConfigHelper.DISEASE_CHANCE, 0.0f, 0.0f, 1.0f);
        createConfig("The maximum radius of a branch that is allowed to postRot away. 8 = Full block size. 24 = Full 3x3 thick size. Set to 0 to prevent rotting",
                IConfigHelper.MAX_BRANCH_ROT_RADIUS, 5, 0, ThickBranchBlock.MAX_RADIUS_THICK);
        createConfig("How much harder it is to destroy a rooty block compared to its non-rooty state",
                IConfigHelper.ROOTY_BLOCK_HARDNESS_MULTIPLIER, 40f, 0f, 128f);
        createConfig("Options for how oak trees generate in swamps. ROOTED: Swamp oak trees will generate on shallow water with mangrove-like roots. SUNK: Swamp oak trees will generate on shallow water one block under the surface. DISABLED: Swamp oaks will not generate on water.",
                IConfigHelper.SWAMP_OAKS_IN_WATER, SwampSpecies.WaterSurfaceGenerationState.ROOTED.toString());
        createConfig("The amount of growth pulses to send when bone meal is applied to a tree. Warning: setting values higher than 64 is not recommended other than for testing purposes. ",
                IConfigHelper.BONE_MEAL_GROWTH_PULSES, 1, 1, 512);

        configs.addSection("interaction");
        createConfig("If enabled all leaves will be passable. If the Passable Foliage mod is installed this config is overridden as true.",
                IConfigHelper.IS_LEAVES_PASSABLE, false);
        createConfig("If enabled player movement on leaves will not be enhanced.",
                IConfigHelper.VANILLA_LEAVES_COLLISION, false);
        createConfig("If enabled then thinner branches can be climbed",
                IConfigHelper.ENABLE_BRANCH_CLIMBING, true);
        createConfig("If enabled players receive reduced fall damage on leaves at the expense of the block(s) destruction",
                IConfigHelper.ENABLE_CANOPY_CRASH, true);
        createConfig("Damage dealt to the axe item when cutting a tree down. VANILLA: Standard 1 Damage. THICKNESS: By Branch/Trunk Thickness. VOLUME: By Tree Volume.",
                IConfigHelper.AXE_DAMAGE_MODE, DynamicTrees.AxeDamage.THICKNESS.toString());
        createConfig("If enabled then trees will fall over when harvested",
                IConfigHelper.ENABLE_FALLING_TREES, true);
        createConfig("If enabled then trees will harm living entities when falling",
                IConfigHelper.ENABLE_FALLING_TREE_DAMAGE, true);
        createConfig("Multiplier for damage incurred by a falling tree",
                IConfigHelper.FALLING_TREE_DAMAGE_MULTIPLIER, 1.0, 0.0, 100.0);
        createConfig("If enabled the Dirt Bucket will place a dirt block on right-click",
                IConfigHelper.DIRT_BUCKET_PLACES_DIRT, true);
        createConfig("If enabled then improperly broken trees(not by an entity) will still drop wood.",
                IConfigHelper.SLOPPY_BREAK_DROPS, false);
        createConfig("The minimum radius a branch must have before its able to be stripped. 8 = Full block size. Set to 0 to disable stripping trees",
                IConfigHelper.MIN_RADIUS_FOR_STRIP, 6, 0, 24);
        createConfig("If enabled, stripping a branch will decrease its radius by one",
                IConfigHelper.ENABLE_STRIP_RADIUS_REDUCTION, true);
        createConfig("Sets the default for whether or not fruit growing from dynamic trees can be bone-mealed. Note that this is a default; it can be overridden by the individual fruit.",
                IConfigHelper.CAN_BONE_MEAL_FRUIT, false);
        createConfig("Sets the default for whether or not pods growing from dynamic trees can be bone-mealed. Note that this is a default; it can be overridden by the individual pod.",
                IConfigHelper.CAN_BONE_MEAL_PODS, true);
        createConfig("If enabled, dynamic sapling blocks will drop their seed when broken.",
                IConfigHelper.DYNAMIC_SAPLING_DROPS, true);

        configs.addSection("vanilla");
        createConfig("Right clicking with a vanilla sapling places a dynamic sapling instead.",
                IConfigHelper.REPLACE_VANILLA_SAPLINGS, false);
        createConfig("Crimson Fungus and Warped Fungus that sprout from nylium will be dynamic instead.",
                IConfigHelper.REPLACE_NYLIUM_FUNGI, true);
        createConfig("If enabled, cancels the non-dynamic trees that spawn with vanilla villages.",
                IConfigHelper.CANCEL_VANILLA_VILLAGE_TREES, true);
        createConfig("The maximum number of leaves blocks that will fling particles when a falling tree crashes into the ground. Higher values might have a performance impact.",
                IConfigHelper.MAX_FALLING_TREE_LEAVES_PARTICLES, 400, 0, 4096);

        configs.addSection("world");
        createConfig("Randomly generate podzol under select trees like spruce.",
                IConfigHelper.GENERATE_PODZOL, true);
        createConfig("World Generation produces Dynamic Trees instead of Vanilla trees.",
                IConfigHelper.WORLD_GEN, true);
        createConfig("Blacklist of dimension registry names for disabling Dynamic Tree worldgen.",
                IConfigHelper.DIMENSION_BLACK_LIST, "[]");

        configs.addSection("misc");
        createConfig("If enabled, dirt bucket recipes will be automatically generated.",
                IConfigHelper.GENERATE_DIRT_BUCKET_RECIPES, true);
        createConfig("The base potion the Biochar Base is brewed from. Minecraft potions use 'awkward'. If you change this, don't forget to update the patchouli manual page too.",
                IConfigHelper.BIOCHAR_BREWING_BASE, "minecraft:thick");

        configs.addSection("integration");
        createConfig("The mod ID of preferred season mod. If a season provider for this mod ID is present, it will be used for integration with seasons. Set this to \"!\" to disable integration or \"*\" to accept the any integration (the first available).",
                IConfigHelper.PREFERRED_SEASON_MOD, SeasonCompatibilityHandler.ANY);
        createConfig("If enabled, seed drop rates will be multiplied based on the current season (requires serene seasons).",
                IConfigHelper.ENABLE_SEASONAL_SEED_DROP, true);
        createConfig("If enabled, growth rates will be multiplied based on the current season (requires serene seasons).",
                IConfigHelper.ENABLE_SEASONAL_GROWTH, true);
        createConfig("If enabled, fruit production rates will be multiplied based on the current season (requires serene seasons).",
                IConfigHelper.ENABLE_SEASONAL_SEED_FRUIT_PRODUCTION, true);
        createConfig("The seasonal offset of the wet season relative to summer. Tropical and arid climates use wet/dry seasons instead of regular summer/fall/winter/spring seasons. Tree growth and fruit production usually peak during the wet season. If set to 0.0 the wet season happens at the same time as summer. The default of 1.5 means it happens between fall and winter.",
                IConfigHelper.WET_SEASON_OFFSET, 1.5, 0.0, 4.0);

//		configs.addSection("visuals");
//		fancyThickRings = createConfig("Rings of thick trees are rendered using a texture created with an expanded tangram construction technique. Otherwise the ring texture is simply stretched",
//		"fancyThickRings", true);
//		CLIENT_BUILDER.pop();

        configs.addSection("debug");
        createConfig("Enable to mark tree spawn locations with concrete circles.",
                IConfigHelper.DEBUG, false);
    }

    private static <T> void createConfig(String comment, String id, T def){
        configs.addKeyValuePair(new Pair<>(id, def), comment, null);
    }
    private static <T> void createConfig(String comment, String id, T def, T rangeMin, T rangeMax){
        configs.addKeyValuePair(new Pair<>(id, def), comment, new Pair<>(rangeMin, rangeMax));
    }

    public static Pair<Class<?>, ?> getDefaultValue (String id){
        return configs.getDefaultValues().get(id);
    }

}