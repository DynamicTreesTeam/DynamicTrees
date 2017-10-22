package com.ferreusveritas.dynamictrees.api;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import com.ferreusveritas.dynamictrees.ConfigHandler;
import com.ferreusveritas.dynamictrees.DynamicTrees;
import com.ferreusveritas.dynamictrees.api.treedata.IBiomeSuitabilityDecider;
import com.ferreusveritas.dynamictrees.api.worldgen.IBiomeDensityProvider;
import com.ferreusveritas.dynamictrees.api.worldgen.IBiomeTreeSelector;
import com.ferreusveritas.dynamictrees.trees.DynamicTree;

import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;

/**
* A registry for all of the dynamic trees. For this mod or other mods.
* 
* @author ferreusveritas
*/
public class TreeRegistry {

	private static Map<String, DynamicTree> treesByName = new HashMap<String, DynamicTree>();
	private static ArrayList<DynamicTree> treesById = new ArrayList<DynamicTree>();
	private static ArrayList<IBiomeSuitabilityDecider> biomeSuitabilityDeciders = new ArrayList<IBiomeSuitabilityDecider>();
	
	/**
	 * Mods should use this to register their {@link DynamicTree}
	 * 
	 * Places the tree in a central registry and gives the tree a runtime numeric serial ID 
	 * 
	 * @param tree The dynamic tree being registered
	 * @return
	 */
	public static DynamicTree registerTree(DynamicTree tree) {
		treesByName.put(tree.getName(), tree);
		int currId = treesById.size();
		treesById.add(tree);
		tree.register(currId);
		return tree;
	}

	public static DynamicTree findTree(String name) {
		return treesByName.get(name);
	}

	public static DynamicTree getTreeById(int id) {
		return treesById.get(id);
	}

	public static ArrayList<DynamicTree> getTrees() {
		return treesById;
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
	
	public static IBiomeSuitabilityDecider.Decision getBiomeSuitability(World world, Biome biome, DynamicTree tree, BlockPos pos) {
		for(IBiomeSuitabilityDecider decider: biomeSuitabilityDeciders) {
			IBiomeSuitabilityDecider.Decision decision = decider.getBiomeSuitability(world, biome, tree, pos);
			if(decision.isHandled()) {
				return decision;
			}
		}
		
		return undecided;
	}
	
	public static boolean isBiomeSuitabilityOverrideEnabled() {
		return !biomeSuitabilityDeciders.isEmpty();
	}
	
	/**
	 * Mods should call this to register an {@link IBiomeTreeSelector}.
	 * 
	 * @param treeSelector The tree selector being registered
	 * @return
	 */
	public static boolean registerBiomeTreeSelector(IBiomeTreeSelector treeSelector) {
		if(ConfigHandler.worldGen && DynamicTrees.treeGenerator != null) {
			DynamicTrees.treeGenerator.biomeTreeHandler.addTreeSelector(treeSelector);
			return true;
		}
		return false;
	}
	
	/**
	 * Mods should call this to register an {@link IBiomeDensityProvider}
	 * 
	 * @param densityProvider The density provider being registered
	 * @return
	 */
	public static boolean registerBiomeDensityProvider(IBiomeDensityProvider densityProvider) {
		if(ConfigHandler.worldGen && DynamicTrees.treeGenerator != null) {
			DynamicTrees.treeGenerator.biomeTreeHandler.addDensityProvider(densityProvider);
			return true;
		}
		return false;
	}
}
