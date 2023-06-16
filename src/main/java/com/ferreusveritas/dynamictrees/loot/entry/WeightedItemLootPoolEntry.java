package com.ferreusveritas.dynamictrees.loot.entry;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSyntaxException;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.util.random.SimpleWeightedRandomList;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.entries.LootPoolEntryType;
import net.minecraft.world.level.storage.loot.entries.LootPoolSingletonContainer;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;

/**
 * @author Harley O'Connor
 */
public final class WeightedItemLootPoolEntry extends LootPoolSingletonContainer {

    private final SimpleWeightedRandomList<Item> items;

    public WeightedItemLootPoolEntry(SimpleWeightedRandomList<Item> items, int weight, int quality, LootItemCondition[] conditions,
                                     LootItemFunction[] functions) {
        super(weight, quality, conditions, functions);
        this.items = items;
    }

    @Override
    public LootPoolEntryType getType() {
        return DTLootPoolEntries.WEIGHTED_ITEM.get();
    }

    @Override
    protected void createItemStack(Consumer<ItemStack> stackConsumer, LootContext lootContext) {
        items.getRandomValue(lootContext.getRandom()).ifPresent(item -> stackConsumer.accept(new ItemStack(item)));
    }

    public static LootPoolSingletonContainer.Builder<?> weightedLootTableItem(SimpleWeightedRandomList<Item> items) {
        return simpleBuilder((weight, quality, conditions, functions) ->
                new WeightedItemLootPoolEntry(items, weight, quality, conditions, functions)
        );
    }

    public static class Serializer extends LootPoolSingletonContainer.Serializer<WeightedItemLootPoolEntry> {
        @Override
        public void serializeCustom(JsonObject json, WeightedItemLootPoolEntry value, JsonSerializationContext conditions) {
            super.serializeCustom(json, value, conditions);
            JsonObject weightedItemsJson = new JsonObject();
            value.items.items.forEach(entry ->
                    weightedItemsJson.addProperty(String.valueOf(ForgeRegistries.ITEMS.getKey(entry.data)), entry.getWeight().asInt())
            );
            json.add("items", weightedItemsJson);
        }

        @Override
        protected WeightedItemLootPoolEntry deserialize(JsonObject json, JsonDeserializationContext context, int weight,
                                                        int quality, LootItemCondition[] conditions, LootItemFunction[] functions) {
            JsonObject weightedItemsJson = GsonHelper.getAsJsonObject(json, "items");
            SimpleWeightedRandomList.Builder<Item> items = new SimpleWeightedRandomList.Builder<>();
            for (Map.Entry<String, JsonElement> itemEntry : weightedItemsJson.entrySet()) {
                String name = itemEntry.getKey();
                Item item = Optional.ofNullable(ForgeRegistries.ITEMS.getValue(new ResourceLocation(name)))
                        .orElseThrow(() -> new JsonSyntaxException("Expected key to be an item, was unknown string '" + name + "'"));
                int itemWeight = GsonHelper.convertToInt(itemEntry.getValue(), name);
                items.add(item, itemWeight);
            }
            return new WeightedItemLootPoolEntry(items.build(), weight, quality, conditions, functions);
        }
    }

}
