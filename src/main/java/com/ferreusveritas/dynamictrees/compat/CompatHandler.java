package com.ferreusveritas.dynamictrees.compat;

import com.ferreusveritas.dynamictrees.ModConstants;

import net.minecraftforge.fml.common.Loader;

public class CompatHandler {
	
	public static void HandleCompat() {
		
		if(Loader.isModLoaded(ModConstants.SERENESEASONS)) {
			try {
				Class<?> clazz = Class.forName("com.ferreusveritas.dynamictrees.compat.SereneSeasonsAdapter");
				ICompatAdapter ssAdapter = (ICompatAdapter) clazz.newInstance();
				ssAdapter.Init();
			} catch (ClassNotFoundException | InstantiationException | IllegalAccessException e) {
				e.printStackTrace();
			}
		}
		
		
	}
	
}
