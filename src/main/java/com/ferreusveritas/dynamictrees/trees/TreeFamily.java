package com.ferreusveritas.dynamictrees.trees;

import com.ferreusveritas.dynamictrees.DynamicTrees;
import net.minecraft.util.ResourceLocation;

/**
 * @author Harley O'Connor
 */
public final class TreeFamily extends FamilyType<Family> {

    public static final FamilyType<Family> TREE_FAMILY = register(new TreeFamily());

    /** The class object for a tree family. Can be used as a generic {@link FamilyType} object. */
    @SuppressWarnings("unchecked")
    public static final Class<FamilyType<Family>> CLASS = (Class<FamilyType<Family>>) TREE_FAMILY.getClass();

    private TreeFamily() {
        super(DynamicTrees.resLoc("tree_family"));
    }

    @Override
    public Family construct(ResourceLocation registryName) {
        return new Family(registryName);
    }
}
