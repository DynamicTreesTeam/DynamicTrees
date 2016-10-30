package com.ferreusveritas.growingtrees.blocks;

import java.util.Random;

import com.ferreusveritas.growingtrees.GrowingTrees;
import com.ferreusveritas.growingtrees.TreeHelper;
import com.ferreusveritas.growingtrees.entities.EntityParticleLeaf;
import com.ferreusveritas.growingtrees.items.Seed;
import com.ferreusveritas.growingtrees.special.BottomListenerDropItems;
import com.ferreusveritas.growingtrees.special.BottomListenerPodzol;
import com.ferreusveritas.growingtrees.special.BottomListenerVine;

import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.EntityFX;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.util.MathHelper;
import net.minecraft.world.EnumSkyBlock;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraft.world.biome.BiomeGenBase;
import net.minecraftforge.common.util.ForgeDirection;

public class BlockDarkOak extends BlockBranch {

	public BlockDarkOak(){
		tapering = 0.35f;
		signalEnergy = 18.0f;//Dark Oaks are monsters
		upProbability = 6;
		growthRate = 0.8f;
		soilLongevity = 14;//Grows for a long long time
		lowestBranchHeight = 8;
		setPrimitiveLog(Blocks.log2, 1);
	}
	
	@Override
	public BlockBranch setGrowingLeavesAndSeeds(String name, BlockGrowingLeaves newGrowingLeaves, int sub, Seed seed){
		super.setGrowingLeavesAndSeeds(name, newGrowingLeaves, sub, seed);
		if(growingLeaves != null){
			short cellSolver[] = {0x0514, 0x0423, 0x0412, 0x0312, 0x0211};
			short hydroSolver[] = {0x0243, 0x0233, 0x0143, 0x0133};
			growingLeaves.setSmother(sub, 3);//thin canopy
			growingLeaves.setSolvers(sub, cellSolver, hydroSolver);
			growingLeaves.setPrimitiveLeaves(sub, Blocks.leaves2, 1).registerBottomSpecials(sub, new BottomListenerPodzol());
		}
		return this;
	}
	
	@Override
	public int getLowestBranchHeight(World world, int x, int y, int z){
		return (int)(super.getLowestBranchHeight(world, x, y, z) * biomeSuitability(world, x, y, z));
	}
	
	@Override
	public float getEnergy(World world, int x, int y, int z){
		return super.getEnergy(world, x, y, z) * biomeSuitability(world, x, y, z);
	}
	
	@Override
	public float getGrowthRate(World world, int x, int y, int z) {
		return super.getGrowthRate(world, x, y, z) * biomeSuitability(world, x, y, z);
	}
	
	@Override
	public int[] customDirectionManipulation(World world, int x, int y, int z, int radius, GrowSignal signal, int probMap[]){

		if(signal.numTurns >= 1){
			probMap[ForgeDirection.UP.ordinal()] = 0;
			probMap[ForgeDirection.DOWN.ordinal()] = 0;
		}

		//float spreadPush = 1.0f + (float)signal.dy / getEnergy(world, x, y, z) * 4;// 1(bottom) to 4(top)
		
		//Amplify cardinal directions to encourage spread
		float energyRatio = signal.dy / getEnergy(world, x, y, z);
		float spreadPush = energyRatio * energyRatio * energyRatio * 4;
		for(ForgeDirection dir: GrowingTrees.cardinalDirs){
			probMap[dir.ordinal()] *= spreadPush;
		}
		
		return probMap;
	}

	@Override
	public float biomeSuitability(World world, int x, int y, int z){

		BiomeGenBase biome = world.getBiomeGenForCoords(x, z);

		if(isOneOfBiomes(biome, BiomeGenBase.roofedForest)){
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
        	rain > 0.95f ? 1.25f ://Dark Oaks love humid biomes(Mushroom Island)
        	1.0f;
		
		return MathHelper.clamp_float(s, 0.0f, 1.0f);
	}
	
	
	@Override
	public boolean rot(World world, int x, int y, int z, int neighborCount, int radius, Random random){
		if(super.rot(world, x, y, z, neighborCount, radius, random)){
			if(radius > 2 && TreeHelper.isRootyDirt(world, x, y - 1, z) && world.getSavedLightValue(EnumSkyBlock.Sky, x, y, z) < 6) {
				world.setBlock(x, y, z, Blocks.red_mushroom);//Change branch to a red mushroom
				world.setBlock(x, y - 1, z, Blocks.dirt, 2, 3);//Change rooty dirt to Podzol
			}
			return true;
		}
		
		return false;
	}
	
	
	
}
