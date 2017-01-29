package com.ferreusveritas.growingtrees.trees;

import com.ferreusveritas.growingtrees.ConfigHandler;
import com.ferreusveritas.growingtrees.blocks.BlockBranch;
import com.ferreusveritas.growingtrees.blocks.BlockGrowingLeaves;

import net.minecraft.init.Blocks;
import net.minecraft.util.MathHelper;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraft.world.biome.BiomeGenBase;
import net.minecraftforge.common.util.ForgeDirection;

public class TreeAcacia extends GrowingTree {

	public TreeAcacia(int seq) {
		super("acacia", seq);
		
		tapering = 0.15f;
		signalEnergy = 12.0f;
		upProbability = 0;
		lowestBranchHeight = 3;
		growthRate = 0.7f;

		setPrimitiveLeaves(Blocks.leaves2, 0);//Vanilla Acacia leaves
		setPrimitiveLog(Blocks.log2, 0);

		cellSolution = new short[]{0x0514, 0x0423, 0x0412, 0x0312, 0x0211};
		hydroSolution = new short[]{0x02F0, 0x0143, 0x0133, 0x01F0};
		smotherLeavesMax = 2;//very thin canopy
	}

	@Override
	public int getBranchHydrationLevel(IBlockAccess blockAccess, int x, int y, int z, ForgeDirection dir, BlockBranch branch, BlockGrowingLeaves fromBlock, int fromSub) {
		if(branch.getRadius(blockAccess, x, y, z) == 1 && isCompatibleGrowingLeaves(fromBlock, fromSub)){//Only compatible leaves
			if(dir == ForgeDirection.DOWN){
				return 3;
			} else
			if(dir != ForgeDirection.UP){//Disallow hydration from above.
				return 5;
			}
		}

		return 0;
	}
	
	@Override
	public float biomeSuitability(World world, int x, int y, int z){
		if(ConfigHandler.ignoreBiomeGrowthRate){
			return 1.0f;
		}
		
		//Acacia is a hardy species resistant to dry hot areas.
		
		BiomeGenBase biome = world.getBiomeGenForCoords(x, z);

		if(isOneOfBiomes(biome, BiomeGenBase.savanna, BiomeGenBase.savannaPlateau)){
			return 1.00f;
		}

		float s = defaultSuitability();
		float temp = biome.getFloatTemperature(x, y, z);
        float rain = biome.rainfall;
        
        s *=
            temp < 0.30f ? 0.25f ://Excessively Cold
            temp < 0.50f ? 0.75f ://Fairly Cold
        	temp > 1.30f ? 0.75f ://Excessively Hot
        	1.0f *
        	rain > 0.50f ? 0.75f ://Too humid
        	1.0f;
		
		return MathHelper.clamp_float(s, 0.0f, 1.0f);
	}
	
}
