package com.ferreusveritas.dynamictrees.trees;

import com.google.common.collect.Sets;
import net.minecraft.util.ResourceLocation;

import java.util.Optional;
import java.util.Set;

/**
 * A {@link FamilyType} can be registered by add-ons for creating things that need extra
 * code, such as cacti. It will handle creating a custom sub-class of {@link Family}, with
 * the type parameter being the custom species class.
 *
 * @author Harley O'Connor
 */
public abstract class FamilyType<T extends Family> {

    private static final Set<FamilyType<?>> SPECIES_TYPES = Sets.newHashSet();

    public static boolean doesExist (final ResourceLocation registryName) {
        return get(registryName).isPresent();
    }

    public static Optional<FamilyType<?>> get (final ResourceLocation registryName) {
        return SPECIES_TYPES.stream().filter(familyType -> familyType.getRegistryName().equals(registryName)).findFirst();
    }

    public static <T extends Family> FamilyType<T> register(final FamilyType<T> familyType) {
        SPECIES_TYPES.add(familyType);
        return familyType;
    }

    private final ResourceLocation registryName;

    public FamilyType(final ResourceLocation registryName) {
        this.registryName = registryName;
    }

    public ResourceLocation getRegistryName() {
        return registryName;
    }

    /**
     * Constructs the {@link Family} class for this family type.
     *
     * @param registryName The {@link ResourceLocation} for the family.
     * @return The {@link Family} object.
     */
    public abstract T construct (final ResourceLocation registryName);

}
