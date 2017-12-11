package com.ferreusveritas.dynamictrees;

import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;

public class ModConfigs {

	public static float seedDropRate;
	public static float seedPlantRate;
	public static int seedTimeToLive;
	public static float treeGrowthRateMultiplier;
	public static float treeHarvestMultiplier;
	public static boolean ignoreBiomeGrowthRate;
	public static float diseaseChance;
	public static boolean replaceVanillaSapling;
	public static boolean vineGen;
	public static boolean podzolGen;
	public static boolean worldGen;
	public static boolean worldGenDebug;
	
	public static void preInit(FMLPreInitializationEvent event) {
		Configuration config = new Configuration(event.getSuggestedConfigurationFile());
		config.load();

		seedDropRate = config.getFloat("dropRate", "seeds", 1f/2f, 0, 1, "The rate at which seeds voluntarily drop from branches");
		seedPlantRate = config.getFloat("plantRate", "seeds", 1f/16f, 0, 1, "The rate at which seeds plant themselves in their ideal biomes");
		seedTimeToLive = config.getInt("timeToLive", "seeds", 1200, 0, 6000, "Ticks before a seed in the world attempts to plant itself or despawn. 1200 = 1 minute");

		treeGrowthRateMultiplier = config.getFloat("growthRateMultiplier", "trees", 1f, 0, 16f, "Factor that multiplies the rate at which trees grow. Use at own risk");
		treeHarvestMultiplier = config.getFloat("harvestMultiplier", "trees", 1f, 0f, 128f, "Factor that multiplies the wood returned from harvesting a tree.  You cheat.");
		ignoreBiomeGrowthRate = config.getBoolean("ignoreBiomeGrowthRate", "trees", false, "If enabled all trees grow as if they are in their native biome");
		diseaseChance = config.getFloat("diseaseChance", "trees", 0.0f, 0.0f, 1.0f, "The chance of a tree on depleted soil to die. 1/256(~0.004) averages to about 1 death every 16 minecraft days");

		replaceVanillaSapling = config.getBoolean("replaceVanillaSapling", "vanilla", false, "Right clicking with a vanilla sapling places a dynamic sapling instead.");
		
		vineGen = config.getBoolean("vineGen", "world", true, "Randomly generate vines on jungle trees.");
		podzolGen = config.getBoolean("podzolGen", "world", true, "Randomly generate podzol under select trees.");
		worldGen = config.getBoolean("worldGen", "world", false, "Experimental world generation.  Generate Dynamic Trees instead of Vanilla trees.");

		worldGenDebug = config.getBoolean("worldGenDebug", "debug", false, "Enable to mark tree spawn locations with wool circles.");
		
		config.save();
	}
}
