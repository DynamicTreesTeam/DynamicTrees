package com.ferreusveritas.dynamictrees.data;

import com.ferreusveritas.dynamictrees.DynamicTrees;
import com.ferreusveritas.dynamictrees.trees.Species;
import net.minecraft.loot.LootParameter;

/**
 * @author Harley O'Connor
 */
public final class DTLootParameters {

    public static final LootParameter<Species> SPECIES = create("species");
    public static final LootParameter<Integer> FERTILITY = create("fertility");
    public static final LootParameter<Integer> FORTUNE = create("fortune");
    public static final LootParameter<Species.LogsAndSticks> LOGS_AND_STICKS = create("logs_and_sticks");

    private static <T> LootParameter<T> create(String path) {
        return new LootParameter<>(DynamicTrees.resLoc(path));
    }

}
