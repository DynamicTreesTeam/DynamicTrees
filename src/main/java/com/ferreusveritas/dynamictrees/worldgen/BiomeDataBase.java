package com.ferreusveritas.dynamictrees.worldgen;

import com.ferreusveritas.dynamictrees.ModConfigs;
import com.ferreusveritas.dynamictrees.api.worldgen.BiomePropertySelectors.*;
import net.minecraft.init.Biomes;
import net.minecraft.world.biome.Biome;

import java.util.function.Function;

public class BiomeDataBase {

	public static BiomeEntry BADENTRY = new BiomeEntry() {
		@Override
		public void setChanceSelector(IChanceSelector chanceSelector) {
		}

		@Override
		public void setDensitySelector(IDensitySelector densitySelector) {
		}

		@Override
		public void setSpeciesSelector(ISpeciesSelector speciesSelector) {
		}

		@Override
		public void setCancelVanillaTreeGen(boolean cancel) {
		}

		@Override
		public void setSubterraneanBiome(boolean is) {
		}
	};

	//A reasonably fast 16x16 sparse array 
	private final BiomeEntry[][] table = new BiomeEntry[16][];

	public BiomeEntry getEntry(Biome biome) {
		if (biome != null) {
			int biomeId = Biome.getIdForBiome(biome);//This should only return 0 - 255 because of Minecraft limitations
			if (biomeId >= 0 && biomeId <= 255) {//Enforce 0-255 for biome id to account for misbehaving mods like Mystcraft
				BiomeEntry[] list = table[biomeId >> 4];
				if (list == null) {
					list = table[biomeId >> 4] = new BiomeEntry[16];
					for (int i = 0; i < 16; i++) {
						list[i] = BADENTRY;
					}
				}
				BiomeEntry entry = list[biomeId & 0x0f];
				return entry != BADENTRY ? entry : (list[biomeId & 0x0f] = new BiomeEntry(biome, biomeId));
			}
		}
		return BADENTRY;
	}

	public void clear() {
		for (int i = 0; i < 16; i++) {
			table[i] = null;
		}
	}

	public boolean isValid() {
		for (Biome biome : Biome.REGISTRY) {
			BiomeEntry entry = getEntry(biome);
			if (entry.getBiome() != biome) {
				return false;
			}
		}

		return true;
	}

	public static class BiomeEntry {
		private final Biome biome;
		private final int biomeId;
		private IChanceSelector chanceSelector = (rnd, spc, rad) -> EnumChance.UNHANDLED;
		private IDensitySelector densitySelector = (rnd, nd) -> -1;
		private ISpeciesSelector speciesSelector = (pos, dirt, rnd) -> new SpeciesSelection();
		private boolean cancelVanillaTreeGen = false;
		private boolean isSubterranean = false;
		private float forestness = 0.0f;
		private final static Function<Integer, Integer> defaultMultipass = pass -> (pass == 0 ? 0 : -1);
		private Function<Integer, Integer> multipass = defaultMultipass;

		public BiomeEntry() {
			biome = Biomes.DEFAULT;
			biomeId = -1;
		}

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

		public void setChanceSelector(IChanceSelector chanceSelector) {
			this.chanceSelector = chanceSelector;
		}

		public void setDensitySelector(IDensitySelector densitySelector) {
			this.densitySelector = densitySelector;
		}

		public void setSpeciesSelector(ISpeciesSelector speciesSelector) {
			this.speciesSelector = speciesSelector;
		}

		public void setCancelVanillaTreeGen(boolean cancel) {
			this.cancelVanillaTreeGen = cancel;
		}

		public void setSubterraneanBiome(boolean is) {
			this.isSubterranean = is;
		}

		public boolean shouldCancelVanillaTreeGen() {
			return cancelVanillaTreeGen;
		}

		public boolean isSubterraneanBiome() {
			return isSubterranean;
		}

		public void setForestness(float forestness) {
			this.forestness = forestness;
		}

		public float getForestness() {
			return forestness;
		}

		public void setMultipass(Function<Integer, Integer> multipass) {
			this.multipass = multipass;
		}

		public Function<Integer, Integer> getMultipass() {
			return multipass;
		}

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

	public float getForestness(Biome biome) {
		return getEntry(biome).getForestness();
	}

	public Function<Integer, Integer> getMultipass(Biome biome) {
		return getEntry(biome).getMultipass();
	}

	public BiomeDataBase setSpeciesSelector(Biome biome, ISpeciesSelector selector, Operation op) {
		if (selector != null) {
			BiomeEntry entry = getEntry(biome);
			ISpeciesSelector existing = entry.getSpeciesSelector();

			switch (op) {
				case REPLACE:
					entry.setSpeciesSelector(selector);
					break;
				case SPLICE_BEFORE:
					entry.setSpeciesSelector((pos, dirt, rnd) -> {
						SpeciesSelection ss = selector.getSpecies(pos, dirt, rnd);
						return ss.isHandled() ? ss : existing.getSpecies(pos, dirt, rnd);
					});
					break;
				case SPLICE_AFTER:
					entry.setSpeciesSelector((pos, dirt, rnd) -> {
						SpeciesSelection ss = existing.getSpecies(pos, dirt, rnd);
						return ss.isHandled() ? ss : selector.getSpecies(pos, dirt, rnd);
					});
					break;
			}
		}
		return this;
	}

	public BiomeDataBase setChanceSelector(Biome biome, IChanceSelector selector, Operation op) {
		if (selector != null) {
			BiomeEntry entry = getEntry(biome);
			IChanceSelector existing = entry.getChanceSelector();

			switch (op) {
				case REPLACE:
					entry.setChanceSelector(selector);
					break;
				case SPLICE_BEFORE:
					entry.setChanceSelector((rnd, spc, rad) -> {
						EnumChance c = selector.getChance(rnd, spc, rad);
						return c != EnumChance.UNHANDLED ? c : existing.getChance(rnd, spc, rad);
					});
					break;
				case SPLICE_AFTER:
					entry.setChanceSelector((rnd, spc, rad) -> {
						EnumChance c = existing.getChance(rnd, spc, rad);
						return c != EnumChance.UNHANDLED ? c : selector.getChance(rnd, spc, rad);
					});
					break;
			}
		}
		return this;
	}

	public BiomeDataBase setDensitySelector(Biome biome, IDensitySelector selector, Operation op) {
		if (selector != null) {
			BiomeEntry entry = getEntry(biome);
			IDensitySelector existing = entry.getDensitySelector();

			switch (op) {
				case REPLACE:
					entry.setDensitySelector(selector);
					break;
				case SPLICE_BEFORE:
					entry.setDensitySelector((rnd, nd) -> {
						double d = selector.getDensity(rnd, nd);
						return d >= 0 ? d : existing.getDensity(rnd, nd);
					});
					break;
				case SPLICE_AFTER:
					entry.setDensitySelector((rnd, nd) -> {
						double d = existing.getDensity(rnd, nd);
						return d >= 0 ? d : selector.getDensity(rnd, nd);
					});
					break;
			}
		}
		return this;
	}

	public BiomeDataBase setCancelVanillaTreeGen(Biome biome, boolean cancel) {
		getEntry(biome).setCancelVanillaTreeGen(cancel);
		return this;
	}

	public BiomeDataBase setIsSubterranean(Biome biome, boolean is) {
		getEntry(biome).setSubterraneanBiome(is);
		return this;
	}

	public BiomeDataBase setForestness(Biome biome, float forestness) {
		getEntry(biome).setForestness(Math.max(forestness, ModConfigs.seedMinForestness));
		return this;
	}

	public BiomeDataBase setMultipass(Biome biome, Function<Integer, Integer> multipass) {
		getEntry(biome).setMultipass(multipass);
		return this;
	}

	public enum Operation {
		REPLACE,
		SPLICE_BEFORE,
		SPLICE_AFTER
	}

}
