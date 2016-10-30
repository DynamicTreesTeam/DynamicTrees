package com.ferreusveritas.growingtrees.blocks;

import com.ferreusveritas.growingtrees.GrowingTrees;
import com.ferreusveritas.growingtrees.TreeHelper;
import com.ferreusveritas.growingtrees.items.Seed;
import com.ferreusveritas.growingtrees.special.BottomListenerPodzol;
import com.ferreusveritas.growingtrees.special.BottomListenerVine;

import net.minecraft.init.Blocks;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;
import net.minecraft.world.biome.BiomeGenBase;
import net.minecraftforge.common.util.ForgeDirection;

public class BlockJungleBranch extends BlockBranch {

	public BlockJungleBranch(){
		tapering = 0.15f;
		signalEnergy = 16.0f;
		upProbability = 3;//Jungle tree grows around wildly
		lowestBranchHeight = 2;//A little lower than normal to provide inconvenient obstruction and climbing
		retries = 2;//Very fast growing
		soilLongevity = 10;//Lasts a bit longer than average(8)
		setPrimitiveLog(Blocks.log, 3);
	}
	
	@Override
	public BlockBranch setGrowingLeavesAndSeeds(String name, BlockGrowingLeaves newGrowingLeaves, int sub, Seed seed){
		super.setGrowingLeavesAndSeeds(name, newGrowingLeaves, sub, seed);
		if(growingLeaves != null){
			growingLeaves.setSmother(sub, 3);//thin canopy
			growingLeaves.setPrimitiveLeaves(sub, Blocks.leaves, 3).registerBottomSpecials(sub, new BottomListenerPodzol(), new BottomListenerVine());
		}
		return this;
	}
	
	@Override
	public int getReinfTravel(){
		return 1;
	}

	@Override
	public int[] customDirectionManipulation(World world, int x, int y, int z, int radius, GrowSignal signal, int probMap[]){
		//Jungle Trees grow a maximum of 16 meters tall
		
		//Amplify cardinal directions to encourage spread
		float spreadPush = 1.0f + (float)signal.dy / getEnergy(world, x, y, z) * 6;// 1(bottom) to 6(top)
		
		for(ForgeDirection dir: GrowingTrees.cardinalDirs){
			probMap[dir.ordinal()] *= spreadPush;
		}
		
		//Contort up direction based on distance from ground
		int up = ForgeDirection.UP.ordinal();
		int down = ForgeDirection.DOWN.ordinal();
		
		probMap[up] *= MathHelper.clamp_int((int)(biomeSuitability(world, x, y, z) * 4) - (signal.dy / 4), 1, 4);//4(bottom) - 1(top)

		//Pull down the farther cardinally we are from the base
		int dist = (int)Math.sqrt(signal.dx * signal.dx + signal.dz * signal.dz);
		probMap[down] *= MathHelper.clamp_int(dist / 4, 1, 4);
		
		return probMap;
	}
	
	//Jungle trees grow taller in suitable biomes
	@Override
	public float getEnergy(World world, int x, int y, int z){
        return super.getEnergy(world, x, y, z) * biomeSuitability(world, x, y, z);
	}
	
	@Override
	public float biomeSuitability(World world, int x, int y, int z){
		
		BiomeGenBase biome = world.getBiomeGenForCoords(x, z);

		if(isOneOfBiomes(biome, BiomeGenBase.jungle, BiomeGenBase.jungleHills, BiomeGenBase.jungleEdge)){
			return 1.00f;
		}

		float s = 0.50f;
		float temp = biome.getFloatTemperature(x, y, z);
        float rain = biome.rainfall;
        
        s *=
        	temp < 0.30f ? 0.25f ://Excessively Cold
        	temp < 0.50f ? 0.50f ://Fairly Cold
        	temp > 0.85f ? 1.25f ://Nice and warm 
        	temp > 1.30f ? 0.75f ://Excessively Hot(Hell)
        	1.0f *
        	rain < 0.10f ? 0.25f ://Very Dry(Desert, Savanna, Hell)
        	rain < 0.30f ? 0.50f ://Fairly Dry (Extreme Hills, Taiga)
        	rain > 0.85f ? 1.50f ://Humid(Jungle, Mushroom Island)
        	1.0f;
        	
		return MathHelper.clamp_float(s, 0.0f, 1.0f);
	}

}
