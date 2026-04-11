package com.dtteam.dynamictrees.loot.entry;

import com.dtteam.dynamictrees.loot.DTLootContextParams;
import com.dtteam.dynamictrees.tree.species.Species;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.entries.LootPoolSingletonContainer;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

import java.util.List;
import java.util.function.Consumer;

/**
 * @author Harley O'Connor
 */
public final class SeedItemLootPoolEntry extends LootPoolSingletonContainer {

    public static final MapCodec<SeedItemLootPoolEntry> CODEC = RecordCodecBuilder.mapCodec(
            instance -> singletonFields(instance)
                    .apply(instance, SeedItemLootPoolEntry::new));


    public SeedItemLootPoolEntry(int weight, int quality, List<LootItemCondition> conditions,
                                 List<LootItemFunction> functions) {
        super(weight, quality, conditions, functions);
    }

    @Override
    public MapCodec<? extends LootPoolSingletonContainer> codec() {
        return CODEC;
    }

    //    @Override
//    public LootPoolEntryType getType() {
//        return DTRegistries.SEED_ITEM.get();
//    }

    @Override
    protected void createItemStack(Consumer<ItemStack> stackConsumer, LootContext context) {
        final Species species = context.getOptionalParameter(DTLootContextParams.SPECIES);
        assert species != null;
        stackConsumer.accept(species.shouldDropSeeds() ? species.getSeedStack(1) : ItemStack.EMPTY);
    }

    public static LootPoolSingletonContainer.Builder<?> lootTableSeedItem() {
        return simpleBuilder(SeedItemLootPoolEntry::new);
    }

}
