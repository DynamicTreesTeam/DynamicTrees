package com.ferreusveritas.dynamictrees.loot.entry;

import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.storage.loot.Serializer;
import net.minecraft.world.level.storage.loot.entries.LootPoolEntryContainer;
import net.minecraft.world.level.storage.loot.entries.LootPoolEntryType;

/**
 * @author Harley O'Connor
 */
public final class DTLootPoolEntries {

    public static LootPoolEntryType ITEM_BY_SPECIES =
            register("dynamictrees:item_by_species", new ItemBySpeciesLootPoolEntry.Serializer());
    public static LootPoolEntryType SEED_ITEM =
            register("dynamictrees:seed_item", new SeedItemLootPoolEntry.Serializer());

    public static LootPoolEntryType WEIGHTED_ITEM =
            register("dynamictrees:weighted_item", new WeightedItemLootPoolEntry.Serializer());

    private static LootPoolEntryType register(String name, Serializer<? extends LootPoolEntryContainer> serializer) {
        return Registry.register(Registry.LOOT_POOL_ENTRY_TYPE, new ResourceLocation(name), new LootPoolEntryType(serializer));
    }

    /**
     * Invoked to initialise static fields.
     */
    public static void load() {
    }

}
