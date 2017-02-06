package com.ferreusveritas.growingtrees.trees;

import net.minecraft.init.Blocks;
import net.minecraft.world.biome.BiomeGenBase;
import net.minecraftforge.common.BiomeDictionary.Type;

public class TreeBirch extends GrowingTree {

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
	
}
