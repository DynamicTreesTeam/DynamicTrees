package com.dtteam.dynamictrees.loot.entry;

import com.dtteam.dynamictrees.registry.DTRegistries;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.util.random.SimpleWeightedRandomList;
import net.minecraft.util.random.WeightedEntry;
import net.minecraft.util.random.WeightedRandomList;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.entries.LootPoolEntryType;
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
                    .group(SimpleWeightedRandomList.codec(WeightedEntry.Wrapper.codec(BuiltInRegistries.ITEM.holderByNameCodec())).fieldOf("items").forGetter(c->c.items))
                    .and(singletonFields(instance))
                    .apply(instance, WeightedItemLootPoolEntry::new));

    private final WeightedRandomList<WeightedEntry.Wrapper<Holder<Item>>> items;

    public WeightedItemLootPoolEntry(WeightedRandomList<WeightedEntry.Wrapper<Holder<Item>>> items, int weight, int quality, List<LootItemCondition> conditions,
                                     List<LootItemFunction> functions) {
        super(weight, quality, conditions, functions);
        this.items = items;
    }

    @Override
    public LootPoolEntryType getType() {
        return DTRegistries.WEIGHTED_ITEM.get();
    }

    @Override
    protected void createItemStack(Consumer<ItemStack> stackConsumer, LootContext lootContext) {
        items.getRandom(lootContext.getRandom()).ifPresent(wrapper -> stackConsumer.accept(new ItemStack(wrapper.data().value())));
    }


}
