package com.ferreusveritas.growingtrees.trees;

import com.ferreusveritas.growingtrees.ConfigHandler;
import net.minecraft.init.Blocks;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;
import net.minecraft.world.biome.BiomeGenBase;

public class TreeBirch extends GrowingTree {

	public TreeBirch(int seq) {
		super("birch", seq);
		
		tapering = 0.1f;
		signalEnergy = 14.0f;
		upProbability = 4;
		lowestBranchHeight = 4;//Birch are tall skinny trees
		retries = 1;//Fast growing
		growthRate = 1.25f;//Fastest growing tree

		setPrimitiveLeaves(Blocks.leaves, 2);//Birch
		setPrimitiveLog(Blocks.log, 2);//Birch
		
	}

	@Override
	public float biomeSuitability(World world, int x, int y, int z){
		if(ConfigHandler.ignoreBiomeGrowthRate){
			return 1.0f;
		}
		
		BiomeGenBase biome = world.getBiomeGenForCoords(x, z);

		if(isOneOfBiomes(biome, BiomeGenBase.birchForest, BiomeGenBase.birchForestHills)){
			return 1.00f;
		}

		float s = defaultSuitability();
		float temp = biome.getFloatTemperature(x, y, z);
        float rain = biome.rainfall;
        
        s *=
        	temp < 0.30f ? 0.75f ://Excessively Cold
        	temp > 1.00f ? 0.50f ://Excessively Hot
        	1.0f *
        	rain < 0.10f ? 0.75f ://Very Dry(Desert, Savanna, Hell)
        	rain < 0.30f ? 0.50f ://Fairly Dry (Extreme Hills, Taiga)
        	rain > 0.95f ? 0.75f ://Too Humid(Mushroom Island)
        	1.0f;
		
		return MathHelper.clamp_float(s, 0.0f, 1.0f);
	}
	
}
