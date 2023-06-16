package com.ferreusveritas.dynamictrees.loot.entry;

import com.ferreusveritas.dynamictrees.loot.DTLootContextParams;
import com.ferreusveritas.dynamictrees.tree.species.Species;
import com.google.common.collect.Maps;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.entries.LootPoolEntryType;
import net.minecraft.world.level.storage.loot.entries.LootPoolSingletonContainer;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.Map;
import java.util.function.Consumer;

/**
 * @author Harley O'Connor
 */
public final class ItemBySpeciesLootPoolEntry extends LootPoolSingletonContainer {

    /** Map of items to set, keyed by the name of the species of tree. */
    private final Map<ResourceLocation, Item> items;

    public ItemBySpeciesLootPoolEntry(int weight, int quality, LootItemCondition[] conditions,
                                      LootItemFunction[] functions,
                                      Map<ResourceLocation, Item> items) {
        super(weight, quality, conditions, functions);
        this.items = items;
    }

    @Override
    public LootPoolEntryType getType() {
        return DTLootPoolEntries.ITEM_BY_SPECIES.get();
    }

    @Override
    protected void createItemStack(Consumer<ItemStack> stackConsumer, LootContext context) {
        final Species species = context.getParamOrNull(DTLootContextParams.SPECIES);
        assert species != null;
        Item item = items.get(species.getRegistryName());
        if (item == null) {
            item = Items.AIR;
        }
        stackConsumer.accept(new ItemStack(item));
    }

    public static class Serializer extends LootPoolSingletonContainer.Serializer<ItemBySpeciesLootPoolEntry> {
        @Override
        public void serializeCustom(JsonObject json, ItemBySpeciesLootPoolEntry value, JsonSerializationContext context) {
            super.serialize(json, value, context);
            final JsonObject itemsJson = new JsonObject();
            for (Map.Entry<ResourceLocation, Item> itemEntry : value.items.entrySet()) {
                itemsJson.add(itemEntry.getKey().toString(), new JsonPrimitive(ForgeRegistries.ITEMS.getKey(itemEntry.getValue()).toString()));
            }
            json.add("name_by_species", itemsJson);
        }

        @Override
        protected ItemBySpeciesLootPoolEntry deserialize(JsonObject json, JsonDeserializationContext context,
                                                         int weight, int quality, LootItemCondition[] conditions,
                                                         LootItemFunction[] functions) {
            final JsonObject namesJson = GsonHelper.getAsJsonObject(json, "name_by_species");
            final Map<ResourceLocation, Item> items = Maps.newHashMap();
            for (Map.Entry<String, JsonElement> itemEntry : namesJson.entrySet()) {
                items.put(new ResourceLocation(itemEntry.getKey()), GsonHelper.convertToItem(itemEntry.getValue(), itemEntry.getKey()));
            }
            return new ItemBySpeciesLootPoolEntry(weight, quality, conditions, functions, items);
        }
    }
}
