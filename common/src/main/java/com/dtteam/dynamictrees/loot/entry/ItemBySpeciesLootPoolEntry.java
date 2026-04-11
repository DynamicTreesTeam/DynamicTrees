package com.dtteam.dynamictrees.loot.entry;

import com.dtteam.dynamictrees.loot.DTLootContextParams;
import com.dtteam.dynamictrees.tree.species.Species;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.entries.LootPoolSingletonContainer;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

/**
 * @author Harley O'Connor
 */
public final class ItemBySpeciesLootPoolEntry extends LootPoolSingletonContainer {

    public static final MapCodec<ItemBySpeciesLootPoolEntry> CODEC = RecordCodecBuilder.mapCodec(
            instance -> instance
                    .group(Codec.unboundedMap(Identifier.CODEC, BuiltInRegistries.ITEM.holderByNameCodec()).fieldOf("name_by_species").forGetter(c->c.items))
                    .and(singletonFields(instance))
                    .apply(instance, ItemBySpeciesLootPoolEntry::new));

    /** Map of items to set, keyed by the name of the species of tree. */
    private final Map<Identifier, Holder<Item>> items;

    public ItemBySpeciesLootPoolEntry(Map<Identifier, Holder<Item>> items, int weight, int quality, List<LootItemCondition> conditions,
                                      List<LootItemFunction> functions) {
        super(weight, quality, conditions, functions);
        this.items = items;
    }

    @Override
    public MapCodec<? extends LootPoolSingletonContainer> codec() {
        return CODEC;
    }

    //    @Override
//    public LootPoolEntryType getType() {
//        return DTRegistries.ITEM_BY_SPECIES.get();
//    }

    @Override
    protected void createItemStack(Consumer<ItemStack> stackConsumer, LootContext context) {
        final Species species = context.getOptionalParameter(DTLootContextParams.SPECIES);
        assert species != null;
        Holder<Item> itemHolder = items.get(species.getRegistryName());
        Item item = itemHolder == null ? Items.AIR : itemHolder.value();
        stackConsumer.accept(new ItemStack(item));
    }

}
