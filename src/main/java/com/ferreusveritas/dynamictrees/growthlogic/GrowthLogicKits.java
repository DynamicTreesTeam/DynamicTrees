package com.ferreusveritas.dynamictrees.growthlogic;

import com.ferreusveritas.dynamictrees.DynamicTrees;
import com.ferreusveritas.dynamictrees.api.registry.IRegistry;

public class GrowthLogicKits {

    public static final GrowthLogicKit DARK_OAK = new DarkOakLogic(DynamicTrees.resLoc("dark_oak"));
    public static final GrowthLogicKit CONIFER = new ConiferLogic(DynamicTrees.resLoc("conifer"));
    public static final GrowthLogicKit MEGA_CONIFER = new ConiferLogic(DynamicTrees.resLoc("mega_conifer"), 5);
    public static final GrowthLogicKit JUNGLE = new JungleLogic(DynamicTrees.resLoc("jungle"));
    public static final GrowthLogicKit NETHER_FUNGUS = new NetherFungusLogic(DynamicTrees.resLoc("nether_fungus"));
    public static final GrowthLogicKit PALM = new PalmGrowthLogic(DynamicTrees.resLoc("palm"));

    public static void register(final IRegistry<GrowthLogicKit> registry) {
        registry.registerAll(DARK_OAK, CONIFER, MEGA_CONIFER, JUNGLE, NETHER_FUNGUS, PALM);
    }

}
