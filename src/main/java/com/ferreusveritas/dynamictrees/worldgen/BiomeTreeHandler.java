package com.ferreusveritas.dynamictrees.worldgen;

import java.util.Random;

import com.ferreusveritas.dynamictrees.api.TreeRegistry;
import com.ferreusveritas.dynamictrees.trees.DynamicTree;

import net.minecraft.init.Biomes;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.biome.Biome;
import net.minecraftforge.common.BiomeDictionary;
import net.minecraftforge.common.BiomeDictionary.Type;

/**
* Selects a tree appropriate for a biome
* 
* @author ferreusveritas
*/
public class BiomeTreeHandler implements IBiomeDensityProvider, IBiomeTreeSelector {

	DynamicTree oak;
	DynamicTree birch;
	DynamicTree spruce;
	DynamicTree acacia;
	DynamicTree jungle;
	DynamicTree darkoak;

	public BiomeTreeHandler() {
		oak = TreeRegistry.findTree("oak");
		birch = TreeRegistry.findTree("birch");
		spruce = TreeRegistry.findTree("spruce");
		acacia = TreeRegistry.findTree("acacia");
		jungle = TreeRegistry.findTree("jungle");
		darkoak = TreeRegistry.findTree("darkoak");
	}

	@Override
	public DynamicTree getTree(Biome biome) {

		if(BiomeDictionary.hasType(biome, Type.SAVANNA)) {
			return acacia;
		}

		if(BiomeDictionary.hasType(biome, Type.CONIFEROUS)) {
			return spruce;
		}

		if(BiomeDictionary.hasType(biome, Type.JUNGLE)) {
			return jungle;
		}

		if(BiomeDictionary.hasType(biome, Type.SPOOKY)) {
			return darkoak;
		}
		
		if(BiomeDictionary.hasType(biome, Type.WASTELAND)) {
			return null;
		}

		if(BiomeDictionary.hasType(biome, Type.SANDY)) {
			return null;
		}

		if(BiomeDictionary.hasType(biome, Type.FOREST)) {
			if(DynamicTree.isOneOfBiomes(biome, Biomes.BIRCH_FOREST, Biomes.BIRCH_FOREST_HILLS)) {
				return birch;
			} else {
				return oak;
			}
		}

		return oak;
	}

	@Override
	public double getDensity(Biome biome, double noiseDensity, Random random) {

		if(BiomeDictionary.hasType(biome, Type.SPOOKY)) { //Roofed Forest
			if(random.nextInt(4) == 0) {
				return 1.0f;
			}
			if(random.nextInt(8) == 0) {
				return 0.0f;
			}
			return (noiseDensity * 0.25) + 0.25;
		}

		double naturalDensity = MathHelper.clamp((biome.decorator.treesPerChunk) / 10.0f, 0.0f, 1.0f);//Gives 0.0 to 1.0
		return noiseDensity * (naturalDensity * 1.5f);
	} 

	@Override
	public boolean chance(Biome biome, DynamicTree tree, int radius, Random random) {
		
		//Never miss a chance to spawn a tree in the roofed forest.
		if(BiomeDictionary.hasType(biome, Type.SPOOKY)) {//Roofed Forest
			return true;
		}
		
		int chance = 1;
		
		if(radius > 3) {
			chance = (int) (radius / 1.5f);
		}

		return random.nextInt(chance) == 0;
	}

}
