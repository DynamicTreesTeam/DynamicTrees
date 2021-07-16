package com.ferreusveritas.dynamictrees.systems.dropcreators;

import com.ferreusveritas.dynamictrees.DynamicTrees;
import com.ferreusveritas.dynamictrees.api.registry.RegistryEvent;

/**
 * @author Harley O'Connor
 */
public final class DropCreators {

    public static final DropCreator NORMAL = new NormalDropCreator(DynamicTrees.resLoc("normal"));
    public static final DropCreator LOOT_TABLE = new LootTableDropCreator(DynamicTrees.resLoc("loot_table"));
    public static final DropCreator SEED = new SeedDropCreator(DynamicTrees.resLoc("seed"));
    public static final DropCreator FRUIT = new FruitDropCreator(DynamicTrees.resLoc("fruit"));
    public static final DropCreator STICKS = new SticksDropCreator(DynamicTrees.resLoc("sticks"));
    public static final DropCreator LOGS = new LogsDropCreator(DynamicTrees.resLoc("logs"));

    public void register(final RegistryEvent<DropCreator> event) {
        event.getRegistry().registerAll(NORMAL, LOOT_TABLE, SEED, FRUIT, STICKS, LOGS);
    }

}
