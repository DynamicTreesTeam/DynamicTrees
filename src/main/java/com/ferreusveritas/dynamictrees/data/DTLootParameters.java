package com.ferreusveritas.dynamictrees.data;

import com.ferreusveritas.dynamictrees.DynamicTrees;
import com.ferreusveritas.dynamictrees.tree.species.Species;
import net.minecraft.world.level.storage.loot.parameters.LootContextParam;

/**
 * @author Harley O'Connor
 */
public final class DTLootParameters {

    public static final LootContextParam<Species> SPECIES = create("species");
    public static final LootContextParam<Integer> FERTILITY = create("fertility");
    public static final LootContextParam<Integer> FORTUNE = create("fortune");
    public static final LootContextParam<Species.LogsAndSticks> LOGS_AND_STICKS = create("logs_and_sticks");

    private static <T> LootContextParam<T> create(String path) {
        return new LootContextParam<>(DynamicTrees.location(path));
    }

}
