package com.ferreusveritas.dynamictrees.growthlogic;

import com.ferreusveritas.dynamictrees.DynamicTrees;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.registries.IForgeRegistry;

public class GrowthLogicKits {
	
	public static final GrowthLogicKit NULL = new NullLogic();
	public static final GrowthLogicKit DARK_OAK = new DarkOakLogic(new ResourceLocation(DynamicTrees.MOD_ID, "dark_oak"));
	public static final GrowthLogicKit CONIFER = new ConiferLogic(new ResourceLocation(DynamicTrees.MOD_ID, "conifer"));
	public static final GrowthLogicKit MEGA_CONIFER = new ConiferLogic(new ResourceLocation(DynamicTrees.MOD_ID, "mega_conifer"), 5);
	public static final GrowthLogicKit JUNGLE = new JungleLogic(new ResourceLocation(DynamicTrees.MOD_ID, "jungle"));

	public static void register(final IForgeRegistry<GrowthLogicKit> registry) {
		registry.registerAll(NULL, DARK_OAK, CONIFER, MEGA_CONIFER, JUNGLE);
	}

}
