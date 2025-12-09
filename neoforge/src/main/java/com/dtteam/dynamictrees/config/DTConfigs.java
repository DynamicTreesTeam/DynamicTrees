package com.dtteam.dynamictrees.config;

import com.dtteam.dynamictrees.DynamicTrees;
import com.dtteam.dynamictrees.block.branch.ThickBranchBlock;
import com.dtteam.dynamictrees.event.handler.OptionalHandlers;
import com.dtteam.dynamictrees.platform.services.IConfigHelper;
import com.dtteam.dynamictrees.systems.season.SeasonCompatibilityHandler;
import com.dtteam.dynamictrees.tree.species.SwampSpecies;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.config.ModConfigEvent;
import net.neoforged.fml.loading.FMLPaths;
import net.neoforged.neoforge.common.ModConfigSpec;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

@EventBusSubscriber(modid = DynamicTrees.MOD_ID, bus = EventBusSubscriber.Bus.MOD)
public class DTConfigs {

    public static final File CONFIG_DIRECTORY;

    public static final Map<String, ModConfigSpec.ConfigValue<?>> CONFIGS = new HashMap<>();

    public static final ModConfigSpec SERVER_CONFIG;
    public static final ModConfigSpec COMMON_CONFIG;
    public static final ModConfigSpec CLIENT_CONFIG;

    private static <V, T extends ModConfigSpec.ConfigValue<V>> T registerConfig (T config){
        CONFIGS.put(config.getPath().getLast(), config);
        return config;
    }

    static {
        CONFIG_DIRECTORY = new File(FMLPaths.CONFIGDIR.get().toUri());

        final ModConfigSpec.Builder SERVER_BUILDER = new ModConfigSpec.Builder();
        final ModConfigSpec.Builder COMMON_BUILDER = new ModConfigSpec.Builder();
        final ModConfigSpec.Builder CLIENT_BUILDER = new ModConfigSpec.Builder();

        SERVER_BUILDER.comment("Seed Settings").push("seeds");
        registerConfig(SERVER_BUILDER.comment("The rate at which seeds drop from leaves.").
                defineInRange(IConfigHelper.LEAVES_SEED_DROP_RATE, 1.0, 0.0, 64.0));
        registerConfig(SERVER_BUILDER.comment("The minimum chance for seed dropping from leaves when a seasonal mod is installed. 0 = during the off season seeds never drop from leaves, 1 = seeds will drop at maximum rate during the entire year. Can be fractional.").
                defineInRange(IConfigHelper.MIN_SEASONAL_LEAVES_SEED_DROP_RATE, 0.15, 0.0, 1.0));
        registerConfig(SERVER_BUILDER.comment("The rate at which seeds voluntarily drop from branches").
                defineInRange(IConfigHelper.VOLUNTARY_SEED_DROP_RATE, 0.01, 0.0, 1.0));
        registerConfig(SERVER_BUILDER.comment("The minimum chance for seed dropping voluntarily when a seasonal mod is installed. 0 = during the off season seeds never drop voluntarily, 1 = seeds will drop at maximum rate during the entire year. Can be fractional.").
                defineInRange(IConfigHelper.MIN_SEASONAL_VOLUNTARY_SEED_DROP_RATE, 0.0, 0.0, 1.0));
        registerConfig(SERVER_BUILDER.comment("The rate at which seeds voluntarily plant themselves in their ideal biomes").
                defineInRange(IConfigHelper.SEED_PLANT_RATE, 1f / 6f, 0.0, 1.0));
        registerConfig(SERVER_BUILDER.comment("Ticks before a seed in the world attempts to plant itself or despawn. 1200 = 1 minute").
                defineInRange(IConfigHelper.SEED_TIME_TO_LIVE, 1200, 0, 6000));
        registerConfig(SERVER_BUILDER.comment("If enabled then seeds will only voluntarily plant themselves in forest-like biomes.").
                define(IConfigHelper.SEED_ONLY_FOREST, true));
        registerConfig(SERVER_BUILDER.comment("The minimum forestness that non-forest-like biomes can have. 0 = is not at all a forest, 1 = may as well be a forest. Can be fractional.").
                defineInRange(IConfigHelper.SEED_MIN_FORESTNESS, 0.0, 0.0, 1.0));
        SERVER_BUILDER.pop();

        SERVER_BUILDER.comment("Tree Settings").push("trees");
        registerConfig(SERVER_BUILDER.comment("Factor that multiplies the rate at which trees grow. Use at own risk").
                defineInRange(IConfigHelper.TREE_GROWTH_MULTIPLIER, 0.5f, 0, 16f));
        registerConfig(SERVER_BUILDER.comment("Factor that multiplies the wood returned from harvesting a tree.  You cheat.").
                defineInRange(IConfigHelper.TREE_HARVEST_MULTIPLIER, 1f, 0f, 128f));
        registerConfig(SERVER_BUILDER.comment("Maximum harvesting hardness that can be calculated. Regardless of tree thickness.").
                defineInRange(IConfigHelper.MAX_TREE_HARDNESS, 20f, 1f, 200f));
        registerConfig(SERVER_BUILDER.comment("A multiplier of tree hardness. Higher values make trees slower to chop, lower values makes them faster to chop.").
                defineInRange(IConfigHelper.TREE_HARDNESS_MULTIPLIER, 1, (1/128f), 32f));
        registerConfig(SERVER_BUILDER.comment("If enabled then sticks will be dropped for partial logs").
                define(IConfigHelper.DROP_STICKS, true));
        registerConfig(SERVER_BUILDER.comment("Scales the growth for the environment.  0.5f is nominal. 0.0 trees only grow in their native biome. 1.0 trees grow anywhere like they are in their native biome").
                defineInRange(IConfigHelper.SCALE_BIOME_GROWTH_RATE, 0.5f, 0.0f, 1.0f));
        registerConfig(SERVER_BUILDER.comment("The chance of a tree on depleted soil to die. 1/256(~0.004) averages to about 1 death every 16 minecraft days").
                defineInRange(IConfigHelper.DISEASE_CHANCE, 0.0f, 0.0f, 1.0f));
        registerConfig(SERVER_BUILDER.comment("The maximum radius of a branch that is allowed to postRot away. 8 = Full block size. 24 = Full 3x3 thick size. Set to 0 to prevent rotting").
                defineInRange(IConfigHelper.MAX_BRANCH_ROT_RADIUS, 7, 0, ThickBranchBlock.MAX_RADIUS_THICK));
        registerConfig(SERVER_BUILDER.comment("How much harder it is to destroy a rooty block compared to its non-rooty state").
                defineInRange(IConfigHelper.ROOTY_BLOCK_HARDNESS_MULTIPLIER, 40f, 0f, 128f));
        registerConfig(SERVER_BUILDER.comment("Options for how oak trees generate in swamps. ROOTED: Swamp oak trees will generate on shallow water with mangrove-like roots. SUNK: Swamp oak trees will generate on shallow water one block under the surface. DISABLED: Swamp oaks will not generate on water.").
                defineEnum(IConfigHelper.SWAMP_OAKS_IN_WATER, SwampSpecies.WaterSurfaceGenerationState.ROOTED));
        registerConfig(SERVER_BUILDER.comment("The amount of growth pulses to send when bone meal is applied to a tree. Warning: setting values higher than 64 is not recommended other than for testing purposes. ").
                defineInRange(IConfigHelper.BONE_MEAL_GROWTH_PULSES, 1, 1, 512));
        SERVER_BUILDER.pop();

        SERVER_BUILDER.comment("Interaction Settings").push("interaction");
        registerConfig(SERVER_BUILDER.comment("If enabled all leaves will be passable. If the Passable Foliage mod is installed this config is overridden").
                define(IConfigHelper.IS_LEAVES_PASSABLE, false));
        registerConfig(SERVER_BUILDER.comment("If enabled player movement on leaves will not be enhanced").
                define(IConfigHelper.VANILLA_LEAVES_COLLISION, false));
        registerConfig(SERVER_BUILDER.comment("If enabled then thinner branches can be climbed").
                define(IConfigHelper.ENABLE_BRANCH_CLIMBING, true));
        registerConfig(SERVER_BUILDER.comment("If enabled players receive reduced fall damage on leaves at the expense of the block(s) destruction").
                define(IConfigHelper.ENABLE_CANOPY_CRASH, true));
        registerConfig(SERVER_BUILDER.comment("Damage dealt to the axe item when cutting a tree down. VANILLA: Standard 1 Damage. THICKNESS: By Branch/Trunk Thickness. VOLUME: By Tree Volume.").
                defineEnum(IConfigHelper.AXE_DAMAGE_MODE, DynamicTrees.AxeDamage.THICKNESS));
        registerConfig(SERVER_BUILDER.comment("If enabled then trees will fall over when harvested").
                define(IConfigHelper.ENABLE_FALLING_TREES, true));
        registerConfig(SERVER_BUILDER.comment("If enabled then trees will harm living entities when falling").
                define(IConfigHelper.ENABLE_FALLING_TREE_DAMAGE, true));
        registerConfig(SERVER_BUILDER.comment("Multiplier for damage incurred by a falling tree").
                defineInRange(IConfigHelper.FALLING_TREE_DAMAGE_MULTIPLIER, 1.0, 0.0, 100.0));
        registerConfig(SERVER_BUILDER.comment("If enabled the Dirt Bucket will place a dirt block on right-click").
                define(IConfigHelper.DIRT_BUCKET_PLACES_DIRT, true));
        registerConfig(SERVER_BUILDER.comment("If enabled then improperly broken trees(not by an entity) will still drop wood.").
                define(IConfigHelper.SLOPPY_BREAK_DROPS, false));
        registerConfig(SERVER_BUILDER.comment("The minimum radius a branch must have before its able to be stripped. 8 = Full block size. Set to 0 to disable stripping trees").
                defineInRange(IConfigHelper.MIN_RADIUS_FOR_STRIP, 6, 0, 24));
        registerConfig(SERVER_BUILDER.comment("If enabled, stripping a branch will decrease its radius by one").
                define(IConfigHelper.ENABLE_STRIP_RADIUS_REDUCTION, true));
        registerConfig(SERVER_BUILDER.comment("Sets the default for whether or not fruit growing from dynamic trees can be bone-mealed. Note that this is a default; it can be overridden by the individual fruit.").
                define(IConfigHelper.CAN_BONE_MEAL_FRUIT, false));
        registerConfig(SERVER_BUILDER.comment("Sets the default for whether or not pods growing from dynamic trees can be bone-mealed. Note that this is a default; it can be overridden by the individual pod.").
                define(IConfigHelper.CAN_BONE_MEAL_PODS, true));
        registerConfig(SERVER_BUILDER.comment("If enabled, dynamic sapling blocks will drop their seed when broken.").
                define(IConfigHelper.DYNAMIC_SAPLING_DROPS, true));
        SERVER_BUILDER.pop();

        COMMON_BUILDER.comment("Vanilla Trees Settings").push("vanilla");
        registerConfig(COMMON_BUILDER.comment("Right clicking with a vanilla sapling places a dynamic sapling instead.").
                define(IConfigHelper.REPLACE_VANILLA_SAPLINGS, false));
        registerConfig(COMMON_BUILDER.comment("Crimson Fungus and Warped Fungus that sprout from nylium will be dynamic instead.").
                define(IConfigHelper.REPLACE_NYLIUM_FUNGI, true));
        registerConfig(COMMON_BUILDER.comment("If enabled, cancels the non-dynamic trees that spawn with vanilla villages.").
                define(IConfigHelper.CANCEL_VANILLA_VILLAGE_TREES, true));
        registerConfig(COMMON_BUILDER.comment("The maximum number of leaves blocks that will fling particles when a falling tree crashes into the ground. Higher values might have a performance impact.").
                defineInRange(IConfigHelper.MAX_FALLING_TREE_LEAVES_PARTICLES, 400, 0, 4096));
        COMMON_BUILDER.pop();

        SERVER_BUILDER.comment("World Generation Settings").push("world");
        registerConfig(SERVER_BUILDER.comment("Randomly generate podzol under select trees like spruce.").
                define(IConfigHelper.GENERATE_PODZOL, true));
        registerConfig(SERVER_BUILDER.comment("World Generation produces Dynamic Trees instead of Vanilla trees.").
                define(IConfigHelper.WORLD_GEN, true));
        registerConfig(SERVER_BUILDER.comment("Blacklist of dimension registry names for disabling Dynamic Tree worldgen").
                define(IConfigHelper.DIMENSION_BLACK_LIST, new ArrayList<>()));
        SERVER_BUILDER.pop();

        COMMON_BUILDER.comment("Miscellaneous Settings").push("misc");
        registerConfig(COMMON_BUILDER.comment("If enabled, dirt bucket recipes will be automatically generated.")
                .define(IConfigHelper.GENERATE_DIRT_BUCKET_RECIPES, true));
        registerConfig(COMMON_BUILDER.comment("If enabled, seeds for mega species can be crafted with four regular seeds.")
                .define(IConfigHelper.GENERATE_MEGA_SEED_RECIPE, false));
        registerConfig(COMMON_BUILDER.comment("The base potion the Biochar Base is brewed from. Minecraft potions use 'awkward'. If you change this, don't forget to update the patchouli manual page too.")
                .define(IConfigHelper.BIOCHAR_BREWING_BASE, "minecraft:thick"));
        COMMON_BUILDER.pop();

        COMMON_BUILDER.comment("Mod Integration Settings").push("integration");
        registerConfig(COMMON_BUILDER.comment("The mod ID of preferred season mod. If a season provider for this mod ID is present, it will be used for integration with seasons. Set this to \"!\" to disable integration or \"*\" to accept the any integration (the first available).")
                .define(IConfigHelper.PREFERRED_SEASON_MOD, SeasonCompatibilityHandler.ANY));
        registerConfig(COMMON_BUILDER.comment("If enabled, seed drop rates will be multiplied based on the current season (requires serene seasons).").
                define(IConfigHelper.ENABLE_SEASONAL_SEED_DROP, true));
        registerConfig(COMMON_BUILDER.comment("If enabled, growth rates will be multiplied based on the current season (requires serene seasons).").
                define(IConfigHelper.ENABLE_SEASONAL_GROWTH, true));
        registerConfig(COMMON_BUILDER.comment("If enabled, fruit production rates will be multiplied based on the current season (requires serene seasons).").
                define(IConfigHelper.ENABLE_SEASONAL_SEED_FRUIT_PRODUCTION, true));
        registerConfig(COMMON_BUILDER.comment("The seasonal offset of the wet season relative to summer. Tropical and arid climates use wet/dry seasons instead of regular summer/fall/winter/spring seasons. Tree growth and fruit production usually peak during the wet season. If set to 0.0 the wet season happens at the same time as summer. The default of 2.5 means it happens between fall and winter.").
                defineInRange(IConfigHelper.WET_SEASON_OFFSET, 2.5, 0.0, 4.0));

        COMMON_BUILDER.pop();

//		CLIENT_BUILDER.comment("Visual Settings").push("visuals");
//		fancyThickRings = CLIENT_BUILDER.comment("Rings of thick trees are rendered using a texture created with an expanded tangram construction technique. Otherwise the ring texture is simply stretched").
//				define("fancyThickRings", true);
//		CLIENT_BUILDER.pop();

        SERVER_BUILDER.comment("Debug Settings").push("debug");
        registerConfig(SERVER_BUILDER.comment("Enable to mark tree spawn locations with concrete circles.").
                define(IConfigHelper.DEBUG, false));
        SERVER_BUILDER.pop();

        SERVER_CONFIG = SERVER_BUILDER.build();
        COMMON_CONFIG = COMMON_BUILDER.build();
        CLIENT_CONFIG = CLIENT_BUILDER.build();
    }

    @SubscribeEvent
    public static void onLoad(final ModConfigEvent.Loading event) {
        OptionalHandlers.configReload();
        SeasonCompatibilityHandler.reloadSeasonManager();
    }

    @SubscribeEvent
    public static void onReload(final ModConfigEvent.Reloading event) {
        OptionalHandlers.configReload();
        SeasonCompatibilityHandler.reloadSeasonManager();
    }

}
