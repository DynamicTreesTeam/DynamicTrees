package com.ferreusveritas.growingtrees.blocks;

import java.util.Random;

import com.ferreusveritas.growingtrees.TreeHelper;
import com.ferreusveritas.growingtrees.items.Seed;
import com.ferreusveritas.growingtrees.special.BottomListenerPodzol;

import net.minecraft.init.Blocks;
import net.minecraft.util.MathHelper;
import net.minecraft.world.EnumSkyBlock;
import net.minecraft.world.World;
import net.minecraft.world.biome.BiomeGenBase;

public class BlockOakBranch extends BlockBranch {
	
	public BlockOakBranch(){
		tapering = 0.3f;
		signalEnergy = 12.0f;
		growthRate = 0.8f;
		setPrimitiveLog(Blocks.log, 0);
	}
	
	@Override
	public BlockBranch setGrowingLeavesAndSeeds(String name, BlockGrowingLeaves newGrowingLeaves, int sub, Seed seed){
		super.setGrowingLeavesAndSeeds(name, newGrowingLeaves, sub, seed);
		if(this.growingLeaves != null){
			this.growingLeaves.setPrimitiveLeaves(sub, Blocks.leaves, 0).registerBottomSpecials(sub, new BottomListenerPodzol());
		}
		return this;
	}
	
	@Override
	public float biomeSuitability(World world, int x, int y, int z){

		BiomeGenBase biome = world.getBiomeGenForCoords(x, z);

		if(isOneOfBiomes(biome, BiomeGenBase.forest, BiomeGenBase.forestHills)){
			return 1.00f;
		}

		float s = 0.75f;//Suitability(Should be just barely less than the biome suitabilities)
		float temp = biome.getFloatTemperature(x, y, z);
        float rain = biome.rainfall;
        
        s *=
        	temp < 0.30f ? 0.75f ://Excessively Cold
        	temp > 1.00f ? 0.50f ://Excessively Hot
        	1.0f *
        	rain < 0.10f ? 0.25f ://Very Dry(Desert, Savanna, Hell)
        	rain < 0.30f ? 0.50f ://Fairly Dry (Extreme Hills, Taiga)
        	rain > 0.95f ? 0.75f ://Too Humid(Mushroom Island)
        	1.0f;
		
		return MathHelper.clamp_float(s, 0.0f, 1.0f);
	}
	
	@Override
	public boolean rot(World world, int x, int y, int z, int neighborCount, int radius, Random random){
		if(super.rot(world, x, y, z, neighborCount, radius, random)){
			if(radius > 4 && TreeHelper.isRootyDirt(world, x, y - 1, z) && world.getSavedLightValue(EnumSkyBlock.Sky, x, y, z) < 4) {
				world.setBlock(x, y, z, Blocks.red_mushroom);//Change branch to a red mushroom
				world.setBlock(x, y - 1, z, Blocks.dirt, 2, 3);//Change rooty dirt to Podzol
			}
			return true;
		}
		
		return false;
	}
	
}
