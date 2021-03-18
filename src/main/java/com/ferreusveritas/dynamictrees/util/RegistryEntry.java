package com.ferreusveritas.dynamictrees.util;

import net.minecraft.util.ResourceLocation;

import java.util.function.Consumer;

/**
 * Holds an entry for a {@link Registry}.
 *
 * @author Harley O'Connor
 */
public abstract class RegistryEntry<T extends RegistryEntry<T>> {

    private ResourceLocation registryName;
    private boolean valid = true;

    protected RegistryEntry() { }

    protected RegistryEntry(ResourceLocation registryName) {
        this.registryName = registryName;
    }

    @SuppressWarnings("unchecked")
    protected T nullEntry () {
        this.valid = false;
        return (T) this;
    }

    public boolean isValid () {
        return this.valid;
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
