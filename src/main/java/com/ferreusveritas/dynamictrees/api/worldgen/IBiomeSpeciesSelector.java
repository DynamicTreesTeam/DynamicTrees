package com.ferreusveritas.dynamictrees.api.worldgen;

import java.util.ArrayList;
import java.util.Random;

import com.ferreusveritas.dynamictrees.api.TreeRegistry;
import com.ferreusveritas.dynamictrees.trees.Species;

import net.minecraft.block.state.IBlockState;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;

/**
 * Provides the tree used for a given biome
 * 
 * Mods should implement this interface and register it via the {@link TreeRegistry} to control which trees spawn in a {@link Biome}.
 * 
 * @author ferreusveritas
 */
public interface IBiomeSpeciesSelector {

	/**
	 * This is the data that represents a species selection.
	 * This class was necessary to have an unhandled state.
	 */
	public class SpeciesSelection {
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
	
	public interface ISpeciesSelector {
		SpeciesSelection getSpecies(BlockPos pos, IBlockState dirt, Random random);
	}
	
	/**
	 * A unique name to identify this {@link IBiomeSpeciesSelector}.
	 * It's recommended to use something like "modid:name"
	 * 
	 * @return
	 */
	public ResourceLocation getName();
	
	/**
	 * This is called during the init phase of the DynamicTrees mod.  Use this to get references to trees
	 * that have been registered during the preInit phase.
	 */
	public void init();
	
	/**
	 * 
	 * @param world
	 * @param biome
	 * @param pos
	 * @param dirt
	 * @return A decision on which tree to use.  Set decision to null for no tree.
	 */
	public SpeciesSelection getSpecies(World world, Biome biome, BlockPos pos, IBlockState dirt, Random random);
	
	/**
	 * Used to determine which selector should run first.  Higher values are executed first.  Negative values are allowed.
	 * 
	 * @return priority number
	 */
	public int getPriority();
	
	public class StaticSpeciesSelector implements ISpeciesSelector {
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
	
	public class RandomSpeciesSelector implements ISpeciesSelector {

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
		Random rand;
		
		public RandomSpeciesSelector(Random rand) {
			this.rand = rand;
		}
		
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
			int chance = rand.nextInt(totalWeight);
			
			for(Entry entry: decisionTable) {
				if(chance < entry.weight) {
					return entry.decision;
				}
				chance -= entry.weight;
			};

			return decisionTable.get(decisionTable.size() - 1).decision;
		}
		
	}
	
}
