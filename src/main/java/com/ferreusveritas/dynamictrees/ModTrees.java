package com.ferreusveritas.dynamictrees;

import java.util.ArrayList;

import com.ferreusveritas.dynamictrees.api.TreeRegistry;
import com.ferreusveritas.dynamictrees.trees.DynamicTree;
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
	 * has registered it's Blocks and Items.  These trees depend
	 * on them.
	 */
	public static void preInit() {
		//Trees
		baseTrees.add(new TreeOak());
		baseTrees.add(new TreeSpruce());
		baseTrees.add(new TreeBirch());
		baseTrees.add(new TreeJungle());
		baseTrees.add(new TreeAcacia());
		baseTrees.add(new TreeDarkOak());
		
		//Register Trees
		TreeRegistry.registerTrees(baseTrees);
	}
	
}
