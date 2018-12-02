package com.ferreusveritas.dynamictrees;

import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class ModModels {
	
	@SideOnly(Side.CLIENT)
	public static void register(ModelRegistryEvent event) {
		DynamicTrees.proxy.registerModels();
	}
	
}