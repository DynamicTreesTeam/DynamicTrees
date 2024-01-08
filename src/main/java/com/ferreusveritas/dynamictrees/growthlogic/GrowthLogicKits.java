package com.ferreusveritas.dynamictrees.growthlogic;

import com.ferreusveritas.dynamictrees.DynamicTrees;
import com.ferreusveritas.dynamictrees.api.registry.Registry;

public class GrowthLogicKits {

    //Tree logic kits
    public static final GrowthLogicKit DARK_OAK = new DarkOakLogic(DynamicTrees.location("dark_oak"));
    public static final GrowthLogicKit CONIFER = new ConiferLogic(DynamicTrees.location("conifer"));
    public static final GrowthLogicKit JUNGLE = new JungleLogic(DynamicTrees.location("jungle"));
    public static final GrowthLogicKit AZALEA = new AzaleaLogic(DynamicTrees.location("azalea"));
    public static final GrowthLogicKit NETHER_FUNGUS = new NetherFungusLogic(DynamicTrees.location("nether_fungus"));
    public static final GrowthLogicKit PALM = new PalmGrowthLogic(DynamicTrees.location("palm"));

    //Root logic kits
    public static final GrowthLogicKit MANGROVE_ROOTS = new MangroveRootsLogic(DynamicTrees.location("mangrove_roots"));

    public static void register(final Registry<GrowthLogicKit> registry) {
        //Tree logic kits
        registry.registerAll(DARK_OAK, CONIFER, JUNGLE, AZALEA, NETHER_FUNGUS, PALM);
        //Root logic kits
        registry.registerAll(MANGROVE_ROOTS);
    }

}
