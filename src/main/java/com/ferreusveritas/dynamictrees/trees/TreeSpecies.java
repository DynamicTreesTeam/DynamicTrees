package com.ferreusveritas.dynamictrees.trees;

import com.ferreusveritas.dynamictrees.DynamicTrees;
import com.ferreusveritas.dynamictrees.blocks.leaves.LeavesProperties;
import net.minecraft.util.ResourceLocation;

/**
 * Default {@link SpeciesType}, handling construction of the base {@link Species} class.
 *
 * @author Harley O'Connor
 */
public final class TreeSpecies extends SpeciesType<Species> {

    public static final SpeciesType<Species> TREE_SPECIES = register(new TreeSpecies());

    private TreeSpecies() {
        super(DynamicTrees.resLoc("tree"));
    }

    @Override
    public Species construct(ResourceLocation registryName, TreeFamily treeFamily, LeavesProperties leavesProperties) {
        return new Species(registryName, treeFamily, leavesProperties);
    }

}
