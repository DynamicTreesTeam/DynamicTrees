package com.ferreusveritas.growingtrees;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import com.ferreusveritas.growingtrees.trees.GrowingTree;

/**
* A registry for all of the growing trees. For this mod or other mods.
* 
* @author ferreusveritas
*/
public class TreeRegistry {

	private static Map<String, GrowingTree> treesByName = new HashMap<String, GrowingTree>();
	private static ArrayList<GrowingTree> treesById = new ArrayList<GrowingTree>();

	public static GrowingTree registerTree(GrowingTree tree) {
		treesByName.put(tree.getName(), tree);
		int currId = treesById.size();
		treesById.add(tree);
		tree.register(currId);
		return tree;
	}

	public static GrowingTree findTree(String name) {
		return treesByName.get(name);
	}

	public static GrowingTree getTreeById(int id) {
		return treesById.get(id);
	}

	public static void registerAllTreeRecipes() {
		for(GrowingTree tree: treesByName.values()) {
			tree.registerRecipes();
		}
	}

	public static ArrayList<GrowingTree> getTrees() {
		return treesById;
	}
}
