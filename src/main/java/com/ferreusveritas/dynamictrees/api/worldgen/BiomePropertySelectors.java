package com.ferreusveritas.dynamictrees.api.worldgen;

import java.util.ArrayList;
import java.util.Random;

import com.ferreusveritas.dynamictrees.api.TreeRegistry;
import com.ferreusveritas.dynamictrees.trees.Species;

import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.biome.Biome;

/**
 * Provides the forest density for a given biome.
 * Mods should implement this interface and register it via the {@link TreeRegistry} to control how densely populated a {@link Biome} is.
 * 
 * @author ferreusveritas
 */
public class BiomePropertySelectors {
	
	public interface IChanceSelector {
		EnumChance getChance(Random random, Species species, int radius);
	}

	public interface IDensitySelector {
		double getDensity(Random random, double noiseDensity);
	}

	public interface ISpeciesSelector {
		SpeciesSelection getSpecies(BlockPos pos, IBlockState dirt, Random random);
	}
	
	/**
	 * This is the data that represents a species selection.
	 * This class was necessary to have an unhandled state.
	 */
	public static class SpeciesSelection {
		private boolean handled;
		private Species species;
		
		public SpeciesSelection() {
			handled = false;
		}
		
		public SpeciesSelection(Species species) {
			this.species = species;
			handled = true;
		}
		
		public boolean isHandled() {
			return handled;
		}
		
		public Species getSpecies() {
			return species;
		}
	}
	
	public static class StaticSpeciesSelector implements ISpeciesSelector {
		final SpeciesSelection decision;
		
		public StaticSpeciesSelector(SpeciesSelection decision) {
			this.decision = decision;
		}

		public StaticSpeciesSelector(Species species) {
			this(new SpeciesSelection(species));
		}
		
		public StaticSpeciesSelector() {
			this(new SpeciesSelection());
		}
		
		@Override
		public SpeciesSelection getSpecies(BlockPos pos, IBlockState dirt, Random random) {
			return decision;
		}
	}
	
	public static class RandomSpeciesSelector implements ISpeciesSelector {

		private class Entry {
			public Entry(SpeciesSelection d, int w) {
				decision = d;
				weight = w;
			}
			
			public SpeciesSelection decision;
			public int weight;
		}
		
		ArrayList<Entry> decisionTable = new ArrayList<Entry>();
		int totalWeight;
		
		public RandomSpeciesSelector addSpecies(Species species, int weight) {
			decisionTable.add(new Entry(new SpeciesSelection(species), weight));
			totalWeight += weight;
			return this;
		}
		
		public RandomSpeciesSelector addUnhandled(int weight) {
			decisionTable.add(new Entry(new SpeciesSelection(), weight));
			totalWeight += weight;
			return this;
			
		}
		
		@Override
		public SpeciesSelection getSpecies(BlockPos pos, IBlockState dirt, Random random) {
			int chance = random.nextInt(totalWeight);
			
			for(Entry entry: decisionTable) {
				if(chance < entry.weight) {
					return entry.decision;
				}
				chance -= entry.weight;
			};

			return decisionTable.get(decisionTable.size() - 1).decision;
		}
		
	}
	
	
	public enum EnumChance {
		OK,
		CANCEL,
		UNHANDLED
	}
}
