package com.ferreusveritas.dynamictrees.util;

import net.minecraft.util.ResourceLocation;

import java.util.function.Consumer;

/**
 * Holds an entry for a {@link Registry}.
 *
 * @author Harley O'Connor
 */
public class RegistryEntry<T extends RegistryEntry<T>> {

    private ResourceLocation registryName;

    public RegistryEntry() { }

    public RegistryEntry(ResourceLocation registryName) {
        this.registryName = registryName;
    }

    public boolean isValid () {
        return true;
    }

    @SuppressWarnings("unchecked")
    public final boolean ifValid(final Consumer<T> consumer) {
        if (this.isValid()) {
            consumer.accept((T) this);
            return true;
        }
        return false;
    }

    public final ResourceLocation getRegistryName() {
        return this.registryName;
    }

    @SuppressWarnings("unchecked")
    public final T setRegistryName(final ResourceLocation registryName) {
        this.registryName = registryName;
        return (T) this;
    }

}
