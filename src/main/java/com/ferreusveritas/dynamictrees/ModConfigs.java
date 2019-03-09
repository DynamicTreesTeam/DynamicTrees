package com.ferreusveritas.dynamictrees;


import java.io.File;
import java.util.HashSet;

import com.ferreusveritas.dynamictrees.blocks.BlockBranch.EnumAxeDamage;

import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;

public class ModConfigs {

	public static File configDirectory;
	
	public static float seedDropRate;
	public static float seedPlantRate;
	public static int seedTimeToLive;
	public static boolean seedOnlyForest;
	public static float seedMinForestness;
	
	public static float treeGrowthMultiplier;
	public static float treeHarvestMultiplier;
	public static int treeGrowthFolding;
	public static boolean dropSticks;
	public static float scaleBiomeGrowthRate;
	public static float diseaseChance;
	public static int maxBranchRotRadius;
	public static boolean enableAppleTrees;
	public static boolean enableThickTrees;

	public static boolean isLeavesPassable;
	public static boolean vanillaLeavesCollision;
	public static boolean enableBranchClimbling;
	public static boolean canopyCrash;
	public static EnumAxeDamage axeDamageMode;
	public static boolean enableFallingTrees;
	public static boolean enableFallingTreeDamage;
	public static float fallingTreeDamageMultiplier;
	
	public static boolean replaceVanillaSapling;
	
	public static boolean podzolGen;
	public static boolean worldGen;
	public static boolean vanillaCactusWorldGen;
	public static HashSet<Integer> dimensionBlacklist = new HashSet<Integer>();
	
	public static boolean fancyThickRings;
	public static boolean rootyTextureMimicry;
	
	public static boolean worldGenDebug;
	
	public static void preInit(FMLPreInitializationEvent event) {
		
		configDirectory = event.getModConfigurationDirectory();
		
		Configuration config = new Configuration(event.getSuggestedConfigurationFile());
		config.load();
		
		//Seeds
		seedDropRate = config.getFloat("dropRate", "seeds", 0, 0, 1, "The rate at which seeds voluntarily drop from branches");
		seedPlantRate = config.getFloat("plantRate", "seeds", 1f/8f, 0, 1, "The rate at which seeds voluntarily plant themselves in their ideal biomes");
		seedTimeToLive = config.getInt("timeToLive", "seeds", 1200, 0, 6000, "Ticks before a seed in the world attempts to plant itself or despawn. 1200 = 1 minute");
		seedOnlyForest = config.getBoolean("onlyForest", "seeds", true, "If enabled then seeds will only voluntarily plant themselves in forest-like biomes");
		seedMinForestness = config.getFloat("minForestness", "seeds", 0, 0, 1, "The minimum forestness that non-forest-like biomes can have. 0 = is not at all a forest, 1 = may as well be a forest. Can be fractional");
		
		//Trees
		treeGrowthMultiplier = config.getFloat("growthMultiplier", "trees", 0.5f, 0, 16f, "Factor that multiplies the rate at which trees grow. Use at own risk");
		treeHarvestMultiplier = config.getFloat("harvestMultiplier", "trees", 1f, 0f, 128f, "Factor that multiplies the wood returned from harvesting a tree.  You cheat.");
		treeGrowthFolding = config.getInt("growthFolding", "trees", 2, 1, 8, "Do X growth cycles at once while ignoring (X-1)/X attempts.  Higher numbers can improve client side performance but too high can make trees grow wierd.");
		dropSticks = config.getBoolean("dropSticks", "trees", true, "If enabled then sticks will be dropped for partial logs");
		scaleBiomeGrowthRate = config.getFloat("scaleBiomeGrowthRate", "trees", 0.5f, 0.0f, 1.0f, "Scales the growth for the environment.  0.5f is nominal. 0.0 trees only grow in their native biome. 1.0 trees grow anywhere like they are in their native biome");
		diseaseChance = config.getFloat("diseaseChance", "trees", 0.0f, 0.0f, 1.0f, "The chance of a tree on depleted soil to die. 1/256(~0.004) averages to about 1 death every 16 minecraft days");
		maxBranchRotRadius = config.getInt("maxBranchRotRadius", "trees", 8, 0, 24, "The maximum radius of a branch that is allowed to rot away. 8 = Full block size.  Set to 0 to prevent rotting");
		enableAppleTrees = config.getBoolean("enableAppleTrees", "trees", true, "If enabled apple trees will be generated during worldgen and oak trees will not drop apples");
		enableThickTrees = config.getBoolean("enableThickTrees", "trees", true, "If enabled then certain species trunks will be able to grow wider than a single block");
		
		//Interaction
		isLeavesPassable = config.getBoolean("isLeavesPassable", "interaction", false, "If enabled all leaves will be passable");
		vanillaLeavesCollision = config.getBoolean("vanillaLeavesCollision", "interaction", false, "If enabled player movement on leaves will not be enhanced");
		enableBranchClimbling = config.getBoolean("enableBranchClimbling", "interaction", true, "If enabled then thinner branches can be climbed");
		canopyCrash = config.getBoolean("canopyCrash", "interaction", true, "If enabled players receive reduced fall damage on leaves at the expense of the block(s) destruction");
		axeDamageMode = EnumAxeDamage.values()[config.getInt("axeDamageMode", "interaction", 1, 0, 2, "Modes: 0=Standard 1 Damage, 1=By Branch/Trunk Thickness, 2=By Tree Volume")];
		enableFallingTrees = config.getBoolean("enableFallingTrees", "interaction", true, "If enabled then trees will fall over when harvested");
		enableFallingTreeDamage = config.getBoolean("enableFallingTreeDamage", "interaction", true, "If enabled then trees will harm living entities when falling");
		fallingTreeDamageMultiplier = config.getFloat("fallingTreeDamageMultiplier", "interaction", 1.0f, 0.0f, 100.0f, "Multiplier for damage incurred by a falling tree");
		dirtBucketPlacesDirt = config.getBoolean("dirtBucketPlacesDirt", "interaction", true, "If enabled the Dirt Bucket will place a dirt block on right-click");
		
		//Vanilla
		replaceVanillaSapling = config.getBoolean("replaceVanillaSapling", "vanilla", false, "Right clicking with a vanilla sapling places a dynamic sapling instead.");
		
		//World
		podzolGen = config.getBoolean("podzolGen", "world", true, "Randomly generate podzol under select trees.");
		worldGen = config.getBoolean("worldGen", "world", true, "World Generation produces Dynamic Trees instead of Vanilla trees.");
		vanillaCactusWorldGen = config.getBoolean("vanillaCactusWorldGen", "world", false, "World Generation produces Vanilla cactus as well as Dynamic cactus if world gen replacement is enabled.");
		String[] dims = config.getStringList("dimensionsBlacklist", "world", new String[] {"7"}, "Blacklist of dimension numbers for disabling Dynamic Tree worldgen");
				
		for(String dim : dims) {
			try {
				int dimValue = Integer.decode(dim);
				dimensionBlacklist.add(dimValue);
			} catch (NumberFormatException nfe) {}
		}
		
		//Client
		fancyThickRings = config.getBoolean("fancyThickRings", "client", true, "Rings of thick trees are rendered using a texture created with an expanded tangram construction technique. Otherwise the ring texture is simply stretched");
		rootyTextureMimicry = config.getBoolean("rootyTextureMimicry", "client", true, "Rooty dirt blocks will mimic the texture of adjacent soil blocks to better match the environment. Otherwise will always default to the most generic block");
		
		//Debug
		worldGenDebug = config.getBoolean("worldGenDebug", "debug", false, "Enable to mark tree spawn locations with wool circles.");
		
		config.save();
	}
}
