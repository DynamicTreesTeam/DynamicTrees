package com.ferreusveritas.dynamictrees.loot.entry;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSyntaxException;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.loot.LootContext;
import net.minecraft.loot.LootPoolEntryType;
import net.minecraft.loot.StandaloneLootEntry;
import net.minecraft.loot.conditions.ILootCondition;
import net.minecraft.loot.functions.ILootFunction;
import net.minecraft.util.JSONUtils;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.WeightedList;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;

/**
 * @author Harley O'Connor
 */
public final class WeightedItemLootEntry extends StandaloneLootEntry {

    private final WeightedList<Item> items;

    public WeightedItemLootEntry(WeightedList<Item> items, int weight, int quality, ILootCondition[] conditions,
                                 ILootFunction[] functions) {
        super(weight, quality, conditions, functions);
        this.items = items;
    }

    @Override
    public LootPoolEntryType getType() {
        return DTLootEntries.WEIGHTED_ITEM;
    }

    @Override
    protected void createItemStack(Consumer<ItemStack> stackConsumer, LootContext lootContext) {
        stackConsumer.accept(new ItemStack(items.getOne(lootContext.getRandom())));
    }

    public static StandaloneLootEntry.Builder<?> weightedLootTableItem(WeightedList<Item> items) {
        return simpleBuilder((weight, quality, conditions, functions) ->
                new WeightedItemLootEntry(items, weight, quality, conditions, functions)
        );
    }

    public static class Serializer extends StandaloneLootEntry.Serializer<WeightedItemLootEntry> {
        public void serializeCustom(JsonObject json, WeightedItemLootEntry value, JsonSerializationContext conditions) {
            super.serializeCustom(json, value, conditions);
            JsonObject weightedItemsJson = new JsonObject();
            value.items.entries.forEach(entry ->
                    weightedItemsJson.addProperty(String.valueOf(entry.data.getRegistryName()), entry.weight)
            );
            json.add("items", weightedItemsJson);
        }

        protected WeightedItemLootEntry deserialize(JsonObject json, JsonDeserializationContext context, int weight,
                                            int quality, ILootCondition[] conditions, ILootFunction[] functions) {
            JsonObject weightedItemsJson = JSONUtils.getAsJsonObject(json, "items");
            WeightedList<Item> items = new WeightedList<>();
            for (Map.Entry<String, JsonElement> itemEntry : weightedItemsJson.entrySet()) {
                String name = itemEntry.getKey();
                Item item = Optional.ofNullable(ForgeRegistries.ITEMS.getValue(new ResourceLocation(name)))
                        .orElseThrow(() -> new JsonSyntaxException("Expected key to be an item, was unknown string '" + name + "'"));
                int itemWeight = JSONUtils.convertToInt(itemEntry.getValue(), name);
                items.add(item, itemWeight);
            }
            return new WeightedItemLootEntry(items, weight, quality, conditions, functions);
        }
    }

}
