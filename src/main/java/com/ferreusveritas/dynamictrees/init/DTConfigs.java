package com.ferreusveritas.dynamictrees.init;

import com.ferreusveritas.dynamictrees.DynamicTrees;
import com.ferreusveritas.dynamictrees.event.handlers.EventHandlers;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.loading.FMLPaths;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

@Mod.EventBusSubscriber(modid = DynamicTrees.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class DTConfigs {

	public static final File CONFIG_DIRECTORY;

	public static final ForgeConfigSpec SERVER_CONFIG;
	public static final ForgeConfigSpec COMMON_CONFIG;
	public static final ForgeConfigSpec CLIENT_CONFIG;
	
	public static final ForgeConfigSpec.DoubleValue SEED_DROP_RATE;
	public static final ForgeConfigSpec.DoubleValue SEED_PLANT_RATE;
	public static final ForgeConfigSpec.IntValue SEED_TIME_TO_LIVE;
	public static final ForgeConfigSpec.BooleanValue SEED_ONLY_FOREST;
	public static final ForgeConfigSpec.DoubleValue SEED_MIN_FORESTNESS;
	
	public static final ForgeConfigSpec.DoubleValue TREE_GROWTH_MULTIPLIER;
	public static final ForgeConfigSpec.DoubleValue TREE_HARVEST_MULTIPLIER;
	public static final ForgeConfigSpec.DoubleValue MAX_TREE_HARDNESS;
	public static final ForgeConfigSpec.IntValue TREE_GROWTH_FOLDING;
	public static final ForgeConfigSpec.BooleanValue DROP_STICKS;
	public static final ForgeConfigSpec.DoubleValue SCALE_BIOME_GROWTH_RATE;
	public static final ForgeConfigSpec.DoubleValue DISEASE_CHANCE;
	public static final ForgeConfigSpec.IntValue MAX_BRANCH_ROT_RADIUS;
	public static final ForgeConfigSpec.DoubleValue ROOTY_BLOCK_HARDNESS_MULTIPLIER;
	public static final ForgeConfigSpec.EnumValue<DynamicTrees.SwampOakWaterState> SWAMP_OAKS_IN_WATER;
	
	public static final ForgeConfigSpec.BooleanValue IS_LEAVES_PASSABLE;
	public static final ForgeConfigSpec.BooleanValue VANILLA_LEAVES_COLLISION;
	public static final ForgeConfigSpec.BooleanValue ENABLE_BRANCH_CLIMBING;
	public static final ForgeConfigSpec.BooleanValue CANOPY_CRASH;
	public static final ForgeConfigSpec.EnumValue<DynamicTrees.AxeDamage> AXE_DAMAGE_MODE;
	public static final ForgeConfigSpec.BooleanValue ENABLE_FALLING_TREES;
	public static final ForgeConfigSpec.BooleanValue ENABLE_FALLING_TREE_DAMAGE;
	public static final ForgeConfigSpec.DoubleValue FALLING_TREE_DAMAGE_MULTIPLIER;
	public static final ForgeConfigSpec.BooleanValue DIRT_BUCKET_PLACES_DIRT;
	public static final ForgeConfigSpec.BooleanValue SLOPPY_BREAK_DROPS;
	public static final ForgeConfigSpec.IntValue MIN_RADIUS_FOR_STRIP;
	public static final ForgeConfigSpec.BooleanValue ENABLE_STRIP_RADIUS_REDUCTION;
	public static final ForgeConfigSpec.BooleanValue CAN_BONE_MEAL_APPLE;
	
	public static final ForgeConfigSpec.BooleanValue REPLACE_VANILLA_SAPLING;
	public static final ForgeConfigSpec.BooleanValue REPLACE_NYLIUM_FUNGI;
	
	public static final ForgeConfigSpec.BooleanValue PODZOL_GEN;

	public static final ForgeConfigSpec.BooleanValue GENERATE_DIRT_BUCKET_RECIPES;

	public static final ForgeConfigSpec.BooleanValue WORLD_GEN;
	public static final ForgeConfigSpec.ConfigValue<List<String>> DIMENSION_BLACKLIST;
	
	//public static final ForgeConfigSpec.BooleanValue fancyThickRings;

	public static final ForgeConfigSpec.BooleanValue WORLD_GEN_DEBUG;

	public static final ForgeConfigSpec.BooleanValue ENABLE_SEASONAL_SEED_DROP_FACTOR;
	public static final ForgeConfigSpec.BooleanValue ENABLE_SEASONAL_GROWTH_FACTOR;
	public static final ForgeConfigSpec.BooleanValue ENABLE_SEASONAL_FRUIT_PRODUCTION_FACTOR;

	static {
		CONFIG_DIRECTORY = new File(FMLPaths.CONFIGDIR.get().toUri());

		final ForgeConfigSpec.Builder SERVER_BUILDER = new ForgeConfigSpec.Builder();
		final ForgeConfigSpec.Builder COMMON_BUILDER = new ForgeConfigSpec.Builder();
		final ForgeConfigSpec.Builder CLIENT_BUILDER = new ForgeConfigSpec.Builder();
		
		SERVER_BUILDER.comment("Seed Settings").push("seeds");
		SEED_DROP_RATE = SERVER_BUILDER.comment("The rate at which seeds voluntarily drop from branches").
				defineInRange("dropRate", 0.01, 0.0, 1.0);
		SEED_PLANT_RATE = SERVER_BUILDER.comment("The rate at which seeds voluntarily plant themselves in their ideal biomes").
				defineInRange("plantRate", 1f/6f, 0.0, 1.0);
		SEED_TIME_TO_LIVE = SERVER_BUILDER.comment("Ticks before a seed in the world attempts to plant itself or despawn. 1200 = 1 minute").
				defineInRange("timeToLive", 1200, 0, 6000);
		SEED_ONLY_FOREST = SERVER_BUILDER.comment("If enabled then seeds will only voluntarily plant themselves in forest-like biomes").
				define("onlyForest", true);
		SEED_MIN_FORESTNESS = SERVER_BUILDER.comment("The minimum forestness that non-forest-like biomes can have. 0 = is not at all a forest, 1 = may as well be a forest. Can be fractional").
				defineInRange("dropRate", 0.0, 0.0, 1.0);
		SERVER_BUILDER.pop();
		
		SERVER_BUILDER.comment("Tree Settings").push("trees");
		TREE_GROWTH_MULTIPLIER = SERVER_BUILDER.comment("Factor that multiplies the rate at which trees grow. Use at own risk").
				defineInRange("growthMultiplier", 0.5f, 0, 16f);
		TREE_HARVEST_MULTIPLIER = SERVER_BUILDER.comment("Factor that multiplies the wood returned from harvesting a tree.  You cheat.").
				defineInRange("harvestMultiplier", 1f, 0f, 128f);
		MAX_TREE_HARDNESS = SERVER_BUILDER.comment("Maximum harvesting hardness that can be calculated. Regardless of tree thickness.").
				defineInRange("maxTreeHardness", 20f, 1f, 200f);
		TREE_GROWTH_FOLDING = SERVER_BUILDER.comment("Do X growth cycles at once while ignoring (X-1)/X attempts.  Higher numbers can improve client side performance but too high can make trees grow wierd.").
				defineInRange("growthFolding", 2, 1, 8);
		DROP_STICKS = SERVER_BUILDER.comment("If enabled then sticks will be dropped for partial logs").
				define("dropSticks", true);
		SCALE_BIOME_GROWTH_RATE = SERVER_BUILDER.comment("Scales the growth for the environment.  0.5f is nominal. 0.0 trees only grow in their native biome. 1.0 trees grow anywhere like they are in their native biome").
				defineInRange("scaleBiomeGrowthRate", 0.5f, 0.0f, 1.0f);
		DISEASE_CHANCE = SERVER_BUILDER.comment("The chance of a tree on depleted soil to die. 1/256(~0.004) averages to about 1 death every 16 minecraft days").
				defineInRange("diseaseChance", 0.0f, 0.0f, 1.0f);
		MAX_BRANCH_ROT_RADIUS = SERVER_BUILDER.comment("The maximum radius of a branch that is allowed to postRot away. 8 = Full block size. Set to 0 to prevent rotting").
				defineInRange("maxBranchRotRadius", 8, 0, 24);
		ROOTY_BLOCK_HARDNESS_MULTIPLIER = SERVER_BUILDER.comment("How much harder it is to destroy a rooty block compared to its non-rooty state").
				defineInRange("rootyBlockHardnessMultiplier", 40f, 0f, 128f);
		SWAMP_OAKS_IN_WATER = SERVER_BUILDER.comment("Options for how oak trees generate in swamps. ROOTED: Swamp oak trees will generate on shallow water with mangrove-like roots. SUNK: Swamp oak trees will generate on shallow water one block under the surface. DISABLED: Swamp oaks will not generate on water.").
				defineEnum("swampOaksInWater", DynamicTrees.SwampOakWaterState.ROOTED);

		SERVER_BUILDER.pop();
		
		SERVER_BUILDER.comment("Interaction Settings").push("interaction");
		IS_LEAVES_PASSABLE = SERVER_BUILDER.comment("If enabled all leaves will be passable").
				define("isLeavesPassable", false);
		VANILLA_LEAVES_COLLISION = SERVER_BUILDER.comment("If enabled player movement on leaves will not be enhanced").
				define("vanillaLeavesCollision", false);
		ENABLE_BRANCH_CLIMBING = SERVER_BUILDER.comment("If enabled then thinner branches can be climbed").
				define("enableBranchClimbling", true);
		CANOPY_CRASH = SERVER_BUILDER.comment("If enabled players receive reduced fall damage on leaves at the expense of the block(s) destruction").
				define("canopyCrash", true);
		AXE_DAMAGE_MODE = SERVER_BUILDER.comment("Damage dealt to the axe item when cutting a tree down. VANILLA: Standard 1 Damage. THICKNESS: By Branch/Trunk Thickness. VOLUME: By Tree Volume.").
				defineEnum("axeDamageMode", DynamicTrees.AxeDamage.THICKNESS);
		ENABLE_FALLING_TREES = SERVER_BUILDER.comment("If enabled then trees will fall over when harvested").
				define("enableFallingTrees", true);
		ENABLE_FALLING_TREE_DAMAGE = SERVER_BUILDER.comment("If enabled then trees will harm living entities when falling").
				define("enableFallingTreeDamage", true);
		FALLING_TREE_DAMAGE_MULTIPLIER = SERVER_BUILDER.comment("Multiplier for damage incurred by a falling tree").
				defineInRange("fallingTreeDamageMultiplier", 1.0, 0.0, 100.0);
		DIRT_BUCKET_PLACES_DIRT = SERVER_BUILDER.comment("If enabled the Dirt Bucket will place a dirt block on right-click").
				define("dirtBucketPlacesDirt", true);
		SLOPPY_BREAK_DROPS = SERVER_BUILDER.comment("If enabled then improperly broken trees(not by an entity) will still drop wood.")
				.define("sloppyBreakDrops", false);
		MIN_RADIUS_FOR_STRIP = SERVER_BUILDER.comment("The minimum radius a branch must have before its able to be stripped. 8 = Full block size. Set to 0 to disable stripping trees").
				defineInRange("minRadiusForStrip", 6, 0, 24);
		ENABLE_STRIP_RADIUS_REDUCTION = SERVER_BUILDER.comment("If enabled, stripping a branch will decrease its radius by one").
				define("enableStripRadiusReduction", true);
		CAN_BONE_MEAL_APPLE = SERVER_BUILDER.comment("If enabled, an apple fruit can be bone mealed.").
				define("canBoneMealApple", false);
		SERVER_BUILDER.pop();
		
		COMMON_BUILDER.comment("Vanilla Trees Settings").push("vanilla");
		REPLACE_VANILLA_SAPLING = COMMON_BUILDER.comment("Right clicking with a vanilla sapling places a dynamic sapling instead.").
				define("replaceVanillaSapling", false);
		REPLACE_NYLIUM_FUNGI = COMMON_BUILDER.comment("Crimson Fungus and Warped Fungus that sprout from nylium will be dynamic instead.").
				define("replaceNyliumFungi", true);
		COMMON_BUILDER.pop();
		
		SERVER_BUILDER.comment("World Generation Settings").push("world");
		PODZOL_GEN = SERVER_BUILDER.comment("Randomly generate podzol under select trees like spruce.").
				define("podzolGen", true);
		SERVER_BUILDER.pop();

		SERVER_BUILDER.comment("Miscellaneous Settings").push("misc");
		GENERATE_DIRT_BUCKET_RECIPES = SERVER_BUILDER.comment("If enabled, dirt bucket recipes will be automatically generated.")
				.define("generateDirtBucketRecipes", true);
		SERVER_BUILDER.pop();
		
		COMMON_BUILDER.comment("World Generation Settings").push("world");
		WORLD_GEN = COMMON_BUILDER.comment("World Generation produces Dynamic Trees instead of Vanilla trees.").
				define("worldGen", true);
		DIMENSION_BLACKLIST = COMMON_BUILDER.comment("Blacklist of dimension registry names for disabling Dynamic Tree worldgen (tree cancellers need to be configured individually for biomes in dynamictrees/tree_canceller.json)").
				define("dimensionsBlacklist", new ArrayList<>());
		COMMON_BUILDER.pop();

		COMMON_BUILDER.comment("Mod Integration Settings").push("integration");
		ENABLE_SEASONAL_SEED_DROP_FACTOR = COMMON_BUILDER.comment("If enabled, seed drop rates will be multiplied based on the current season (requires serene seasons).").
				define("enableSeasonalSeedDropFactor", true);
		ENABLE_SEASONAL_GROWTH_FACTOR = COMMON_BUILDER.comment("If enabled, growth rates will be multiplied based on the current season (requires serene seasons).").
				define("enableSeasonalGrowthFactor", true);
		ENABLE_SEASONAL_FRUIT_PRODUCTION_FACTOR = COMMON_BUILDER.comment("If enabled, fruit production rates will be multiplied based on the current season (requires serene seasons).").
				define("enableSeasonalFruitProductionFactor", true);

		COMMON_BUILDER.pop();

//		CLIENT_BUILDER.comment("Visual Settings").push("visuals");
//		fancyThickRings = CLIENT_BUILDER.comment("Rings of thick trees are rendered using a texture created with an expanded tangram construction technique. Otherwise the ring texture is simply stretched").
//				define("fancyThickRings", true);
//		CLIENT_BUILDER.pop();
		
		SERVER_BUILDER.comment("Debug Settings").push("debug");
		WORLD_GEN_DEBUG = SERVER_BUILDER.comment("Enable to mark tree spawn locations with wool circles.").
				define("debug", false);
		SERVER_BUILDER.pop();
		
		SERVER_CONFIG = SERVER_BUILDER.build();
		COMMON_CONFIG = COMMON_BUILDER.build();
		CLIENT_CONFIG = CLIENT_BUILDER.build();
	}
	
	@SubscribeEvent
	public static void onLoad (final ModConfig.Loading event) {
		EventHandlers.configReload();
	}

	@SubscribeEvent
	public static void onReload (final ModConfig.Reloading event) {
		EventHandlers.configReload();
	}

}
