package com.ferreusveritas.dynamictrees.systems.dropcreators;

import com.ferreusveritas.dynamictrees.DynamicTrees;
import com.ferreusveritas.dynamictrees.api.registry.Registry;

/**
 * @author Harley O'Connor
 */
public final class DropCreators {

    public static final DropCreator NORMAL = new NormalDropCreator(DynamicTrees.location("normal"));
    public static final DropCreator LOOT_TABLE = new LootTableDropCreator(DynamicTrees.location("loot_table"));
    public static final DropCreator SEED = new SeedDropCreator(DynamicTrees.location("seed"));
    public static final DropCreator FRUIT = new FruitDropCreator(DynamicTrees.location("fruit"));
    public static final DropCreator STICK = new StickDropCreator(DynamicTrees.location("stick"));
    public static final DropCreator LOG = new LogDropCreator(DynamicTrees.location("log"));

    public static void register(final Registry<DropCreator> registry) {
        registry.registerAll(NORMAL, LOOT_TABLE, SEED, FRUIT, STICK, LOG);
    }

}
