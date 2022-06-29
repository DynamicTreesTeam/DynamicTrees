package com.ferreusveritas.dynamictrees.util;

import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.loot.LootTable;
import org.apache.logging.log4j.util.TriConsumer;

import javax.annotation.Nullable;
import java.util.Optional;
import java.util.function.BiConsumer;

/**
 * @author Harley O'Connor
 */
public final class Optionals {

    public static <B extends Block> Optional<B> ofBlock(@Nullable B block) {
        return Optional.ofNullable(block == Blocks.AIR ? null : block);
    }

    public static <I extends Item> Optional<I> ofItem(@Nullable I item) {
        return Optional.ofNullable(item == Items.AIR ? null : item);
    }

    public static <T extends LootTable> Optional<T> ofLootTable(@Nullable T lootTable) {
        return Optional.ofNullable(lootTable == LootTable.EMPTY ? null : lootTable);
    }

    public static <A, B> void ifAllPresent(BiConsumer<A, B> consumer, Optional<A> aOptional, Optional<B> bOptional) {
        aOptional.ifPresent(a -> bOptional.ifPresent(b -> consumer.accept(a, b)));
    }

    public static <A, B, C> void ifAllPresent(TriConsumer<A, B, C> consumer, Optional<A> aOptional,
                                              Optional<B> bOptional, Optional<C> cOptional) {
        aOptional.ifPresent(a -> bOptional.ifPresent(b -> cOptional.ifPresent(c -> consumer.accept(a, b, c))));
    }

}
