package com.ferreusveritas.dynamictrees;


import java.util.HashSet;

import com.ferreusveritas.dynamictrees.blocks.BlockBranch.EnumAxeDamage;

import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;

public class ModConfigs {

	public static float seedDropRate;
	public static float seedPlantRate;
	public static int seedTimeToLive;
	
	public static float treeGrowthRateMultiplier;
	public static float treeHarvestMultiplier;
	public static float scaleBiomeGrowthRate;
	public static float diseaseChance;
	public static boolean enableAppleTrees;

	public static boolean isLeavesPassable;
	public static boolean vanillaLeavesCollision;
	public static boolean enableBranchClimbling;
	public static boolean canopyCrash;
	public static EnumAxeDamage axeDamageMode;
	
	public static boolean replaceVanillaSapling;
	
	public static boolean vineGen;
	public static boolean podzolGen;
	public static boolean worldGen;
	public static boolean vanillaCactusWorldGen;
	public static HashSet<Integer> dimensionBlacklist = new HashSet<Integer>();
	
	public static boolean worldGenDebug;
	
	public static boolean poissonDiscImageWrite = false;
	
	public static void preInit(FMLPreInitializationEvent event) {
		Configuration config = new Configuration(event.getSuggestedConfigurationFile());
		config.load();
		
		//Seeds
		seedDropRate = config.getFloat("dropRate", "seeds", 1f/4f, 0, 1, "The rate at which seeds voluntarily drop from branches");
		seedPlantRate = config.getFloat("plantRate", "seeds", 1f/8f, 0, 1, "The rate at which seeds plant themselves in their ideal biomes");
		seedTimeToLive = config.getInt("timeToLive", "seeds", 1200, 0, 6000, "Ticks before a seed in the world attempts to plant itself or despawn. 1200 = 1 minute");
		
		//Trees
		treeGrowthRateMultiplier = config.getFloat("growthRateMultiplier", "trees", 1f, 0, 16f, "Factor that multiplies the rate at which trees grow. Use at own risk");
		treeHarvestMultiplier = config.getFloat("harvestMultiplier", "trees", 1f, 0f, 128f, "Factor that multiplies the wood returned from harvesting a tree.  You cheat.");
		scaleBiomeGrowthRate = config.getFloat("scaleBiomeGrowthRate", "trees", 0.5f, 0.0f, 1.0f, "Scales the growth for the environment.  0.5f is nominal. 0.0 trees only grow in their native biome. 1.0 trees grow anywhere like they are in their native biome");
		diseaseChance = config.getFloat("diseaseChance", "trees", 0.0f, 0.0f, 1.0f, "The chance of a tree on depleted soil to die. 1/256(~0.004) averages to about 1 death every 16 minecraft days");
		enableAppleTrees = config.getBoolean("enableAppleTrees", "trees", true, "If enabled apple trees will be generated during worldgen and oak trees will not drop apples");
		
		//Interaction
		isLeavesPassable = config.getBoolean("isLeavesPassable", "interaction", false, "If enabled all leaves will be passable");
		vanillaLeavesCollision = config.getBoolean("vanillaLeavesCollision", "interaction", false, "If enabled player movement on leaves will not be enhanced");
		enableBranchClimbling = config.getBoolean("enableBranchClimbling", "interaction", true, "If enabled then thinner branches can be climbed");
		canopyCrash = config.getBoolean("canopyCrash", "interaction", true, "If enabled players receive reduced fall damage on leaves at the expense of the block(s) destruction");
		axeDamageMode = EnumAxeDamage.values()[config.getInt("axeDamageMode", "interaction", 1, 0, 2, "Modes: 0=Standard 1 Damage, 1=By Branch/Trunk Thickness, 2=By Tree Volume")];
		
		//Vanilla
		replaceVanillaSapling = config.getBoolean("replaceVanillaSapling", "vanilla", false, "Right clicking with a vanilla sapling places a dynamic sapling instead.");
		
		//World
		vineGen = config.getBoolean("vineGen", "world", true, "Randomly generate vines on jungle trees.");
		podzolGen = config.getBoolean("podzolGen", "world", true, "Randomly generate podzol under select trees.");
		worldGen = config.getBoolean("worldGen", "world", true, "World Generation produces Dynamic Trees instead of Vanilla trees.");
		vanillaCactusWorldGen = config.getBoolean("vanillaCactusWorldGen", "world", false, "World Generation produces Vanilla cactus as well as Dynamic cactus if world gen replacement is enabled.");
		String[] dims = config.getStringList("dimensionsBlacklist", "world", new String[0], "Blacklist of dimension numbers for disabling Dynamic Tree worldgen");
		
		for(String dim : dims) {
			try {
				dimensionBlacklist.add(Integer.decode(dim));
			} catch (NumberFormatException nfe) {}
		}
		
		//Debug
		worldGenDebug = config.getBoolean("worldGenDebug", "debug", false, "Enable to mark tree spawn locations with wool circles.");
		
		config.save();
	}
}
