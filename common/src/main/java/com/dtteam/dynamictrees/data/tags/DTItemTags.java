package com.dtteam.dynamictrees.data.tags;

import com.dtteam.dynamictrees.DynamicTrees;
import com.dtteam.dynamictrees.DynamicTrees;
import com.dtteam.dynamictrees.systems.substance.GrowthSubstance;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;

/**
 * @author Harley O'Connor
 */
public final class DTItemTags {

    public static final TagKey<Item> BRANCHES = bind("branches");
    public static final TagKey<Item> BRANCHES_THAT_BURN = bind("branches_that_burn");
    public static final TagKey<Item> FUNGUS_BRANCHES = bind("fungus_branches");

    public static final TagKey<Item> SEEDS = bind("seeds");
    public static final TagKey<Item> FUNGUS_CAPS = bind("fungus_caps");

    /**
     * Items that apply a growth pulse to trees. By default, includes bone meal.
     */
    public static final TagKey<Item> FERTILIZER = bind("fertilizer");
    /**
     * Items that apply the {@link GrowthSubstance growth substance} to trees.
     * This is for modded items such as Create's tree fertilizer.
     */
    public static final TagKey<Item> ENHANCED_FERTILIZER = bind("enhanced_fertilizer");

    private static TagKey<Item> bind(String identifier) {
        return TagKey.create(Registries.ITEM, DynamicTrees.location(identifier));
    }

}
