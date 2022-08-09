package com.ferreusveritas.dynamictrees.loot.entry;

import com.ferreusveritas.dynamictrees.loot.DTLootParameters;
import com.ferreusveritas.dynamictrees.trees.Species;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import net.minecraft.item.ItemStack;
import net.minecraft.loot.LootContext;
import net.minecraft.loot.LootPoolEntryType;
import net.minecraft.loot.StandaloneLootEntry;
import net.minecraft.loot.conditions.ILootCondition;
import net.minecraft.loot.functions.ILootFunction;

import java.util.function.Consumer;

/**
 * @author Harley O'Connor
 */
public final class SeedItemLootEntry extends StandaloneLootEntry {

    public SeedItemLootEntry(int weight, int quality, ILootCondition[] conditions, ILootFunction[] functions) {
        super(weight, quality, conditions, functions);
    }

    @Override
    public LootPoolEntryType getType() {
        return DTLootEntries.SEED_ITEM;
    }

    @Override
    protected void createItemStack(Consumer<ItemStack> stackConsumer, LootContext context) {
        final Species species = context.getParamOrNull(DTLootParameters.SPECIES);
        assert species != null;
        stackConsumer.accept(species.getSeedStack(1));
    }

    public static StandaloneLootEntry.Builder<?> lootTableSeedItem() {
        return simpleBuilder(SeedItemLootEntry::new);
    }

    public static class Serializer extends StandaloneLootEntry.Serializer<SeedItemLootEntry> {
        @Override
        protected SeedItemLootEntry deserialize(JsonObject json, JsonDeserializationContext context,
                                                int weight, int quality, ILootCondition[] conditions,
                                                ILootFunction[] functions) {
            return new SeedItemLootEntry(weight, quality, conditions, functions);
        }
    }
}
