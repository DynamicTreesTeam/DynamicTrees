package com.ferreusveritas.dynamictrees.worldgen;

import java.util.HashMap;
import java.util.Random;

import com.ferreusveritas.dynamictrees.api.worldgen.IBiomeDensityProvider;
import com.ferreusveritas.dynamictrees.trees.DynamicTree;

import net.minecraft.util.math.MathHelper;
import net.minecraft.world.biome.Biome;
import net.minecraftforge.common.BiomeDictionary;
import net.minecraftforge.common.BiomeDictionary.Type;

public class DefaultBiomeDensityProvider implements IBiomeDensityProvider {

	private interface IChanceCalc {
		float getChance(int radius);
	}

	private class ChanceCalcStatic implements IChanceCalc {
		float value;
		
		public ChanceCalcStatic(float value) {
			this.value = value;
		}
		
		@Override
		public float getChance(int radius) {
			return value;
		}
	}
	
	private class ChanceCalcByRadius implements IChanceCalc {
		@Override
		public float getChance(int radius) {
			if(radius > 3) {//Start dropping tree spawn opportunities when the radius gets bigger than 3
				return 2.0f / radius;
			}
			return 1.0f;
		}
	}
		
	HashMap<Integer, IChanceCalc> fastChanceLookup = new HashMap<Integer, IChanceCalc>();
	
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

		int biomeId = Biome.getIdForBiome(biome);
		IChanceCalc chanceCalc;
		
		if(fastChanceLookup.containsKey(biomeId)) {
			chanceCalc = fastChanceLookup.get(biomeId);
		} else {
			if(BiomeDictionary.isBiomeOfType(biome, Type.FOREST)) {//Never miss a chance to spawn a tree in a forest.
				chanceCalc = new ChanceCalcStatic(1.0f);
			}
			else if(BiomeDictionary.isBiomeOfType(biome, Type.SWAMP)) {//Swamps need more tree opportunities since it's so watery
				chanceCalc = new ChanceCalcStatic(0.5f);
			} 
			else if(biome.theBiomeDecorator.treesPerChunk == -999) {//Deserts, Mesas, Beaches
				chanceCalc = new ChanceCalcStatic(0.0f);
			}
			else {
				chanceCalc = new ChanceCalcByRadius();//Let the radius determine the chance
			}

			fastChanceLookup.put(biomeId, chanceCalc);
		}
		
		//the last call should never be UNHANDLED for the DefaultBiomeDensityProvider since it is the last in the chain
		return random.nextFloat() < chanceCalc.getChance(radius) ? EnumChance.OK : EnumChance.CANCEL;
	}
	
}
