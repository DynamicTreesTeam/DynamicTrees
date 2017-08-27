package com.ferreusveritas.dynamictrees;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import com.ferreusveritas.dynamictrees.trees.DynamicTree;

/**
* A registry for all of the growing trees. For this mod or other mods.
* 
* @author ferreusveritas
*/
public class TreeRegistry {

	private static Map<String, DynamicTree> treesByName = new HashMap<String, DynamicTree>();
	private static ArrayList<DynamicTree> treesById = new ArrayList<DynamicTree>();

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

	public static void registerAllTreeRecipes() {
		for(DynamicTree tree: treesByName.values()) {
			tree.registerRecipes();
		}
	}

	public static ArrayList<DynamicTree> getTrees() {
		return treesById;
	}
}
