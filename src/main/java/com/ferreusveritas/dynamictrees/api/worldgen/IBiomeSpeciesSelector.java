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
	public Decision getSpecies(World world, Biome biome, BlockPos pos, IBlockState dirt, Random random);
	
	/**
	 * Used to determine which selector should run first.  Higher values are executed first.  Negative values are allowed.
	 * 
	 * @return priority number
	 */
	public int getPriority();
	
	public class Decision {
		private boolean handled;
		private Species species;
		
		public Decision() {
			handled = false;
		}
		
		public Decision(Species species) {
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

	public interface DecisionProvider {
		Decision getDecision();
	}
	
	public class StaticDecision implements DecisionProvider {
		final Decision decision;
		
		public StaticDecision(Decision decision) {
			this.decision = decision;
		}

		@Override
		public Decision getDecision() {
			return decision;
		}
	}
	
	public class RandomDecision implements DecisionProvider {

		private class Entry {
			public Entry(Decision d, int w) {
				decision = d;
				weight = w;
			}
			
			public Decision decision;
			public int weight;
		}
		
		ArrayList<Entry> decisionTable = new ArrayList<Entry>();
		int totalWeight;
		Random rand;
		
		public RandomDecision(Random rand) {
			this.rand = rand;
		}
		
		public RandomDecision addSpecies(Species species, int weight) {
			decisionTable.add(new Entry(new Decision(species), weight));
			totalWeight += weight;
			return this;
		}
		
		public RandomDecision addUnhandled(int weight) {
			decisionTable.add(new Entry(new Decision(), weight));
			totalWeight += weight;
			return this;
			
		}
		
		@Override
		public Decision getDecision() {
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
