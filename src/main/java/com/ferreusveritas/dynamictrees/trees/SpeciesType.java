package com.ferreusveritas.dynamictrees.trees;

import com.ferreusveritas.dynamictrees.blocks.leaves.LeavesProperties;
import com.google.common.collect.Sets;
import net.minecraft.util.ResourceLocation;

import java.util.Optional;
import java.util.Set;

/**
 * A {@link SpeciesType} can be registered by add-ons for creating things that need extra
 * code, such as cacti. It will handle creating a custom sub-class of {@link Species}, with
 * the type parameter being the custom species class.
 *
 * <p><i>Note that this is not yet in use. It will be implemented properly when family tree
 * packs are done.</i></p>
 *
 * @author Harley O'Connor
 */
public abstract class SpeciesType<T extends Species> {

    private static final Set<SpeciesType<?>> SPECIES_TYPES = Sets.newHashSet();

    @SuppressWarnings("all")
    public static final Class<SpeciesType<Species>> CLASS = (Class<SpeciesType<Species>>) TreeSpecies.TREE_SPECIES.getClass();

    public static boolean doesExist (final ResourceLocation registryName) {
        return get(registryName).isPresent();
    }

    public static Optional<SpeciesType<?>> get (final ResourceLocation registryName) {
        return SPECIES_TYPES.stream().filter(speciesType -> speciesType.getRegistryName().equals(registryName)).findFirst();
    }

    public static <T extends Species> SpeciesType<T> register(final SpeciesType<T> speciesType) {
        SPECIES_TYPES.add(speciesType);
        return speciesType;
    }

    private final ResourceLocation registryName;

    public SpeciesType(final ResourceLocation registryName) {
        this.registryName = registryName;
    }

    public ResourceLocation getRegistryName() {
        return registryName;
    }

    /**
     * Constructs the {@link Species} class for this species type, using the common
     * leaves from the given {@link TreeFamily}.
     *
     * @param registryName The {@link ResourceLocation} for the species.
     * @param treeFamily The {@link TreeFamily} the species belongs to.
     * @return The {@link Species} object.
     */
    public T construct (final ResourceLocation registryName, final TreeFamily treeFamily) {
        return this.construct(registryName, treeFamily, treeFamily.getCommonLeaves());
    }

    /**
     * Constructs the {@link Species} class for this species type.
     *
     * @param registryName The {@link ResourceLocation} for the species.
     * @param treeFamily The {@link TreeFamily} the species belongs to.
     * @param leavesProperties The {@link LeavesProperties} for the species.
     * @return The {@link Species} object.
     */
    public abstract T construct (final ResourceLocation registryName, final TreeFamily treeFamily, final LeavesProperties leavesProperties);

}
