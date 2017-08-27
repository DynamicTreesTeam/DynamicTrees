package com.ferreusveritas.dynamictrees.trees;

import java.util.Random;

import com.ferreusveritas.dynamictrees.TreeHelper;

import net.minecraft.init.Blocks;
import net.minecraft.world.EnumSkyBlock;
import net.minecraft.world.World;
import net.minecraft.world.biome.BiomeGenBase;
import net.minecraftforge.common.BiomeDictionary.Type;

public class TreeBirch extends DynamicTree {

	public TreeBirch(int seq) {
		super("birch", seq);

		//Birch are tall, skinny, fast growing trees
		setBasicGrowingParameters(0.1f, 14.0f, 4, 4, 1.25f);

		retries = 1;//Special fast growing

		//Vanilla Birch Stuff
		setPrimitiveLeaves(Blocks.leaves, 2);
		setPrimitiveLog(Blocks.log, 2);
		setPrimitiveSapling(Blocks.sapling, 2);

		envFactor(Type.COLD, 0.75f);
		envFactor(Type.HOT, 0.50f);
		envFactor(Type.DRY, 0.50f);
		envFactor(Type.FOREST, 1.05f);
	}

	@Override
	public boolean isBiomePerfect(BiomeGenBase biome) {
		return isOneOfBiomes(biome, BiomeGenBase.birchForest, BiomeGenBase.birchForestHills);
	};

	@Override
	public boolean rot(World world, int x, int y, int z, int neighborCount, int radius, Random random) {
		if(super.rot(world, x, y, z, neighborCount, radius, random)) {
			if(radius > 4 && TreeHelper.isRootyDirt(world, x, y - 1, z) && world.getSavedLightValue(EnumSkyBlock.Sky, x, y, z) < 4) {
				world.setBlock(x, y, z, Blocks.brown_mushroom);//Change branch to a brown mushroom
				world.setBlock(x, y - 1, z, Blocks.dirt, 0, 3);//Change rooty dirt to dirt
			}
			return true;
		}

		return false;
	}

	
}
