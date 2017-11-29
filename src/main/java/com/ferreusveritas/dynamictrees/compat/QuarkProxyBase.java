package com.ferreusveritas.dynamictrees.compat;

import net.minecraft.block.Block;
import net.minecraftforge.fml.common.Loader;

public class QuarkProxyBase {

	public static final String QuarkModId = "quark";
	
	public static boolean hasQuark() {
		return Loader.isModLoaded(QuarkModId);
	}
	
	public void init() {}
	
	public void registerGreenerColor(Block block) {}
	
}
