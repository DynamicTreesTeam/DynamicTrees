package com.ferreusveritas.dynamictrees.growthlogic;

import com.ferreusveritas.dynamictrees.ModConstants;
import com.ferreusveritas.dynamictrees.ModTrees;
import com.ferreusveritas.dynamictrees.api.TreeRegistry;

import net.minecraft.util.ResourceLocation;

public class GrowthLogicKits {
	
	public final static NullLogic nullLogic = new NullLogic();
	
	public static void preInit() {
		new GrowthLogicKits();
	}
	
	public GrowthLogicKits() {
		TreeRegistry.registerGrowthLogicKit(new ResourceLocation(ModConstants.MODID, ModTrees.NULL), new NullLogic());
		TreeRegistry.registerGrowthLogicKit(new ResourceLocation(ModConstants.MODID, ModTrees.DARKOAK), new DarkOakLogic());
		TreeRegistry.registerGrowthLogicKit(new ResourceLocation(ModConstants.MODID, ModTrees.CONIFER), new ConiferLogic());
		TreeRegistry.registerGrowthLogicKit(new ResourceLocation(ModConstants.MODID, ModTrees.JUNGLE), new JungleLogic());
	}
	
}
