package com.ferreusveritas.dynamictrees.proxy;

import net.minecraftforge.fml.common.Loader;

public class CCProxyBase {

	public static final String CCModId = "ComputerCraft";
	
	public static boolean hasComputerCraft() {
		return Loader.isModLoaded(CCModId) || Loader.isModLoaded(CCModId.toLowerCase());
	}
	
	public CCProxyBase() {}
	
	public void createBlocks() {}
	
	public void createItems() {}
	
	public void registerBlocks() {}
	
	public void registerItems() {}
}
