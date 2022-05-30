package com.ferreusveritas.dynamictrees.api.registry;

import net.minecraft.util.ResourceLocation;

import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * An implementation for {@link AbstractRegistry} using a {@link ConcurrentHashMap} to store its entries.
 *
 * @author Harley O'Connor
 * @see SimpleRegistry
 */
public final class ConcurrentRegistry<V extends RegistryEntry<V>> extends AbstractRegistry<V> {

    private final Map<ResourceLocation, V> entries = new ConcurrentHashMap<>();

    /**
     * Constructs a new {@link ConcurrentRegistry} with the name being set to {@link Class#getSimpleName()} of the given
     * {@link RegistryEntry}.
     *
     * @param type      The {@link Class} of the {@link RegistryEntry}.
     * @param nullValue A null entry. See {@link #nullValue} for more details.
     */
    public ConcurrentRegistry(final Class<V> type, final V nullValue) {
        this(type.getSimpleName(), type, nullValue);
    }

    /**
     * Constructs a new {@link ConcurrentRegistry}.
     *
     * @param name      The {@link #name} for this {@link ConcurrentRegistry}.
     * @param type      The {@link Class} of the {@link RegistryEntry}.
     * @param nullValue A null entry. See {@link #nullValue} for more details.
     */
    public ConcurrentRegistry(final String name, final Class<V> type, final V nullValue) {
        this(name, type, nullValue, false);
    }

    /**
     * Constructs a new {@link ConcurrentRegistry} with the name being set to {@link Class#getSimpleName()} of the given
     * {@link RegistryEntry}.
     *
     * @param type      The {@link Class} of the {@link RegistryEntry}.
     * @param nullValue A null entry. See {@link #nullValue} for more details.
     * @param clearable True if {@link #clear()} can be called to wipe the registry.
     */
    public ConcurrentRegistry(final Class<V> type, final V nullValue, final boolean clearable) {
        this(type.getSimpleName(), type, nullValue, clearable);
    }

    /**
     * Constructs a new {@link ConcurrentRegistry}.
     *
     * @param name      The {@link #name} for this {@link ConcurrentRegistry}.
     * @param type      The {@link Class} of the {@link RegistryEntry}.
     * @param nullValue A null entry. See {@link #nullValue} for more details.
     * @param clearable True if {@link #clear()} can be called to wipe the registry.
     */
    public ConcurrentRegistry(final String name, final Class<V> type, final V nullValue, final boolean clearable) {
        super(name, type, nullValue, clearable);

        this.register(nullValue);
    }

    /**
     * Registers the given {@link RegistryEntry} to this {@link ConcurrentRegistry}.
     *
     * <p>Note that this will throw a runtime exception if this {@link SimpleRegistry} is locked, or if
     * the {@link ResourceLocation} already has a value registered, therefore {@link #isLocked()} or/and {@link
     * #has(ResourceLocation)} should be checked before calling if either conditions are uncertain.</p>
     *
     * <p>If you're thinking of using this you should probably be doing it from a
     * {@link RegistryEvent}, in which case you don't have to worry about locking.</p>
     *
     * @param value The {@link RegistryEntry} to register.
     * @return This {@link SimpleRegistry} object for chaining.
     */
    @Override
    public Registry<V> register(V value) {
        this.assertValid(value);

        this.entries.put(value.getRegistryName(), value);
        return this;
    }

    /**
     * Gets all {@link RegistryEntry} objects currently registered. Note this are obtained as an
     * <b>unmodifiable set</b>, meaning they should only be read from this. For registering values
     * use {@link #register(RegistryEntry)}.
     *
     * @return All {@link RegistryEntry} objects currently registered.
     */
    @Override
    public Set<V> getAll() {
        return Collections.unmodifiableSet(new HashSet<>(this.entries.values()));
    }

    /**
     * Clears all {@link RegistryEntry}s currently registered in {@link #entries}.
     */
    @Override
    protected void clearAll() {
        this.entries.clear();
    }

}
