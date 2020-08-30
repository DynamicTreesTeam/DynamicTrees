package com.ferreusveritas.dynamictrees.growthlogic;

import com.ferreusveritas.dynamictrees.DynamicTrees;
import com.ferreusveritas.dynamictrees.api.TreeRegistry;
import com.ferreusveritas.dynamictrees.init.DTTrees;
import net.minecraft.util.ResourceLocation;

public class GrowthLogicKits {
	
	public final static NullLogic nullLogic = new NullLogic();
	
	public static void setup() {
		new GrowthLogicKits();
	}
	
	public GrowthLogicKits() {
		TreeRegistry.registerGrowthLogicKit(new ResourceLocation(DynamicTrees.MODID, DTTrees.NULL), new NullLogic());
		TreeRegistry.registerGrowthLogicKit(new ResourceLocation(DynamicTrees.MODID, DTTrees.DARKOAK), new DarkOakLogic());
		TreeRegistry.registerGrowthLogicKit(new ResourceLocation(DynamicTrees.MODID, DTTrees.CONIFER), new ConiferLogic());
		TreeRegistry.registerGrowthLogicKit(new ResourceLocation(DynamicTrees.MODID, DTTrees.JUNGLE), new JungleLogic());
	}
	
}
