package com.ferreusveritas.dynamictrees.data;

import com.ferreusveritas.dynamictrees.DynamicTrees;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.Tag;
import net.minecraft.world.item.Item;

/**
 * @author Harley O'Connor
 */
public final class DTItemTags {

    public static final Tag.Named<Item> BRANCHES = bind("branches");
    public static final Tag.Named<Item> BRANCHES_THAT_BURN = bind("branches_that_burn");
    public static final Tag.Named<Item> FUNGUS_BRANCHES = bind("fungus_branches");

    public static final Tag.Named<Item> SEEDS = bind("seeds");
    public static final Tag.Named<Item> FUNGUS_CAPS = bind("fungus_caps");

    /**
     * Items that apply a growth pulse to trees. By default, includes bone meal.
     */
    public static final Tag.Named<Item> FERTILIZER = bind("fertilizer");
    /**
     * Items that apply the {@link com.ferreusveritas.dynamictrees.systems.substances.GrowthSubstance growth substance}
     * to trees.
     */
    public static final Tag.Named<Item> ENHANCED_FERTILIZER = bind("enhanced_fertilizer");

    private static Tag.Named<Item> bind(String identifier) {
        return ItemTags.bind(DynamicTrees.MOD_ID + ":" + identifier);
    }

}
