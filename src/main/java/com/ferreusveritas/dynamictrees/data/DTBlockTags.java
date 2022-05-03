package com.ferreusveritas.dynamictrees.data;

import com.ferreusveritas.dynamictrees.DynamicTrees;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.Tag;
import net.minecraft.world.level.block.Block;

/**
 * @author Harley O'Connor
 */
public final class DTBlockTags {

    public static final Tag.Named<Block> BRANCHES = bind("branches");
    public static final Tag.Named<Block> STRIPPED_BRANCHES = bind("stripped_branches");
    public static final Tag.Named<Block> BRANCHES_THAT_BURN = bind("branches_that_burn");
    public static final Tag.Named<Block> STRIPPED_BRANCHES_THAT_BURN = bind("stripped_branches_that_burn");
    public static final Tag.Named<Block> FOLIAGE = bind("foliage");
    public static final Tag.Named<Block> FUNGUS_BRANCHES = bind("fungus_branches");
    public static final Tag.Named<Block> STRIPPED_FUNGUS_BRANCHES = bind("stripped_fungus_branches");
    public static final Tag.Named<Block> FUNGUS_CAPS = bind("fungus_caps");
    public static final Tag.Named<Block> LEAVES = bind("leaves");
    public static final Tag.Named<Block> SAPLINGS = bind("saplings");
    public static final Tag.Named<Block> WART_BLOCKS = bind("wart_blocks");

    private static Tag.Named<Block> bind(String identifier) {
        return BlockTags.bind(DynamicTrees.MOD_ID + ":" + identifier);
    }

}
