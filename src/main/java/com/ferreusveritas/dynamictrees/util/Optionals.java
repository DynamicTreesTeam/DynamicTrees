package com.ferreusveritas.dynamictrees.util;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import org.apache.logging.log4j.util.TriConsumer;

import javax.annotation.Nullable;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Supplier;

/**
 * @author Harley O'Connor
 */
public final class Optionals {

    public static <B extends Block> Optional<B> ofBlock(@Nullable Supplier<B> blockSup) {
        return blockSup == null ? Optional.empty() : ofBlock(blockSup.get());
    }
    public static <B extends Block> Optional<B> ofBlock(@Nullable B block) {
        return Optional.ofNullable(block == Blocks.AIR ? null : block);
    }
    public static <I extends Item> Optional<I> ofItem(@Nullable Supplier<I> itemSup) {
        return itemSup == null ? Optional.empty() : ofItem(itemSup.get());
    }

    public static <I extends Item> Optional<I> ofItem(@Nullable I item) {
        return Optional.ofNullable(item == Items.AIR ? null : item);
    }

    public static <A, B> void ifAllPresent(BiConsumer<A, B> consumer, Optional<A> aOptional, Optional<B> bOptional) {
        aOptional.ifPresent(a -> bOptional.ifPresent(b -> consumer.accept(a, b)));
    }

    public static <A, B, C> void ifAllPresent(TriConsumer<A, B, C> consumer, Optional<A> aOptional,
                                              Optional<B> bOptional, Optional<C> cOptional) {
        aOptional.ifPresent(a -> bOptional.ifPresent(b -> cOptional.ifPresent(c -> consumer.accept(a, b, c))));
    }

}
