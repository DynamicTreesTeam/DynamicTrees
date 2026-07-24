package com.dtteam.dynamictrees.utility;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;

/**
 * Helpers for converting registered objects to the {@link ResourceKey}s required by
 * {@link net.minecraft.data.tags.TagAppender} since 26.2.
 */
public final class TagUtils {

    private TagUtils() {}

    public static ResourceKey<Block> key(Block block) {
        return BuiltInRegistries.BLOCK.getResourceKey(block).orElseThrow();
    }

    public static ResourceKey<Item> key(Item item) {
        return BuiltInRegistries.ITEM.getResourceKey(item).orElseThrow();
    }
}
