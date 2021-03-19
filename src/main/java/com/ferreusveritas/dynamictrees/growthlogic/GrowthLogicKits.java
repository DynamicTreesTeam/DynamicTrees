package com.ferreusveritas.dynamictrees.growthlogic;

import com.ferreusveritas.dynamictrees.DynamicTrees;
import com.ferreusveritas.dynamictrees.api.registry.Registry;

public class GrowthLogicKits {
	
	public static final GrowthLogicKit DARK_OAK = new DarkOakLogic(DynamicTrees.resLoc("dark_oak"));
	public static final GrowthLogicKit CONIFER = new ConiferLogic(DynamicTrees.resLoc("conifer"));
	public static final GrowthLogicKit MEGA_CONIFER = new ConiferLogic(DynamicTrees.resLoc("mega_conifer"), 5);
	public static final GrowthLogicKit JUNGLE = new JungleLogic(DynamicTrees.resLoc("jungle"));

	public static void register(final Registry<GrowthLogicKit> registry) {
		registry.registerAll(DARK_OAK, CONIFER, MEGA_CONIFER, JUNGLE);
	}

}
