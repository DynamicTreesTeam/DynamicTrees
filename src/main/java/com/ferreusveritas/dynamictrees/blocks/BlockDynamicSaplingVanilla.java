package com.ferreusveritas.dynamictrees.blocks;

import java.util.HashMap;
import java.util.Map;

import com.ferreusveritas.dynamictrees.api.backport.IBlockState;
import com.ferreusveritas.dynamictrees.trees.DynamicTree;

public class BlockDynamicSaplingVanilla extends BlockDynamicSapling {

	protected Map<Integer, DynamicTree> trees = new HashMap<Integer, DynamicTree>();
	
	public BlockDynamicSaplingVanilla(String name) {
		super(name);
	}

	///////////////////////////////////////////
	// TREE INFORMATION
	///////////////////////////////////////////

	@Override
	public DynamicTree getTree(IBlockState state) {
		if(state.getBlock() == this) {
			return trees.get(state.getMeta());
		}
    	return trees.get(0);
	}

	@Override
	public BlockDynamicSaplingVanilla setTree(IBlockState state, DynamicTree tree) {
		if(state.getBlock() == this) {
			trees.put(state.getMeta(), tree);
		}
		return this;
	}

	///////////////////////////////////////////
	// BLOCKSTATES
	///////////////////////////////////////////
    
}
