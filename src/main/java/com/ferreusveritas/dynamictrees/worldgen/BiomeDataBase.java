package com.ferreusveritas.dynamictrees.worldgen;

import java.util.ArrayList;
import java.util.Collections;

import com.ferreusveritas.dynamictrees.api.worldgen.IBiomeDensityProvider.EnumChance;
import com.ferreusveritas.dynamictrees.api.worldgen.IBiomeDensityProvider.IChanceSelector;
import com.ferreusveritas.dynamictrees.api.worldgen.IBiomeDensityProvider.IDensitySelector;
import com.ferreusveritas.dynamictrees.api.worldgen.IBiomeSpeciesSelector.ISpeciesSelector;
import com.ferreusveritas.dynamictrees.api.worldgen.IBiomeSpeciesSelector.SpeciesSelection;

import net.minecraft.world.biome.Biome;

public class BiomeDataBase {

	private final ArrayList<Entry> table = new ArrayList<Entry>(Collections.nCopies(256, null));
	
	public class Entry {
		private final Biome biome;
		private final int biomeId;
		private IChanceSelector chanceSelector = (rnd, spc, rad) -> { return EnumChance.UNHANDLED; };
		private IDensitySelector densitySelector = (rnd, nd) -> { return -1; };
		private ISpeciesSelector speciesSelector = (pos, dirt, rnd) -> { return new SpeciesSelection(); };
		private boolean cancelVanillaTreeGen = false;
		
		public Entry(Biome biome, int biomeId) {
			this.biome = biome;
			this.biomeId = biomeId;
		}
		
		public Biome getBiome() {
			return biome;
		}
		
		public int getBiomeId() {
			return biomeId;
		}
		
		public IChanceSelector getChanceSelector() {
			return chanceSelector;
		}
		
		public IDensitySelector getDensitySelector() {
			return densitySelector;
		}
		
		public ISpeciesSelector getSpeciesSelector() {
			return speciesSelector;
		}
		
		public boolean shouldCancelVanillaTreeGen() {
			return cancelVanillaTreeGen;
		}
	}
	
	public Entry getEntry(Biome biome) {
		int biomeId = Biome.getIdForBiome(biome);
		Entry entry = table.get(biomeId);
		
		if(entry == null) {
			entry = new Entry(biome, biomeId);
		}
		
		return entry;
	}
	
	public ISpeciesSelector getSpecies(Biome biome) {
		return getEntry(biome).speciesSelector;
	}
	
	public IChanceSelector getChance(Biome biome) {
		return getEntry(biome).chanceSelector;
	}

	public IDensitySelector getDensity(Biome biome) {
		return getEntry(biome).densitySelector;
	}
	
	public boolean shouldCancelVanillaTreeGen(Biome biome) {
		return getEntry(biome).cancelVanillaTreeGen;
	}
	
	public BiomeDataBase setSpeciesSelector(Biome biome, ISpeciesSelector decider) {
		getEntry(biome).speciesSelector = decider;
		return this;
	}
	
	public BiomeDataBase spliceBeforeSpeciesSelector(Biome biome, ISpeciesSelector decider) {
		Entry entry = getEntry(biome);
		ISpeciesSelector existing = entry.speciesSelector;
		entry.speciesSelector = (pos, dirt, rnd) -> {
			SpeciesSelection ss = decider.getSpecies(pos, dirt, rnd);
			return ss.isHandled() ? ss : existing.getSpecies(pos, dirt, rnd);
		};
		return this;
	}
	
	public BiomeDataBase setChanceSelector(Biome biome, IChanceSelector chance) {
		getEntry(biome).chanceSelector = chance;
		return this;
	}

	public BiomeDataBase spliceBeforeChanceSelector(Biome biome, IChanceSelector chance) {
		Entry entry = getEntry(biome);
		IChanceSelector existing = entry.chanceSelector;
		entry.chanceSelector = (rnd, spc, rad) -> {
			EnumChance c = chance.getChance(rnd, spc, rad);
			return c != EnumChance.UNHANDLED ? c : existing.getChance(rnd, spc, rad);
		};
		return this;
	}
	
	public BiomeDataBase setDensitySelector(Biome biome, IDensitySelector density) {
		getEntry(biome).densitySelector = density;
		return this;
	}

	public BiomeDataBase spliceBeforeDensitySelector(Biome biome, IDensitySelector density) {
		Entry entry = getEntry(biome);
		IDensitySelector existing = entry.densitySelector;
		entry.densitySelector = (rnd, nd) -> {
			double d = density.getDensity(rnd, nd);
			return d >= 0 ? d : existing.getDensity(rnd, nd);
		};
		return this;
	}
	
	public BiomeDataBase setCancelVanillaTreeGen(Biome biome, boolean cancel) {
		getEntry(biome).cancelVanillaTreeGen = cancel;
		return this;
	}
}
