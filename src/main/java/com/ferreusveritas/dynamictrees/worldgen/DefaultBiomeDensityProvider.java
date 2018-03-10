package com.ferreusveritas.dynamictrees.worldgen;

import java.util.HashMap;
import java.util.Random;

import com.ferreusveritas.dynamictrees.ModConstants;
import com.ferreusveritas.dynamictrees.api.worldgen.IBiomeDensityProvider;
import com.ferreusveritas.dynamictrees.trees.Species;
import com.ferreusveritas.dynamictrees.util.CompatHelper;
import com.ferreusveritas.dynamictrees.util.MathHelper;

import net.minecraft.init.Biomes;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.biome.Biome;
import net.minecraftforge.common.BiomeDictionary.Type;

public class DefaultBiomeDensityProvider implements IBiomeDensityProvider {

	private interface IChance {
		EnumChance getChance(Random random, int radius);
	}

	private class ChanceStatic implements IChance {
		private final EnumChance chance;
		
		public ChanceStatic(EnumChance chance) {
			this.chance = chance;
		}
		
		@Override
		public EnumChance getChance(Random random, int radius) {
			return chance;
		}
	}

	private class ChanceRandom implements IChance {
		private final float value;
		
		public ChanceRandom(float value) {
			this.value = value;
		}
		
		@Override
		public EnumChance getChance(Random random, int radius) {
			return random.nextFloat() < value ? EnumChance.OK : EnumChance.CANCEL;
		}
	}
	
	private class ChanceByRadius implements IChance {
		@Override
		public EnumChance getChance(Random random, int radius) {
			float chance = 1.0f;
			
			if(radius > 3) {//Start dropping tree spawn opportunities when the radius gets bigger than 3
				chance = 2.0f / radius;
				return random.nextFloat() < chance ? EnumChance.OK : EnumChance.CANCEL;
			}

			return random.nextFloat() < chance ? EnumChance.OK : EnumChance.CANCEL;
		}
	}
		
	HashMap<Integer, IChance> fastChanceLookup = new HashMap<Integer, IChance>();
	
	@Override
	public ResourceLocation getName() {
		return new ResourceLocation(ModConstants.MODID, "default");
	}
	
	@Override
	public int getPriority() {
		return 0;
	}

	@Override
	public double getDensity(Biome biome, double noiseDensity, Random random) {
		
		if(CompatHelper.biomeHasType(biome, Type.SPOOKY)) { //Roofed Forest
			return 0.4f + (noiseDensity / 3.0f);
		}
		
		if(CompatHelper.biomeHasType(biome, Type.SANDY)) { //Desert
			return (noiseDensity * 0.6) + 0.4;
		}
		
		double naturalDensity = MathHelper.clamp((CompatHelper.getBiomeTreesPerChunk(biome)) / 10.0f, 0.0f, 1.0f);//Gives 0.0 to 1.0
		return noiseDensity * naturalDensity;
	}
	
	@Override
	public EnumChance chance(Biome biome, Species species, int radius, Random random) {
		
		int biomeId = Biome.getIdForBiome(biome);
		IChance chance = fastChanceLookup.get(biomeId);
		
		if(chance == null) {
			if(CompatHelper.biomeHasType(biome, Type.CONIFEROUS)) {
				chance = new IChance() {
					@Override
					public EnumChance getChance(Random random, int radius) {
						if(radius > 6) {
							return random.nextFloat() < 0.5f ? EnumChance.OK : EnumChance.CANCEL;
						}

						return EnumChance.OK;
					}
				};
			}
			else if(CompatHelper.biomeHasType(biome, Type.FOREST)) {//Never miss a chance to spawn a tree in a forest.
				chance = new ChanceStatic(EnumChance.OK);
			}
			else if(biome == Biomes.MUTATED_ROOFED_FOREST) {//Although this is a forest it's not registered as one for some reason
				chance = new ChanceStatic(EnumChance.OK);
			}
			else if(CompatHelper.biomeHasType(biome, Type.SWAMP)) {//Swamps need more tree opportunities since it's so watery
				chance = new ChanceRandom(0.75f);
			} 
			else if(CompatHelper.biomeHasType(biome, Type.SANDY)) {//Deserts (for cacti)
				chance = new ChanceRandom(0.075f);
			}
			else if(CompatHelper.getBiomeTreesPerChunk(biome) < 0) {//Deserts, Mesas, Beaches(-999) Mushroom Island(-100)
				chance = new ChanceStatic(EnumChance.CANCEL);
			}
			else {
				chance = new ChanceByRadius();//Let the radius determine the chance
			}

			fastChanceLookup.put(biomeId, chance);
		}
		
		//the last call should never be UNHANDLED for the DefaultBiomeDensityProvider since it is the last in the chain
		return chance.getChance(random, radius);
	}
	
}
