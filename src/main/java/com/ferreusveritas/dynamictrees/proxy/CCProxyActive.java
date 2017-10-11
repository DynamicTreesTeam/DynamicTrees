package com.ferreusveritas.dynamictrees.proxy;

import com.ferreusveritas.dynamictrees.blocks.BlockDendroCoil;
import com.ferreusveritas.dynamictrees.util.GameRegistry;

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
		//No need to register ItemBlocks in 1.7.10
	}
	
}
