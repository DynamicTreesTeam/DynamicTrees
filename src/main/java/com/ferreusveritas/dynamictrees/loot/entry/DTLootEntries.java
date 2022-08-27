package com.ferreusveritas.dynamictrees.loot.entry;

import net.minecraft.loot.ILootSerializer;
import net.minecraft.loot.LootEntry;
import net.minecraft.loot.LootPoolEntryType;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.registry.Registry;

/**
 * @author Harley O'Connor
 */
public final class DTLootEntries {

    public static LootPoolEntryType ITEM_BY_SPECIES =
            register("dynamictrees:item_by_species", new ItemBySpeciesLootEntry.Serializer());
    public static LootPoolEntryType SEED_ITEM =
            register("dynamictrees:seed_item", new SeedItemLootEntry.Serializer());

    public static LootPoolEntryType WEIGHTED_ITEM =
            register("dynamictrees:weighted_item", new WeightedItemLootEntry.Serializer());

    private static LootPoolEntryType register(String name, ILootSerializer<? extends LootEntry> serializer) {
        return Registry.register(Registry.LOOT_POOL_ENTRY_TYPE, new ResourceLocation(name), new LootPoolEntryType(serializer));
    }

    /** Invoked to initialise static fields. */
    public static void load() {}

}
