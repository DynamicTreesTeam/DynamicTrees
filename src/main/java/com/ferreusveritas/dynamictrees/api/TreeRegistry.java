package com.ferreusveritas.dynamictrees.api;

import java.util.ArrayList;

import com.ferreusveritas.dynamictrees.ModConstants;
import com.ferreusveritas.dynamictrees.api.treedata.DropCreatorStorage;
import com.ferreusveritas.dynamictrees.api.treedata.IBiomeSuitabilityDecider;
import com.ferreusveritas.dynamictrees.api.treedata.IDropCreator;
import com.ferreusveritas.dynamictrees.trees.DynamicTree;
import com.ferreusveritas.dynamictrees.trees.Species;

import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;

/**
* A registry for all of the dynamic trees. Use this for this mod or other mods.
* 
* @author ferreusveritas
*/
public class TreeRegistry {

	private static final ArrayList<IBiomeSuitabilityDecider> biomeSuitabilityDeciders = new ArrayList<IBiomeSuitabilityDecider>();
	public static final DropCreatorStorage globalDropCreatorStorage = new DropCreatorStorage();
	
	//////////////////////////////
	// TREE REGISTRY
	//////////////////////////////
	
	/**
	 * Mods should use this to register their {@link DynamicTree}
	 * 
	 * Places the tree in a central registry.
	 * The proper place to use this is during the preInit phase of your mod.
	 * 
	 * @param species The dynamic tree being registered
	 * @return DynamicTree for chaining
	 */
	public static Species registerSpecies(Species species) {
		Species.REGISTRY.register(species);
		return species;
	}

	public static void registerSpecies(Species ... values) {
		Species.REGISTRY.registerAll(values);
	}
	
	public static Species findSpecies(ResourceLocation name) {
		return Species.REGISTRY.getValue(name);
	}
	
	/**
	 * Searches first for the full tree name.  If that fails then it
	 * will find the first tree matching the simple name and return it instead otherwise null
	 * 
	 * @param name The name of the tree.  Either the simple name or the full name
	 * @return The tree that was found or null if not found
	 */
	public static Species findSpeciesSloppy(String name) {
		
		//Exact find
		ResourceLocation resloc = new ResourceLocation(name);
		if(Species.REGISTRY.containsKey(resloc)) {
			return Species.REGISTRY.getValue(resloc);
		}

		//Search DynamicTrees Domain
		resloc = new ResourceLocation(ModConstants.MODID, resloc.getResourcePath());
		if(Species.REGISTRY.containsKey(resloc)) {
			return Species.REGISTRY.getValue(resloc);
		}
		
		//Search all domains
		for(Species species : Species.REGISTRY) {
			if(species.getRegistryName().getResourcePath().equals(resloc.getResourcePath())) {
				return species;
			}
		}
		
		return null;
	}

	//////////////////////////////
	// DROP HANDLING
	//////////////////////////////
	
	/**
	 * This exists so that mods not interested in making Dynamic Trees can still add drops to
	 * all trees.
	 * 
	 * @param dropCreator
	 */
	public static void registerDropCreator(IDropCreator dropCreator) {
		globalDropCreatorStorage.addDropCreator(dropCreator);
	}
	
	
	//////////////////////////////
	// BIOME HANDLING
	//////////////////////////////
	
	/**
	 * Mods should call this to register an {@link IBiomeSuitabilityDecider}
	 * 
	 * @param decider The decider being registered
	 */
	public static void registerBiomeSuitabilityDecider(IBiomeSuitabilityDecider decider) {
		biomeSuitabilityDeciders.add(decider);
	}
	
	private static final IBiomeSuitabilityDecider.Decision undecided = new IBiomeSuitabilityDecider.Decision();
	
	public static IBiomeSuitabilityDecider.Decision getBiomeSuitability(World world, Biome biome, Species species, BlockPos pos) {
		for(IBiomeSuitabilityDecider decider: biomeSuitabilityDeciders) {
			IBiomeSuitabilityDecider.Decision decision = decider.getBiomeSuitability(world, biome, species, pos);
			if(decision.isHandled()) {
				return decision;
			}
		}
		
		return undecided;
	}
	
	public static boolean isBiomeSuitabilityOverrideEnabled() {
		return !biomeSuitabilityDeciders.isEmpty();
	}
	
}
