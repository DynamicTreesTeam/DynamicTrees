package com.ferreusveritas.dynamictrees.compat;

import com.ferreusveritas.dynamictrees.blocks.BlockDynamicLeaves;

public class CommonProxyCompat {

	public CCProxyBase ccproxy;
	
	public void preInit() {
		BlockDynamicLeaves.passableLeavesModLoaded = net.minecraftforge.fml.common.Loader.isModLoaded("passableleaves");
		
		//Computercraft Creative Mode Stuff
		ccproxy = CCProxyBase.hasComputerCraft() ? new CCProxyActive() : new CCProxyBase();
		ccproxy.createBlocks();
	}

	public void init() {}
	
	public void registerBlocks() {
		ccproxy.registerBlocks();
	}
	
	public void registerItems() {
		ccproxy.registerItems();		
	}

	public void registerRecipes() {
		ccproxy.registerRecipes();
	}
	
}
