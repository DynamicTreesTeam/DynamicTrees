package com.dtteam.dynamictrees.loot.entry;

import net.minecraft.world.level.storage.loot.entries.LootPoolEntryContainer;

import com.dtteam.dynamictrees.registry.DTRegistries;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.util.random.WeightedList;
import net.minecraft.util.random.Weighted;
import net.minecraft.util.random.WeightedList;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import com.mojang.serialization.MapCodec;
import net.minecraft.world.level.storage.loot.entries.LootPoolSingletonContainer;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

import java.util.List;
import java.util.function.Consumer;

/**
 * @author Harley O'Connor
 */
public final class WeightedItemLootPoolEntry extends LootPoolSingletonContainer {

    public static final MapCodec<WeightedItemLootPoolEntry> CODEC = RecordCodecBuilder.mapCodec(
            instance -> instance
                    .group(WeightedList.codec(Weighted.codec(BuiltInRegistries.ITEM.holderByNameCodec())).fieldOf("items").forGetter(c->c.items))
                    .and(singletonFields(instance))
                    .apply(instance, WeightedItemLootPoolEntry::new));

    private final WeightedList<Weighted<Holder<Item>>> items;

    public WeightedItemLootPoolEntry(WeightedList<Weighted<Holder<Item>>> items, int weight, int quality, List<LootItemCondition> conditions,
                                     List<LootItemFunction> functions) {
        super(weight, quality, conditions, functions);
        this.items = items;
    }

    public MapCodec<? extends LootPoolSingletonContainer> codec() {
        return CODEC;
    }

    protected void createItemStack(Consumer<ItemStack> stackConsumer, LootContext lootContext) {
        items.getRandom(lootContext.getRandom()).ifPresent(wrapper -> stackConsumer.accept(new ItemStack(wrapper.value().value())));
    }


}
