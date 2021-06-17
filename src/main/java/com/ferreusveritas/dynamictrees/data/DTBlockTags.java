package com.ferreusveritas.dynamictrees.data;

import com.ferreusveritas.dynamictrees.DynamicTrees;
import net.minecraft.block.Block;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.ITag;

/**
 * @author Harley O'Connor
 */
public final class DTBlockTags {

    public static final ITag.INamedTag<Block> BRANCHES = bind("branches");
    public static final ITag.INamedTag<Block> STRIPPED_BRANCHES = bind("stripped_branches");
    public static final ITag.INamedTag<Block> BRANCHES_THAT_BURN = bind("branches_that_burn");
    public static final ITag.INamedTag<Block> STRIPPED_BRANCHES_THAT_BURN = bind("stripped_branches_that_burn");
    public static final ITag.INamedTag<Block> FOLIAGE = bind("foliage");
    public static final ITag.INamedTag<Block> FUNGUS_BRANCHES = bind("fungus_branches");
    public static final ITag.INamedTag<Block> STRIPPED_FUNGUS_BRANCHES = bind("stripped_fungus_branches");
    public static final ITag.INamedTag<Block> FUNGUS_CAPS = bind("fungus_caps");
    public static final ITag.INamedTag<Block> LEAVES = bind("leaves");
    public static final ITag.INamedTag<Block> SAPLINGS = bind("saplings");
    public static final ITag.INamedTag<Block> WART_BLOCKS = bind("wart_blocks");

    private static ITag.INamedTag<Block> bind(String identifier) {
        return BlockTags.bind(DynamicTrees.MOD_ID + ":" + identifier);
    }

}
