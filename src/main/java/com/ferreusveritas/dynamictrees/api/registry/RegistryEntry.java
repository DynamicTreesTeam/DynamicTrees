package com.ferreusveritas.dynamictrees.api.registry;

import net.minecraft.util.ResourceLocation;
import org.apache.commons.lang3.tuple.Pair;

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

    /**
     * Makes the current entry invalid.
     *
     * @return This {@link RegistryEntry}, for calling this in-line.
     */
    @SuppressWarnings("unchecked")
    protected T nullEntry () {
        this.valid = false;
        return (T) this;
    }

    /**
     * @return True if the {@link RegistryEntry} is not null.
     */
    public boolean isValid () {
        return this.valid;
    }

    /**
     * Calls {@link Consumer#accept(Object)} on the given {@link Consumer} of type {@link T},
     * only if this {@link RegistryEntry} is {@code valid}.
     *
     * @param consumer The {@link Consumer} of type {@link T}.
     * @return {@code true} if this {@link RegistryEntry} is {@code valid}; {@code false}
     *         otherwise.
     */
    @SuppressWarnings("unchecked")
    public final boolean ifValid(final Consumer<T> consumer) {
        if (this.isValid()) {
            consumer.accept((T) this);
            return true;
        }
        return false;
    }

    /**
     * Calls {@link Consumer#accept(Object)} on the given {@link Consumer} of type {@link T},
     * only if this {@link RegistryEntry} is {@code valid}. Otherwise calls
     * {@link Runnable#run()} on the given {@link Runnable}.
     *
     * @param consumer The {@link Consumer} of type {@link T} to consume if {@code valid}.
     * @param runnable The {@link Runnable} to run if {@code invalid}.
     * @return {@code true} if this {@link RegistryEntry} is {@code valid}; {@code false}
     *         otherwise.
     */
    @SuppressWarnings("unchecked")
    public final boolean ifValidElse(final Consumer<T> consumer, final Runnable runnable) {
        if (this.isValid()) {
            consumer.accept((T) this);
            return true;
        }
        runnable.run();
        return false;
    }

    /**
     * Calls {@link Runnable#run()} on the given {@link Runnable} if this {@link RegistryEntry}
     * is {@code invalid}.
     *
     * @param runnable The {@link Runnable} to run if {@code invalid}.
     * @return {@code true} if this {@link RegistryEntry} is {@code invalid}; {@code false}
     *         otherwise.
     */
    public final boolean ifInvalid(final Runnable runnable) {
        if (!this.isValid()) {
            runnable.run();
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

    @Override
    public String toString() {
        return this.getClass().getSimpleName() + "{registryName=" + this.getRegistryName() + "}";
    }

    public String toLoadDataString() {
        return this.toString();
    }

    public String toReloadDataString() {
        return this.toString();
    }

    @SafeVarargs
    public final String getString(final Pair<String, Object>... propertyPairs) {
        final StringBuilder stringBuilder = new StringBuilder(this.getClass().getSimpleName() + "{registryName=" + this.getRegistryName() + ", ");

        for (int i = 0; i < propertyPairs.length; i++) {
            final Pair<String, Object> currentProperty = propertyPairs[i];
            stringBuilder.append(currentProperty.getKey()).append("=").append(currentProperty.getValue()).append(i != propertyPairs.length - 1 ? ", " : "}");
        }

        return stringBuilder.toString();
    }

}
