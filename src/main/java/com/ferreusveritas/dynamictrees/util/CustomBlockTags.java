package com.ferreusveritas.dynamictrees.util;

import com.ferreusveritas.dynamictrees.DynamicTrees;
import net.minecraft.block.Block;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.ITag;

/**
 * @author Harley O'Connor
 */
public final class CustomBlockTags {

    public static final ITag.INamedTag<Block> FOLIAGE = bind(DynamicTrees.MOD_ID + ":foliage");

    public static ITag.INamedTag<Block> bind(String identifier) {
        return BlockTags.bind(identifier);
    }

}
