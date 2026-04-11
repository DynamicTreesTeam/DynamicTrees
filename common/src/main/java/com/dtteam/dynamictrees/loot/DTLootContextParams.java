package com.dtteam.dynamictrees.loot;

import com.dtteam.dynamictrees.DynamicTrees;
import com.dtteam.dynamictrees.tree.species.Species;
import net.minecraft.util.context.ContextKey;

/**
 * @author Harley O'Connor
 */
public final class DTLootContextParams {

    public static final ContextKey<Species> SPECIES = create("species");
    public static final ContextKey<Integer> FERTILITY = create("fertility");
    public static final ContextKey<Float> SEASONAL_SEED_DROP_FACTOR = create("seasonal_seed_drop_factor");
    public static final ContextKey<Integer> VOLUME = create("volume");

    private static <T> ContextKey<T> create(String path) {
        return new ContextKey<>(DynamicTrees.location(path));
    }

    /** Invoked to initialise static fields. */
    public static void load() {}

}
