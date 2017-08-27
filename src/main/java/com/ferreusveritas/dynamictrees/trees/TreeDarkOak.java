package com.ferreusveritas.dynamictrees.trees;

import java.util.ArrayList;
import java.util.Random;

import com.ferreusveritas.dynamictrees.DynamicTrees;
import com.ferreusveritas.dynamictrees.TreeHelper;
import com.ferreusveritas.dynamictrees.blocks.GrowSignal;
import com.ferreusveritas.dynamictrees.special.BottomListenerPodzol;
import com.ferreusveritas.dynamictrees.util.SimpleVoxmap;
import com.ferreusveritas.dynamictrees.util.Vec3d;

import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.world.EnumSkyBlock;
import net.minecraft.world.World;
import net.minecraft.world.biome.BiomeGenBase;
import net.minecraftforge.common.BiomeDictionary.Type;
import net.minecraftforge.common.util.ForgeDirection;

public class TreeDarkOak extends DynamicTree {

	public TreeDarkOak(int seq) {
		super("darkoak", seq);

		//Dark Oak Trees are tall, slowly growing, thick trees
		setBasicGrowingParameters(0.35f, 18.0f, 6, 8, 0.8f);

		soilLongevity = 14;//Grows for a long long time

		setPrimitiveLeaves(Blocks.leaves2, 1);
		setPrimitiveLog(Blocks.log2, 1);
		setPrimitiveSapling(Blocks.sapling, 5);

		envFactor(Type.COLD, 0.75f);
		envFactor(Type.HOT, 0.50f);
		envFactor(Type.DRY, 0.25f);
		envFactor(Type.MUSHROOM, 1.25f);

		smotherLeavesMax = 3;//thin canopy
		cellSolution = new short[] {0x0514, 0x0423, 0x0412, 0x0312, 0x0211};
		hydroSolution = new short[] {0x0243, 0x0233, 0x0143, 0x0133};

		registerBottomSpecials(new BottomListenerPodzol());
	}

	@Override
	public int getLowestBranchHeight(World world, int x, int y, int z) {
		return (int)(super.getLowestBranchHeight(world, x, y, z) * biomeSuitability(world, x, y, z));
	}

	@Override
	public float getEnergy(World world, int x, int y, int z) {
		return super.getEnergy(world, x, y, z) * biomeSuitability(world, x, y, z);
	}

	@Override
	public float getGrowthRate(World world, int x, int y, int z) {
		return super.getGrowthRate(world, x, y, z) * biomeSuitability(world, x, y, z);
	}

	@Override
	protected int[] customDirectionManipulation(World world, int x, int y, int z, int radius, GrowSignal signal, int probMap[]) {

		if(signal.numTurns >= 1) {
			probMap[ForgeDirection.UP.ordinal()] = 0;
			probMap[ForgeDirection.DOWN.ordinal()] = 0;
		}

		//Amplify cardinal directions to encourage spread(this algorithm is wacked-out poo brain)
		float energyRatio = signal.dy / getEnergy(world, x, y, z);
		float spreadPush = energyRatio * energyRatio * energyRatio * 4;
		spreadPush = spreadPush < 1.0f ? 1.0f : spreadPush;
		
		for(ForgeDirection dir: DynamicTrees.horizontalDirs) {
			probMap[dir.ordinal()] *= spreadPush;
		}

		return probMap;
	}

	@Override
	public boolean isBiomePerfect(BiomeGenBase biome) {
		return isOneOfBiomes(biome, BiomeGenBase.roofedForest);
	};

	@Override
	public boolean rot(World world, int x, int y, int z, int neighborCount, int radius, Random random) {
		if(super.rot(world, x, y, z, neighborCount, radius, random)) {
			if(radius > 2 && TreeHelper.isRootyDirt(world, x, y - 1, z) && world.getSavedLightValue(EnumSkyBlock.Sky, x, y, z) < 6) {
				world.setBlock(x, y, z, Blocks.red_mushroom);//Change branch to a red mushroom
				world.setBlock(x, y - 1, z, Blocks.dirt, 2, 3);//Change rooty dirt to Podzol
			}
			return true;
		}
		
		return false;
	}

	@Override
	public ArrayList<ItemStack> getDrops(World world, int x, int y, int z, int chance, ArrayList<ItemStack> drops) {
		if ((world.rand.nextInt(chance) == 0)) {
			drops.add(new ItemStack(Items.apple, 1, 0));
		}
		return drops;
	}

	@Override
	public void createLeafCluster(){

		leafCluster = new SimpleVoxmap(7, 5, 7, new byte[] {

			//Layer 0(Bottom)
			0, 0, 0, 0, 0, 0, 0,
			0, 0, 2, 2, 2, 0, 0,
			0, 2, 0, 0, 0, 2, 0,
			0, 2, 0, 0, 0, 2, 0,
			0, 2, 0, 0, 0, 2, 0,
			0, 0, 2, 2, 2, 0, 0,
			0, 0, 0, 0, 0, 0, 0,

			//Layer 1
			0, 0, 1, 1, 1, 0, 0,
			0, 1, 2, 2, 2, 1, 0,
			1, 2, 3, 4, 3, 2, 1,
			1, 2, 4, 0, 4, 2, 1,
			1, 2, 3, 4, 3, 2, 1,
			0, 1, 2, 2, 2, 1, 0,
			0, 0, 1, 1, 1, 0, 0,

			//Layer 2
			0, 0, 0, 0, 0, 0, 0,
			0, 0, 1, 1, 1, 0, 0,
			0, 1, 2, 2, 2, 1, 0,
			0, 1, 2, 4, 2, 1, 0,
			0, 1, 2, 2, 2, 1, 0,
			0, 0, 1, 1, 1, 0, 0,
			0, 0, 0, 0, 0, 0, 0,

			//Layer 3
			0, 0, 0, 0, 0, 0, 0,
			0, 0, 0, 0, 0, 0, 0,
			0, 0, 1, 1, 1, 0, 0,
			0, 0, 1, 2, 1, 0, 0,
			0, 0, 1, 1, 1, 0, 0,
			0, 0, 0, 0, 0, 0, 0,
			0, 0, 0, 0, 0, 0, 0,

			//Layer 4 (Top)
			0, 0, 0, 0, 0, 0, 0,
			0, 0, 0, 0, 0, 0, 0,
			0, 0, 0, 0, 0, 0, 0,
			0, 0, 0, 1, 0, 0, 0,
			0, 0, 0, 0, 0, 0, 0,
			0, 0, 0, 0, 0, 0, 0,
			0, 0, 0, 0, 0, 0, 0

		}).setCenter(new Vec3d(3, 1, 3));
	}
}
