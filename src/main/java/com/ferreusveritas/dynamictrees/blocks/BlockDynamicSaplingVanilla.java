package com.ferreusveritas.dynamictrees.blocks;

import java.util.HashMap;
import java.util.Map;

import com.ferreusveritas.dynamictrees.api.backport.IBlockState;
import com.ferreusveritas.dynamictrees.trees.Species;

public class BlockDynamicSaplingVanilla extends BlockDynamicSapling {

	protected Map<Integer, Species> trees = new HashMap<Integer, Species>();
	
	public BlockDynamicSaplingVanilla(String name) {
		super(name);
	}

	///////////////////////////////////////////
	// TREE INFORMATION
	///////////////////////////////////////////

	@Override
	public Species getSpecies(IBlockState state) {
		if(state.getBlock() == this) {
			return trees.get(state.getMeta());
		}
    	return trees.get(0);
	}

	@Override
	public BlockDynamicSaplingVanilla setSpecies(IBlockState state, Species species) {
		if(state.getBlock() == this) {
			trees.put(state.getMeta(), species);
		}
		return this;
	}

	///////////////////////////////////////////
	// BLOCKSTATES
	///////////////////////////////////////////
    
}
