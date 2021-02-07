package com.ferreusveritas.dynamictrees.init;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import com.ferreusveritas.dynamictrees.DynamicTrees;

import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.loading.FMLPaths;

@Mod.EventBusSubscriber
public class DTConfigs {

	public static File configDirectory;

	public static ForgeConfigSpec SERVER_CONFIG;
	public static ForgeConfigSpec COMMON_CONFIG;
	public static ForgeConfigSpec CLIENT_CONFIG;
	
	public static ForgeConfigSpec.DoubleValue seedDropRate;
	public static ForgeConfigSpec.DoubleValue seedPlantRate;
	public static ForgeConfigSpec.IntValue seedTimeToLive;
	public static ForgeConfigSpec.BooleanValue seedOnlyForest;
	public static ForgeConfigSpec.DoubleValue seedMinForestness;
	
	public static ForgeConfigSpec.DoubleValue treeGrowthMultiplier;
	public static ForgeConfigSpec.DoubleValue treeHarvestMultiplier;
	public static ForgeConfigSpec.DoubleValue maxTreeHardness;
	public static ForgeConfigSpec.IntValue treeGrowthFolding;
	public static ForgeConfigSpec.BooleanValue dropSticks;
	public static ForgeConfigSpec.DoubleValue scaleBiomeGrowthRate;
	public static ForgeConfigSpec.DoubleValue diseaseChance;
	public static ForgeConfigSpec.IntValue maxBranchRotRadius;
	public static ForgeConfigSpec.BooleanValue enableAppleTrees;
	public static ForgeConfigSpec.BooleanValue enableThickTrees;
	public static ForgeConfigSpec.DoubleValue rootyBlockHardnessMultiplier;
	
	public static ForgeConfigSpec.BooleanValue isLeavesPassable;
	public static ForgeConfigSpec.BooleanValue vanillaLeavesCollision;
	public static ForgeConfigSpec.BooleanValue enableBranchClimbling;
	public static ForgeConfigSpec.BooleanValue canopyCrash;
	public static ForgeConfigSpec.EnumValue<DynamicTrees.EnumAxeDamage> axeDamageMode;
	public static ForgeConfigSpec.BooleanValue enableFallingTrees;
	public static ForgeConfigSpec.BooleanValue enableFallingTreeDamage;
	public static ForgeConfigSpec.DoubleValue fallingTreeDamageMultiplier;
	public static ForgeConfigSpec.BooleanValue dirtBucketPlacesDirt;
	public static ForgeConfigSpec.BooleanValue sloppyBreakDrops;
	public static ForgeConfigSpec.IntValue minRadiusForStrip;
	public static ForgeConfigSpec.BooleanValue enableStripRadiusReduction;
	
	public static ForgeConfigSpec.BooleanValue replaceVanillaSapling;
	public static ForgeConfigSpec.BooleanValue replaceNyliumFungi;
	
	public static ForgeConfigSpec.BooleanValue podzolGen;
	public static ForgeConfigSpec.BooleanValue worldGen;
	public static ForgeConfigSpec.BooleanValue vanillaCactusWorldGen;
	public static ForgeConfigSpec.ConfigValue<List<String>> dimensionBlackList;
	
	//public static ForgeConfigSpec.BooleanValue fancyThickRings;

	public static ForgeConfigSpec.BooleanValue worldGenDebug;

	public static ForgeConfigSpec.BooleanValue enableSeasonalSeedDropFactor;
	public static ForgeConfigSpec.BooleanValue enableSeasonalGrowthFactor;
	public static ForgeConfigSpec.BooleanValue enableSeasonalFruitProductionFactor;

	static {
		configDirectory = new File(FMLPaths.CONFIGDIR.get().toUri());

		final ForgeConfigSpec.Builder SERVER_BUILDER = new ForgeConfigSpec.Builder();
		final ForgeConfigSpec.Builder COMMON_BUILDER = new ForgeConfigSpec.Builder();
		final ForgeConfigSpec.Builder CLIENT_BUILDER = new ForgeConfigSpec.Builder();
		
		SERVER_BUILDER.comment("Seed settings").push("seeds");
		seedDropRate = SERVER_BUILDER.comment("The rate at which seeds voluntarily drop from branches").
				defineInRange("dropRate", 0.01, 0.0, 1.0);
		seedPlantRate = SERVER_BUILDER.comment("The rate at which seeds voluntarily plant themselves in their ideal biomes").
				defineInRange("plantRate", 1f/6f, 0.0, 1.0);
		seedTimeToLive = SERVER_BUILDER.comment("Ticks before a seed in the world attempts to plant itself or despawn. 1200 = 1 minute").
				defineInRange("timeToLive", 1200, 0, 6000);
		seedOnlyForest = SERVER_BUILDER.comment("If enabled then seeds will only voluntarily plant themselves in forest-like biomes").
				define("onlyForest", true);
		seedMinForestness = SERVER_BUILDER.comment("The minimum forestness that non-forest-like biomes can have. 0 = is not at all a forest, 1 = may as well be a forest. Can be fractional").
				defineInRange("dropRate", 0.0, 0.0, 1.0);
		SERVER_BUILDER.pop();
		
		SERVER_BUILDER.comment("Tree settings").push("trees");
		treeGrowthMultiplier = SERVER_BUILDER.comment("Factor that multiplies the rate at which trees grow. Use at own risk").
				defineInRange("growthMultiplier", 0.5f, 0, 16f);
		treeHarvestMultiplier = SERVER_BUILDER.comment("Factor that multiplies the wood returned from harvesting a tree.  You cheat.").
				defineInRange("harvestMultiplier", 1f, 0f, 128f);
		maxTreeHardness = SERVER_BUILDER.comment("Maximum harvesting hardness that can be calculated. Regardless of tree thickness.").
				defineInRange("maxTreeHardness", 20f, 1f, 200f);
		treeGrowthFolding = SERVER_BUILDER.comment("Do X growth cycles at once while ignoring (X-1)/X attempts.  Higher numbers can improve client side performance but too high can make trees grow wierd.").
				defineInRange("growthFolding", 2, 1, 8);
		dropSticks = SERVER_BUILDER.comment("If enabled then sticks will be dropped for partial logs").
				define("dropSticks", true);
		scaleBiomeGrowthRate = SERVER_BUILDER.comment("Scales the growth for the environment.  0.5f is nominal. 0.0 trees only grow in their native biome. 1.0 trees grow anywhere like they are in their native biome").
				defineInRange("scaleBiomeGrowthRate", 0.5f, 0.0f, 1.0f);
		diseaseChance = SERVER_BUILDER.comment("The chance of a tree on depleted soil to die. 1/256(~0.004) averages to about 1 death every 16 minecraft days").
				defineInRange("diseaseChance", 0.0f, 0.0f, 1.0f);
		maxBranchRotRadius = SERVER_BUILDER.comment("The maximum radius of a branch that is allowed to rot away. 8 = Full block size. Set to 0 to prevent rotting").
				defineInRange("maxBranchRotRadius", 8, 0, 24);
		enableAppleTrees = SERVER_BUILDER.comment("If enabled apple trees will be generated during worldgen and oak trees will not drop apples").
				define("enableAppleTrees", true);
		enableThickTrees = SERVER_BUILDER.comment("If enabled apple trees will be generated during worldgen and oak trees will not drop apples").
				define("enableThickTrees", true);
		rootyBlockHardnessMultiplier = SERVER_BUILDER.comment("How much harder it is to destroy a rooty block compared to its non-rooty state").
				defineInRange("rootyBlockHardnessMultiplier", 40f, 0f, 128f);
		SERVER_BUILDER.pop();
		
		SERVER_BUILDER.comment("Interaction between player and Dynamic Trees content").push("interaction");
		isLeavesPassable = SERVER_BUILDER.comment("If enabled all leaves will be passable").
				define("isLeavesPassable", false);
		vanillaLeavesCollision = SERVER_BUILDER.comment("If enabled player movement on leaves will not be enhanced").
				define("vanillaLeavesCollision", false);
		enableBranchClimbling = SERVER_BUILDER.comment("If enabled then thinner branches can be climbed").
				define("enableBranchClimbling", true);
		canopyCrash = SERVER_BUILDER.comment("If enabled players receive reduced fall damage on leaves at the expense of the block(s) destruction").
				define("canopyCrash", true);
		axeDamageMode = SERVER_BUILDER.comment("Modes: 0=Standard 1 Damage, 1=By Branch/Trunk Thickness, 2=By Tree Volume").
				defineEnum("axeDamageMode", DynamicTrees.EnumAxeDamage.THICKNESS);
		enableFallingTrees = SERVER_BUILDER.comment("If enabled then trees will fall over when harvested").
				define("enableFallingTrees", true);
		enableFallingTreeDamage = SERVER_BUILDER.comment("If enabled then trees will harm living entities when falling").
				define("enableFallingTreeDamage", true);
		fallingTreeDamageMultiplier = SERVER_BUILDER.comment("Multiplier for damage incurred by a falling tree").
				defineInRange("fallingTreeDamageMultiplier", 1.0, 0.0, 100.0);
		dirtBucketPlacesDirt = SERVER_BUILDER.comment("If enabled the Dirt Bucket will place a dirt block on right-click").
				define("dirtBucketPlacesDirt", true);
		sloppyBreakDrops = SERVER_BUILDER.comment("If enabled then improperly broken trees(not by an entity) will still drop wood.")
				.define("sloppyBreakDrops", false);
		minRadiusForStrip = SERVER_BUILDER.comment("The minimum radius a branch must have before its able to be stripped. 8 = Full block size. Set to 0 to disable stripping trees").
				defineInRange("minRadiusForStrip", 6, 0, 24);
		enableStripRadiusReduction = SERVER_BUILDER.comment("If enabled stripping a branch will decrease its radius by one").
				define("enableStripRadiusReduction", true);
		SERVER_BUILDER.pop();
		
		SERVER_BUILDER.comment("Settings regarding vanilla trees").push("vanilla");
		replaceVanillaSapling = SERVER_BUILDER.comment("Right clicking with a vanilla sapling places a dynamic sapling instead.").
				define("replaceVanillaSapling", false);
		replaceNyliumFungi = SERVER_BUILDER.comment("Fungi that sprout from bonemealing nylium will be dynamic instead.").
				define("replaceNyliumFungi", false);
		SERVER_BUILDER.pop();
		
		SERVER_BUILDER.comment("World settings").push("world");
		podzolGen = SERVER_BUILDER.comment("Randomly generate podzol under select trees like spruce.").
				define("podzolGen", true);
		vanillaCactusWorldGen = SERVER_BUILDER.comment("World Generation produces Vanilla cactus as well as Dynamic cactus if world gen replacement is enabled.").
				define("vanillaCactusWorldGen",false);
		
		SERVER_BUILDER.pop();
		
		COMMON_BUILDER.comment("World settings").push("world");
		worldGen = COMMON_BUILDER.comment("World Generation produces Dynamic Trees instead of Vanilla trees.").
				define("worldGen", true);
		dimensionBlackList = COMMON_BUILDER.comment("Blacklist of dimension registry names for disabling Dynamic Tree worldgen (tree cancellers need to be configured individually for biomes in dynamictrees/tree_canceller.json)").
				define("dimensionsBlacklist", new ArrayList<>());
		
		COMMON_BUILDER.pop();

		COMMON_BUILDER.comment("Mod Integration Settings").push("integration");
		enableSeasonalSeedDropFactor = COMMON_BUILDER.comment("If enabled, seed drop rates will be multiplied based on the current season (requires serene seasons).").
				define("enableSeasonalSeedDropFactor", true);
		enableSeasonalGrowthFactor = COMMON_BUILDER.comment("If enabled, growth rates will be multiplied based on the current season (requires serene seasons).").
				define("enableSeasonalGrowthFactor", true);
		enableSeasonalFruitProductionFactor = COMMON_BUILDER.comment("If enabled, fruit production rates will be multiplied based on the current season (requires serene seasons).").
				define("enableSeasonalFruitProductionFactor", true);

		COMMON_BUILDER.pop();

//		CLIENT_BUILDER.comment("Visual clientside settings").push("client");
//		fancyThickRings = CLIENT_BUILDER.comment("Rings of thick trees are rendered using a texture created with an expanded tangram construction technique. Otherwise the ring texture is simply stretched").
//				define("fancyThickRings", true);
//		CLIENT_BUILDER.pop();
		
		SERVER_BUILDER.comment("Debug settings for development").push("debug");
		worldGenDebug = SERVER_BUILDER.comment("Enable to mark tree spawn locations with wool circles.").
				define("debug", false);
		SERVER_BUILDER.pop();
		
		SERVER_CONFIG = SERVER_BUILDER.build();
		COMMON_CONFIG = COMMON_BUILDER.build();
		CLIENT_CONFIG = CLIENT_BUILDER.build();
	}
	
	@SubscribeEvent
	public static void onLoad (final ModConfig.Loading event) { }

}
