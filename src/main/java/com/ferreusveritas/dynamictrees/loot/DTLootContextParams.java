package com.ferreusveritas.dynamictrees.loot;

import com.ferreusveritas.dynamictrees.DynamicTrees;
import com.ferreusveritas.dynamictrees.tree.species.Species;
import net.minecraft.world.level.storage.loot.parameters.LootContextParam;

/**
 * @author Harley O'Connor
 */
public final class DTLootContextParams {

    public static final LootContextParam<Species> SPECIES = create("species");
    public static final LootContextParam<Integer> FERTILITY = create("fertility");
    public static final LootContextParam<Float> SEASONAL_SEED_DROP_FACTOR = create("seasonal_seed_drop_factor");
    public static final LootContextParam<Integer> VOLUME = create("volume");

    private static <T> LootContextParam<T> create(String path) {
        return new LootContextParam<>(DynamicTrees.location(path));
    }

    /** Invoked to initialise static fields. */
    public static void load() {}

}
