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

	private interface IDensity {
		double getDensity(Random random, double noiseDensity);
	}
	
	HashMap<Integer, IChance> fastChanceLookup = new HashMap<Integer, IChance>();
	HashMap<Integer, IDensity> fastDensityLookup = new HashMap<Integer, IDensity>();

	int lastBiomeChanceId = -1;
	IChance lastBiomeChance = null;

	int lastBiomeDensityId = -1;
	IDensity lastBiomeDensity = null;
	
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
		int biomeId = Biome.getIdForBiome(biome);
		
		return fastDensityLookup.computeIfAbsent(biomeId, k -> computeDensity(biome, noiseDensity, random)).getDensity(random, noiseDensity);
	}

	@Override
	public EnumChance chance(Biome biome, Species species, int radius, Random random) {
		int biomeId = Biome.getIdForBiome(biome);
		
		return fastChanceLookup.computeIfAbsent(biomeId, k -> computeChance(biome, species, radius, random)).getChance(random, radius);
	}
	
	public IDensity computeDensity(Biome biome, double noiseDensity, Random random) {
		if(CompatHelper.biomeHasType(biome, Type.SPOOKY)) { //Roofed Forest
			return (rnd, nd) -> { return 0.4f + (nd / 3.0f); };
		}
		if(CompatHelper.biomeHasType(biome, Type.SANDY)) { //Desert
			return (rnd, nd) -> { return ( nd * 0.6) + 0.4; };
		}
		final double treeDensity = MathHelper.clamp((CompatHelper.getBiomeTreesPerChunk(biome)) / 10.0f, 0.0f, 1.0f);//Gives 0.0 to 1.0
		return (rnd, nd) -> { return nd * treeDensity; };
	}
	
	public IChance computeChance(Biome biome, Species species, int radius, Random random) {
		if(CompatHelper.biomeHasType(biome, Type.CONIFEROUS)) {
			return (rnd, rad) -> { return radius > 6 && rnd.nextFloat() < 0.5f ? EnumChance.CANCEL : EnumChance.OK; };
		}
		if(CompatHelper.biomeHasType(biome, Type.FOREST)) {//Never miss a chance to spawn a tree in a forest.
			return (rnd, rad) -> { return EnumChance.OK; };
		}
		if(biome == Biomes.MUTATED_ROOFED_FOREST) {//Although this is a forest it's not registered as one for some reason
			return (rnd, rad) -> { return EnumChance.OK; };
		}
		if(CompatHelper.biomeHasType(biome, Type.SWAMP)) {//Swamps need more tree opportunities since it's so watery
			return (rnd, rad) -> { return rnd.nextFloat() < 0.75f ? EnumChance.OK : EnumChance.CANCEL; };
		} 
		if(CompatHelper.biomeHasType(biome, Type.SANDY)) {//Deserts (for cacti)
			return (rnd, rad) -> { return rnd.nextFloat() < 0.75f ? EnumChance.OK : EnumChance.CANCEL; };
		}
		else if(CompatHelper.getBiomeTreesPerChunk(biome) < 0) {//Deserts, Mesas, Beaches(-999) Mushroom Island(-100)
			return (rnd, rad) -> { return EnumChance.CANCEL; };
		}
		else {
			return (rnd, rad) -> {//Let the radius determine the chance
				//Start dropping tree spawn opportunities when the radius gets bigger than 3
				return random.nextFloat() < (rad > 3 ? 2.0f / rad : 1.0f) ? EnumChance.OK : EnumChance.CANCEL;
				//Note: the last call should never be UNHANDLED for the DefaultBiomeDensityProvider since it is the last in the chain
			};
		}
	}
	
}
