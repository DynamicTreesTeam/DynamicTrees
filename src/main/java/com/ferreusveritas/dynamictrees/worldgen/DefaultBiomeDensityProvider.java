package com.ferreusveritas.dynamictrees.worldgen;

import java.util.ArrayList;
import java.util.Collections;
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
	
	private ArrayList<DensityData> densityDataLookup = new ArrayList<DensityData>(Collections.nCopies(256, null));
	
	@Override
	public ResourceLocation getName() {
		return new ResourceLocation(ModConstants.MODID, "default");
	}
	
	@Override
	public int getPriority() {
		return 0;
	}

	@Override
	public double density(Biome biome, double noiseDensity, Random random) {
		return getDensityData(biome).getDensity().getDensity(random, noiseDensity);
	}

	@Override
	public EnumChance chance(Biome biome, Species species, int radius, Random random) {
		return getDensityData(biome).getChance().getChance(random, species, radius);
	}
	
	public DensityData getDensityData(Biome biome) {
		int biomeId = Biome.getIdForBiome(biome);
		DensityData densityData = densityDataLookup.get(biomeId);
		
		if(densityData == null) {
			densityData = computeDensityData(biome);
			densityDataLookup.set(biomeId, densityData);
		}
		
		return densityData;
	}
	
	public void injectDensityData(Biome biome, DensityData data) {
		int biomeId = Biome.getIdForBiome(biome);
		densityDataLookup.set(biomeId, data);
	}
	
	public DensityData computeDensityData(Biome biome) {
		return new DensityData(computeChance(biome), computeDensity(biome));
	}
	
	public IDensity computeDensity(Biome biome) {
		if(CompatHelper.biomeHasType(biome, Type.SPOOKY)) { //Roofed Forest
			return (rnd, nd) -> { return 0.4f + (nd / 3.0f); };
		}
		if(CompatHelper.biomeHasType(biome, Type.SANDY)) { //Desert
			return (rnd, nd) -> { return ( nd * 0.6) + 0.4; };
		}
		final double treeDensity = MathHelper.clamp((CompatHelper.getBiomeTreesPerChunk(biome)) / 10.0f, 0.0f, 1.0f);//Gives 0.0 to 1.0
		return (rnd, nd) -> { return nd * treeDensity; };
	}
	
	public IChance computeChance(Biome biome) {
		if(CompatHelper.biomeHasType(biome, Type.CONIFEROUS)) {
			return (rnd, spc, rad) -> { return rad > 6 && rnd.nextFloat() < 0.5f ? EnumChance.CANCEL : EnumChance.OK; };
		}
		if(CompatHelper.biomeHasType(biome, Type.FOREST)) {//Never miss a chance to spawn a tree in a forest.
			return (rnd, spc, rad) -> { return EnumChance.OK; };
		}
		if(biome == Biomes.MUTATED_ROOFED_FOREST) {//Although this is a forest it's not registered as one for some reason
			return (rnd, spc, rad) -> { return EnumChance.OK; };
		}
		if(CompatHelper.biomeHasType(biome, Type.SWAMP)) {//Swamps need more tree opportunities since it's so watery
			return (rnd, spc, rad) -> { return rnd.nextFloat() < 0.75f ? EnumChance.OK : EnumChance.CANCEL; };
		} 
		if(CompatHelper.biomeHasType(biome, Type.SANDY)) {//Deserts (for cacti)
			return (rnd, spc, rad) -> { return rnd.nextFloat() < 0.75f ? EnumChance.OK : EnumChance.CANCEL; };
		}
		else if(CompatHelper.getBiomeTreesPerChunk(biome) < 0) {//Deserts, Mesas, Beaches(-999) Mushroom Island(-100)
			return (rnd, spc, rad) -> { return EnumChance.CANCEL; };
		}
		if (biome == Biomes.RIVER) {
			return (rnd, spc, rad) -> { return EnumChance.CANCEL; };
		}
		else {
			return (rnd, spc, rad) -> {//Let the radius determine the chance
				//Start dropping tree spawn opportunities when the radius gets bigger than 3
				return rnd.nextFloat() < (rad > 3 ? 2.0f / rad : 1.0f) ? EnumChance.OK : EnumChance.CANCEL;
				//Note: the last call should never be UNHANDLED for the DefaultBiomeDensityProvider since it is the last in the chain
			};
		}
	}
	
}
