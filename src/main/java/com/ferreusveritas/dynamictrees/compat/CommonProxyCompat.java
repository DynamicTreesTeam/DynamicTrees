package com.ferreusveritas.dynamictrees.compat;

public class CommonProxyCompat {

	public CCProxyBase ccproxy;
	
	public void preInit() {
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
