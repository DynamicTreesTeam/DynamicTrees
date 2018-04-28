package com.ferreusveritas.dynamictrees.worldgen;

import java.util.ArrayList;
import java.util.Collections;

import com.ferreusveritas.dynamictrees.api.worldgen.BiomePropertySelectors.EnumChance;
import com.ferreusveritas.dynamictrees.api.worldgen.BiomePropertySelectors.IChanceSelector;
import com.ferreusveritas.dynamictrees.api.worldgen.BiomePropertySelectors.IDensitySelector;
import com.ferreusveritas.dynamictrees.api.worldgen.BiomePropertySelectors.ISpeciesSelector;
import com.ferreusveritas.dynamictrees.api.worldgen.BiomePropertySelectors.SpeciesSelection;

import net.minecraft.world.biome.Biome;

public class BiomeDataBase {

	private final ArrayList<BiomeEntry> table = new ArrayList<BiomeEntry>(Collections.nCopies(256, null));
	
	public class BiomeEntry {
		private final Biome biome;
		private final int biomeId;
		private IChanceSelector chanceSelector = (rnd, spc, rad) -> { return EnumChance.UNHANDLED; };
		private IDensitySelector densitySelector = (rnd, nd) -> { return -1; };
		private ISpeciesSelector speciesSelector = (pos, dirt, rnd) -> { return new SpeciesSelection(); };
		private boolean cancelVanillaTreeGen = false;
		
		public BiomeEntry(Biome biome, int biomeId) {
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
	
	public BiomeEntry getEntry(Biome biome) {
		int biomeId = Biome.getIdForBiome(biome);
		BiomeEntry entry = table.get(biomeId);
		
		if(entry == null) {
			entry = new BiomeEntry(biome, biomeId);
			table.set(biomeId, entry);
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
	
	public BiomeDataBase setSpeciesSelector(Biome biome, ISpeciesSelector selector, Operation op) {
		BiomeEntry entry = getEntry(biome);
		ISpeciesSelector existing = entry.speciesSelector;
		
		switch (op) {
			case REPLACE:
				entry.speciesSelector = selector;
				break;
			case SPLICE_AFTER:
				entry.speciesSelector = (pos, dirt, rnd) -> {
					SpeciesSelection ss = selector.getSpecies(pos, dirt, rnd);
					return ss.isHandled() ? ss : existing.getSpecies(pos, dirt, rnd);
				};
				break;
			case SPLICE_BEFORE:
				entry.speciesSelector = (pos, dirt, rnd) -> {
					SpeciesSelection ss = existing.getSpecies(pos, dirt, rnd);
					return ss.isHandled() ? ss : selector.getSpecies(pos, dirt, rnd);
				};
			break;
		}
		
		return this;
	}
	
	public BiomeDataBase setChanceSelector(Biome biome, IChanceSelector selector, Operation op) {
		BiomeEntry entry = getEntry(biome);
		IChanceSelector existing = entry.chanceSelector;
		
		switch(op) {
			case REPLACE:
				entry.chanceSelector = selector;
				break;
			case SPLICE_AFTER:
				entry.chanceSelector = (rnd, spc, rad) -> {
					EnumChance c = selector.getChance(rnd, spc, rad);
					return c != EnumChance.UNHANDLED ? c : existing.getChance(rnd, spc, rad);
				};
				break;
			case SPLICE_BEFORE:
				entry.chanceSelector = (rnd, spc, rad) -> {
					EnumChance c = existing.getChance(rnd, spc, rad);
					return c != EnumChance.UNHANDLED ? c : selector.getChance(rnd, spc, rad);
				};
				break;
		}
		
		return this;
	}
	
	public BiomeDataBase setDensitySelector(Biome biome, IDensitySelector selector, Operation op) {
		BiomeEntry entry = getEntry(biome);
		IDensitySelector existing = entry.densitySelector;
		
		switch (op) {
			case REPLACE:
				entry.densitySelector = selector;
				break;
			case SPLICE_AFTER:
				entry.densitySelector = (rnd, nd) -> {
					double d = selector.getDensity(rnd, nd);
					return d >= 0 ? d : existing.getDensity(rnd, nd);
				};
				break;
			case SPLICE_BEFORE:
				entry.densitySelector = (rnd, nd) -> {
					double d = existing.getDensity(rnd, nd);
					return d >= 0 ? d : selector.getDensity(rnd, nd);
				};
				break;
		}
		
		return this;
	}
	
	public BiomeDataBase setCancelVanillaTreeGen(Biome biome, boolean cancel) {
		getEntry(biome).cancelVanillaTreeGen = cancel;
		return this;
	}
	
	public enum Operation {
		REPLACE,
		SPLICE_BEFORE,
		SPLICE_AFTER
	}
}
