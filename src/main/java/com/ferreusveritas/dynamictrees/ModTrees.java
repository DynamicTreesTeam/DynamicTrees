package com.ferreusveritas.dynamictrees;

import java.util.ArrayList;
import java.util.Collections;

import com.ferreusveritas.dynamictrees.trees.DynamicTree;
import com.ferreusveritas.dynamictrees.trees.Species;
import com.ferreusveritas.dynamictrees.trees.TreeAcacia;
import com.ferreusveritas.dynamictrees.trees.TreeBirch;
import com.ferreusveritas.dynamictrees.trees.TreeDarkOak;
import com.ferreusveritas.dynamictrees.trees.TreeJungle;
import com.ferreusveritas.dynamictrees.trees.TreeOak;
import com.ferreusveritas.dynamictrees.trees.TreeSpruce;

public class ModTrees {

	public static ArrayList<DynamicTree> baseTrees = new ArrayList<DynamicTree>();

	/**
	 * Pay Attn! This should be run after the Dynamic Trees Mod
	 * has created it's Blocks and Items.  These trees depend
	 * on the Dynamic Sapling
	 */
	public static void preInit() {
		Collections.addAll(baseTrees, new TreeOak(), new TreeSpruce(), new TreeBirch(), new TreeJungle(), new TreeAcacia(), new TreeDarkOak());
		baseTrees.forEach(tree -> tree.registerSpecies(Species.REGISTRY));
	}
	
}
