package com.ferreusveritas.dynamictrees.growthlogic;

import com.ferreusveritas.dynamictrees.DynamicTrees;
import com.ferreusveritas.dynamictrees.api.registry.Registry;

public class GrowthLogicKits {

    public static final GrowthLogicKit DARK_OAK = new DarkOakLogic(DynamicTrees.resLoc("dark_oak"));
    public static final GrowthLogicKit CONIFER = new ConiferLogic(DynamicTrees.resLoc("conifer"));
    public static final GrowthLogicKit JUNGLE = new JungleLogic(DynamicTrees.resLoc("jungle"));
    public static final GrowthLogicKit NETHER_FUNGUS = new NetherFungusLogic(DynamicTrees.resLoc("nether_fungus"));
    public static final GrowthLogicKit PALM = new PalmGrowthLogic(DynamicTrees.resLoc("palm"));

    public static void register(final Registry<GrowthLogicKit> registry) {
        registry.registerAll(DARK_OAK, CONIFER, JUNGLE, NETHER_FUNGUS, PALM);
    }

}
