package com.ferreusveritas.dynamictrees.loot.entry;

import com.ferreusveritas.dynamictrees.loot.DTLootParameters;
import com.ferreusveritas.dynamictrees.trees.Species;
import com.google.common.collect.Maps;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.loot.LootContext;
import net.minecraft.loot.LootPoolEntryType;
import net.minecraft.loot.StandaloneLootEntry;
import net.minecraft.loot.conditions.ILootCondition;
import net.minecraft.loot.functions.ILootFunction;
import net.minecraft.util.JSONUtils;
import net.minecraft.util.ResourceLocation;

import java.util.Map;
import java.util.function.Consumer;

/**
 * @author Harley O'Connor
 */
public final class ItemBySpeciesLootEntry extends StandaloneLootEntry {

    /** Map of items to set, keyed by the name of the species of tree. */
    private final Map<ResourceLocation, Item> items;

    public ItemBySpeciesLootEntry(int weight, int quality, ILootCondition[] conditions,
                                  ILootFunction[] functions,
                                  Map<ResourceLocation, Item> items) {
        super(weight, quality, conditions, functions);
        this.items = items;
    }

    @Override
    public LootPoolEntryType getType() {
        return DTLootEntries.ITEM_BY_SPECIES;
    }

    @Override
    protected void createItemStack(Consumer<ItemStack> stackConsumer, LootContext context) {
        final Species species = context.getParamOrNull(DTLootParameters.SPECIES);
        assert species != null;
        Item item = items.get(species.getRegistryName());
        if (item == null) {
            item = Items.AIR;
        }
        stackConsumer.accept(new ItemStack(item));
    }

    public static class Serializer extends StandaloneLootEntry.Serializer<ItemBySpeciesLootEntry> {
        @Override
        public void serializeCustom(JsonObject json, ItemBySpeciesLootEntry value, JsonSerializationContext context) {
            super.serialize(json, value, context);
            final JsonObject itemsJson = new JsonObject();
            for (Map.Entry<ResourceLocation, Item> itemEntry : value.items.entrySet()) {
                itemsJson.add(itemEntry.getKey().toString(), new JsonPrimitive(itemEntry.getValue().getRegistryName().toString()));
            }
            json.add("name_by_species", itemsJson);
        }

        @Override
        protected ItemBySpeciesLootEntry deserialize(JsonObject json, JsonDeserializationContext context,
                                                     int weight, int quality, ILootCondition[] conditions,
                                                     ILootFunction[] functions) {
            final JsonObject namesJson = JSONUtils.getAsJsonObject(json, "name_by_species");
            final Map<ResourceLocation, Item> items = Maps.newHashMap();
            for (Map.Entry<String, JsonElement> itemEntry : namesJson.entrySet()) {
                items.put(new ResourceLocation(itemEntry.getKey()), JSONUtils.convertToItem(itemEntry.getValue(), itemEntry.getKey()));
            }
            return new ItemBySpeciesLootEntry(weight, quality, conditions, functions, items);
        }
    }
}
