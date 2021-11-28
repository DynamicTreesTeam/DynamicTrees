package com.ferreusveritas.dynamictrees.systems.dropcreators;

import com.ferreusveritas.dynamictrees.DynamicTrees;
import com.ferreusveritas.dynamictrees.api.registry.Registry;

/**
 * @author Harley O'Connor
 */
public final class DropCreators {

    public static final DropCreator NORMAL = new NormalDropCreator(DynamicTrees.resLoc("normal"));
    public static final DropCreator LOOT_TABLE = new LootTableDropCreator(DynamicTrees.resLoc("loot_table"));
    public static final DropCreator SEED = new SeedDropCreator(DynamicTrees.resLoc("seed"));
    public static final DropCreator FRUIT = new FruitDropCreator(DynamicTrees.resLoc("fruit"));
    public static final DropCreator STICK = new StickDropCreator(DynamicTrees.resLoc("stick"));
    public static final DropCreator LOG = new LogDropCreator(DynamicTrees.resLoc("log"));

    public static void register(final Registry<DropCreator> registry) {
        registry.registerAll(NORMAL, LOOT_TABLE, SEED, FRUIT, STICK, LOG);
    }

}
