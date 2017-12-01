package com.ferreusveritas.dynamictrees.compat;

import com.ferreusveritas.dynamictrees.api.backport.GameRegistry;
import com.ferreusveritas.dynamictrees.blocks.BlockDendroCoil;

public class CCProxyActive extends CCProxyBase {

	BlockDendroCoil blockDendroCoil;
	
	public CCProxyActive() {}
	
	@Override
	public void createBlocks() {
		blockDendroCoil = new BlockDendroCoil();
	}
	
	@Override
	public void createItems() {}
	
	@Override
	public void registerBlocks() {
		GameRegistry.register(blockDendroCoil);
	}
	
	@Override
	public void registerItems() {
		//We don't need to register ItemBlocks in 1.7.10
	}
	
	@Override
	public void registerRecipes() {}

}
