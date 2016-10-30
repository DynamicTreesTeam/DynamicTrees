package com.ferreusveritas.growingtrees.blocks;

import com.ferreusveritas.growingtrees.TreeHelper;
import com.ferreusveritas.growingtrees.items.Seed;

import net.minecraft.init.Blocks;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;
import net.minecraft.world.biome.BiomeGenBase;

public class BlockBirchBranch extends BlockBranch {

	public BlockBirchBranch(){
		tapering = 0.1f;
		signalEnergy = 14.0f;
		upProbability = 4;
		lowestBranchHeight = 4;//Birch are tall skinny trees
		retries = 1;//Fast growing
		growthRate = 1.25f;//Fastest growing tree
		setPrimitiveLog(Blocks.log, 2);
	}

	@Override
	public BlockBranch setGrowingLeavesAndSeeds(String name, BlockGrowingLeaves newGrowingLeaves, int sub, Seed seed){
		super.setGrowingLeavesAndSeeds(name, newGrowingLeaves, sub, seed);
		if(growingLeaves != null){
			growingLeaves.setPrimitiveLeaves(sub, Blocks.leaves, 2);
		}
		return this;
	}
	
	@Override
	public float biomeSuitability(World world, int x, int y, int z){
		
		BiomeGenBase biome = world.getBiomeGenForCoords(x, z);

		if(isOneOfBiomes(biome, BiomeGenBase.birchForest, BiomeGenBase.birchForestHills)){
			return 1.00f;
		}

		float s = 0.75f;//Suitability
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
