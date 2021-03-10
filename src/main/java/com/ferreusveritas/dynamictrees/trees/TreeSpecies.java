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

    /** The class object for a tree species. Can be used as a generic {@link SpeciesType} object. */
    @SuppressWarnings("unchecked")
    public static final Class<SpeciesType<Species>> CLASS = (Class<SpeciesType<Species>>) TREE_SPECIES.getClass();

    private TreeSpecies() {
        super(DynamicTrees.resLoc("tree"));
    }

    @Override
    public Species construct(ResourceLocation registryName, Family family, LeavesProperties leavesProperties) {
        return new Species(registryName, family, leavesProperties);
    }

}
