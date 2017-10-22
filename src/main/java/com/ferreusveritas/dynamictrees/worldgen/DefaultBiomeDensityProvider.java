package com.ferreusveritas.dynamictrees.worldgen;

import java.util.Random;

import com.ferreusveritas.dynamictrees.api.worldgen.IBiomeDensityProvider;
import com.ferreusveritas.dynamictrees.trees.DynamicTree;

import net.minecraft.util.math.MathHelper;
import net.minecraft.world.biome.Biome;
import net.minecraftforge.common.BiomeDictionary;
import net.minecraftforge.common.BiomeDictionary.Type;

public class DefaultBiomeDensityProvider implements IBiomeDensityProvider {

	@Override
	public String getName() {
		return "default";
	}
	
	@Override
	public int getPriority() {
		return 0;
	}

	@Override
	public double getDensity(Biome biome, double noiseDensity, Random random) {
		
		if(BiomeDictionary.isBiomeOfType(biome, Type.SPOOKY)) { //Roofed Forest
			if(random.nextInt(4) == 0) {
				return 1.0f;
			}
			if(random.nextInt(8) == 0) {
				return 0.0f;
			}
			return (noiseDensity * 0.25) + 0.25;
		}
		
		double naturalDensity = MathHelper.clamp_float((biome.theBiomeDecorator.treesPerChunk) / 10.0f, 0.0f, 1.0f);//Gives 0.0 to 1.0
		return noiseDensity * (naturalDensity * 1.5f);
	}
	
	@Override
	public EnumChance chance(Biome biome, DynamicTree tree, int radius, Random random) {
		
		//Never miss a chance to spawn a tree in the roofed forest.
		if(BiomeDictionary.isBiomeOfType(biome, Type.SPOOKY)) {//Roofed Forest
			return EnumChance.OK;
		}
		
		int chance = 1;//"1" always returns OK
		
		if(radius > 3) {//Start dropping tree spawn opportunities when the radius gets bigger than 3
			chance = (int) (radius / 1.5f);
		}
		
		//the last call should never be UNHANDLED
		return random.nextInt(chance) == 0 ? EnumChance.OK : EnumChance.CANCEL;
	}
	
}
