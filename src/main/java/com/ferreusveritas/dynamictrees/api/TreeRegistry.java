package com.ferreusveritas.dynamictrees.api;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import com.ferreusveritas.dynamictrees.api.backport.BlockPos;
import com.ferreusveritas.dynamictrees.api.treedata.IBiomeSuitabilityDecider;
import com.ferreusveritas.dynamictrees.trees.DynamicTree;

import net.minecraft.world.World;

/**
* A registry for all of the dynamic trees. For this mod or other mods.
* 
* @author ferreusveritas
*/
public class TreeRegistry {

	private static Map<String, DynamicTree> treesByName = new HashMap<String, DynamicTree>();
	private static ArrayList<DynamicTree> treesById = new ArrayList<DynamicTree>();
	private static ArrayList<IBiomeSuitabilityDecider> biomeSuitabilityDeciders;
	private static boolean doBiomeSuitabilityOverride;
	
	/**
	 * All mods that depend on this mod must register their own trees
	 * 
	 * @param tree
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
	
	public static void addBiomeSuitabilityDecider(IBiomeSuitabilityDecider decider) {
		doBiomeSuitabilityOverride = true;
		biomeSuitabilityDeciders.add(decider);
	}
	
	private static final IBiomeSuitabilityDecider.Decision unhandledBiomeSuitability = new IBiomeSuitabilityDecider.Decision();
	
	public static IBiomeSuitabilityDecider.Decision getBiomeSuitability(World world, DynamicTree tree, BlockPos pos) {
		for(IBiomeSuitabilityDecider decider: biomeSuitabilityDeciders) {
			IBiomeSuitabilityDecider.Decision decision = decider.getBiomeSuitability(world, tree, pos);
			if(decision.isHandled()) {
				return decision;
			}
		}
		
		return unhandledBiomeSuitability;
	}
	
	public static boolean isBiomeSuitabilityOverrideEnabled() {
		return doBiomeSuitabilityOverride;
	}
}
