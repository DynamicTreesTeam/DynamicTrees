package com.ferreusveritas.dynamictrees.loot.entry;

import com.ferreusveritas.dynamictrees.loot.DTLootContextParams;
import com.ferreusveritas.dynamictrees.tree.species.Species;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.entries.LootPoolEntryContainer;
import net.minecraft.world.level.storage.loot.entries.LootPoolEntryType;
import net.minecraft.world.level.storage.loot.entries.LootPoolSingletonContainer;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

import java.util.function.Consumer;

/**
 * @author Harley O'Connor
 */
public final class SeedItemLootPoolEntry extends LootPoolSingletonContainer {

    public SeedItemLootPoolEntry(int weight, int quality, LootItemCondition[] conditions, LootItemFunction[] functions) {
        super(weight, quality, conditions, functions);
    }

    @Override
    public LootPoolEntryType getType() {
        return DTLootPoolEntries.SEED_ITEM;
    }

    @Override
    protected void createItemStack(Consumer<ItemStack> stackConsumer, LootContext context) {
        final Species species = context.getParamOrNull(DTLootContextParams.SPECIES);
        assert species != null;
        stackConsumer.accept(species.shouldDropSeeds() ? species.getSeedStack(1) : ItemStack.EMPTY);
    }

    public static LootPoolSingletonContainer.Builder<?> lootTableSeedItem() {
        return simpleBuilder(SeedItemLootPoolEntry::new);
    }

    public static class Serializer extends LootPoolSingletonContainer.Serializer<SeedItemLootPoolEntry> {
        @Override
        protected SeedItemLootPoolEntry deserialize(JsonObject json, JsonDeserializationContext context,
                                                    int weight, int quality, LootItemCondition[] conditions,
                                                    LootItemFunction[] functions) {
            return new SeedItemLootPoolEntry(weight, quality, conditions, functions);
        }
    }
}
