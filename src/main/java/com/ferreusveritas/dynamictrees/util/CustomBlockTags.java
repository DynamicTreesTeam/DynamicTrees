package com.ferreusveritas.dynamictrees.util;

import net.minecraft.block.Block;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.ITag;

/**
 * @author Harley O'Connor
 */
public final class CustomBlockTags {

    public static final ITag.INamedTag<Block> FOLIAGE = bind("foliage");

    public static ITag.INamedTag<Block> bind(String identifier) {
        return BlockTags.bind(identifier);
    }

}
