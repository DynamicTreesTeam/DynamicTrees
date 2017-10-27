package com.ferreusveritas.dynamictrees.api;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import com.ferreusveritas.dynamictrees.api.treedata.IBiomeSuitabilityDecider;
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
	private static Map<String, DynamicTree> treesByFullName = new HashMap<String, DynamicTree>();
	private static ArrayList<DynamicTree> treesById = new ArrayList<DynamicTree>();
	private static ArrayList<IBiomeSuitabilityDecider> biomeSuitabilityDeciders = new ArrayList<IBiomeSuitabilityDecider>();
	
	//////////////////////////////
	// TREE REGISTRY
	//////////////////////////////
	
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
		treesByFullName.put(tree.getFullName(), tree);
		int currId = treesById.size();
		treesById.add(tree);
		tree.register(currId);
		return tree;
	}

	/**
	 * Searches first for the full tree name.  If that fails then it
	 * will find the first tree matching the simple name and return it instead otherwise null
	 * 
	 * @param name
	 * @return
	 */
	public static DynamicTree findTree(String name) {
		DynamicTree tree = treesByFullName.get(name);
		
		if(tree == null) {
			tree = treesByName.get(name);
		}
		
		return tree;
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
	

}
