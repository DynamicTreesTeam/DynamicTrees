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
		TreeRegistry.registerGrowthLogicKit(new ResourceLocation(DynamicTrees.MOD_ID, DTTrees.NULL), new NullLogic());
		TreeRegistry.registerGrowthLogicKit(new ResourceLocation(DynamicTrees.MOD_ID, DTTrees.DARK_OAK), new DarkOakLogic());
		TreeRegistry.registerGrowthLogicKit(new ResourceLocation(DynamicTrees.MOD_ID, DTTrees.CONIFER), new ConiferLogic());
		TreeRegistry.registerGrowthLogicKit(new ResourceLocation(DynamicTrees.MOD_ID, DTTrees.JUNGLE), new JungleLogic());
	}
	
}
