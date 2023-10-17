package com.ferreusveritas.dynamictrees.loot.entry;

import com.ferreusveritas.dynamictrees.DynamicTrees;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.storage.loot.Serializer;
import net.minecraft.world.level.storage.loot.entries.LootPoolEntryContainer;
import net.minecraft.world.level.storage.loot.entries.LootPoolEntryType;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

import java.util.function.Supplier;

/**
 * @author Harley O'Connor
 */
public final class DTLootPoolEntries {
    public static final DeferredRegister<LootPoolEntryType> LOOT_POOL_ENTRY_TYPES = DeferredRegister.create(Registries.LOOT_POOL_ENTRY_TYPE, DynamicTrees.MOD_ID);
    public static final RegistryObject<LootPoolEntryType> ITEM_BY_SPECIES =
            register("item_by_species", ItemBySpeciesLootPoolEntry.Serializer::new);
    public static final RegistryObject<LootPoolEntryType> SEED_ITEM =
            register("seed_item", SeedItemLootPoolEntry.Serializer::new);
    public static final RegistryObject<LootPoolEntryType> WEIGHTED_ITEM =
            register("weighted_item", WeightedItemLootPoolEntry.Serializer::new);

    private static RegistryObject<LootPoolEntryType> register(String name, Supplier<Serializer<? extends LootPoolEntryContainer>> serializerFactory) {
        return LOOT_POOL_ENTRY_TYPES.register(name, () -> new LootPoolEntryType(serializerFactory.get()));
    }
}
