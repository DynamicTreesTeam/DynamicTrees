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
				chance = (rnd, rad) -> { return radius > 6 && rnd.nextFloat() < 0.5f ? EnumChance.CANCEL : EnumChance.OK; };
			}
			else if(CompatHelper.biomeHasType(biome, Type.FOREST)) {//Never miss a chance to spawn a tree in a forest.
				chance = (rnd, rad) -> { return EnumChance.OK; };
			}
			else if(biome == Biomes.MUTATED_ROOFED_FOREST) {//Although this is a forest it's not registered as one for some reason
				chance = (rnd, rad) -> { return EnumChance.OK; };
			}
			else if(CompatHelper.biomeHasType(biome, Type.SWAMP)) {//Swamps need more tree opportunities since it's so watery
				chance = (rnd, rad) -> { return rnd.nextFloat() < 0.75f ? EnumChance.OK : EnumChance.CANCEL; };
			} 
			else if(CompatHelper.biomeHasType(biome, Type.SANDY)) {//Deserts (for cacti)
				chance = (rnd, rad) -> { return rnd.nextFloat() < 0.75f ? EnumChance.OK : EnumChance.CANCEL; };
			}
			else if(CompatHelper.getBiomeTreesPerChunk(biome) < 0) {//Deserts, Mesas, Beaches(-999) Mushroom Island(-100)
				chance = (rnd, rad) -> { return EnumChance.CANCEL; };
			}
			else {
				chance = (rnd, rad) -> {//Let the radius determine the chance
					//Start dropping tree spawn opportunities when the radius gets bigger than 3
					return random.nextFloat() < (rad > 3 ? 2.0f / rad : 1.0f) ? EnumChance.OK : EnumChance.CANCEL;
				};
			}

			fastChanceLookup.put(biomeId, chance);
		}
		
		//the last call should never be UNHANDLED for the DefaultBiomeDensityProvider since it is the last in the chain
		return chance.getChance(random, radius);
	}
	
}
