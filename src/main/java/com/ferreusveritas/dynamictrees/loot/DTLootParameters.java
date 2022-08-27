package com.ferreusveritas.dynamictrees.loot;

import com.ferreusveritas.dynamictrees.DynamicTrees;
import com.ferreusveritas.dynamictrees.systems.nodemappers.NetVolumeNode;
import com.ferreusveritas.dynamictrees.trees.Species;
import net.minecraft.loot.LootParameter;

/**
 * @author Harley O'Connor
 */
public final class DTLootParameters {

    public static final LootParameter<Species> SPECIES = create("species");
    public static final LootParameter<Integer> FERTILITY = create("fertility");
    public static final LootParameter<Float> SEASONAL_SEED_DROP_FACTOR = create("seasonal_seed_drop_factor");
    public static final LootParameter<Integer> VOLUME = create("volume");

    private static <T> LootParameter<T> create(String path) {
        return new LootParameter<>(DynamicTrees.resLoc(path));
    }

    /** Invoked to initialise static fields. */
    public static void load() {}

}
