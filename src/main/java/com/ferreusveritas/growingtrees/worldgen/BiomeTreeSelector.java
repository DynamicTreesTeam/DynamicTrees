package com.ferreusveritas.growingtrees.worldgen;

import com.ferreusveritas.growingtrees.TreeRegistry;
import com.ferreusveritas.growingtrees.trees.GrowingTree;

import net.minecraft.world.biome.BiomeGenBase;
import net.minecraftforge.common.BiomeDictionary;
import net.minecraftforge.common.BiomeDictionary.Type;

/**
* Selects a tree appropriate for a biome
* 
* @author ferreusveritas
*/
public class BiomeTreeSelector {

	GrowingTree oak;
	GrowingTree birch;
	GrowingTree spruce;
	GrowingTree acacia;
	GrowingTree jungle;

	public BiomeTreeSelector() {
		oak = TreeRegistry.findTree("oak");
		birch = TreeRegistry.findTree("birch");
		spruce = TreeRegistry.findTree("spruce");
		acacia = TreeRegistry.findTree("acacia");
		jungle = TreeRegistry.findTree("jungle");
	}

	public GrowingTree select(BiomeGenBase biome) {

		//TODO: Make override for other mods

		if(BiomeDictionary.isBiomeOfType(biome, Type.SAVANNA)) {
			return acacia;
		}

		if(BiomeDictionary.isBiomeOfType(biome, Type.CONIFEROUS)) {
			return spruce;
		}

		if(BiomeDictionary.isBiomeOfType(biome, Type.JUNGLE)) {
			return jungle;
		}

		if(BiomeDictionary.isBiomeOfType(biome, Type.WASTELAND)) {
			return null;
		}

		if(BiomeDictionary.isBiomeOfType(biome, Type.SANDY)) {
			return null;
		}

		if(BiomeDictionary.isBiomeOfType(biome, Type.FOREST)) {
			if(GrowingTree.isOneOfBiomes(biome, BiomeGenBase.birchForest, BiomeGenBase.birchForestHills)) {
				return birch;
			} else {
				return oak;
			}
		}

		return oak;
	}

}
