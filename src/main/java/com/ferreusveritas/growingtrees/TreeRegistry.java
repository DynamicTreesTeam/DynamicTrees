package com.ferreusveritas.growingtrees;

import java.util.HashMap;
import java.util.Map;

import com.ferreusveritas.growingtrees.trees.GrowingTree;

import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;

/**
 * A registry for all of the growing trees. For this mod or other mods.
 * 
 * @author ferreusveritas
 */
public class TreeRegistry {

	private static Map<String, GrowingTree> trees = new HashMap<String, GrowingTree>();
	
	public static GrowingTree registerTree(GrowingTree tree){
		trees.put(tree.getName(), tree);
		tree.register();
		return tree;
	}

	public static GrowingTree findTree(String name){
		return trees.get(name);
	}
	
	public static void registerAllTreeRecipes(){
		for(GrowingTree tree: trees.values()){
			tree.registerRecipes();
		}
	}
	
}
