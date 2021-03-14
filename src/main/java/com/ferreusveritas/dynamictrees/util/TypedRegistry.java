package com.ferreusveritas.dynamictrees.util;

import net.minecraft.util.ResourceLocation;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.fml.ModLoader;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;

/**
 * An extension of {@link Registry} that allows for custom {@link EntryType}.
 *
 * @param <V> The {@link RegistryEntry} type that will be registered.
 * @param <T> The base {@link EntryType} for this registry.
 * @author Harley O'Connor
 */
public class TypedRegistry<V extends RegistryEntry<V>, T extends TypedRegistry.EntryType<V>> extends Registry<V> {

    /**
     * A {@link Map} of {@link EntryType} objects and their registry names. These handle construction
     * of the {@link RegistryEntry}. This is useful for other mods to register sub-classes of the registry
     * entry that can then be referenced from a Json file via a simple resource location.
     */
    private final Map<ResourceLocation, T> typeRegistry = new HashMap<>();

    /** The default {@link T}, the base {@link TypedRegistry.EntryType} for this registry. */
    private final T defaultType;

    /**
     * Constructs a new {@link Registry} with the name being set to {@link Class#getSimpleName()}
     * of the given {@link RegistryEntry}.
     *
     * @param type The {@link Class} of the {@link RegistryEntry}.
     * @param nullValue A null entry. See {@link #nullValue} for more details.
     * @param defaultType The {@link EntryType} of the {@link RegistryEntry}.
     */
    public TypedRegistry(Class<V> type, V nullValue, T defaultType) {
        super(type, nullValue);
        this.defaultType = defaultType;
    }

    /**
     * Constructs a new {@link Registry}.
     *
     * @param name The {@link #name} for this {@link Registry}.
     * @param type The {@link Class} of the {@link RegistryEntry}.
     * @param nullValue A null entry. See {@link #nullValue} for more details.
     * @param defaultType The {@link EntryType} of the {@link RegistryEntry}.
     */
    public TypedRegistry(String name, Class<V> type, V nullValue, T defaultType) {
        super(name, type, nullValue);
        this.defaultType = defaultType;
    }

    /**
     * Registers a custom {@link EntryType}, allowing custom sub-classes of the registry entry
     * to be created and then referenced from Json via the registry name {@link ResourceLocation}.
     *
     * @param registryName The registry name {@link ResourceLocation}.
     * @param type The {@link EntryType} to register as an extension of {@link T}.
     */
    public final void registerType(final ResourceLocation registryName, final T type) {
        this.typeRegistry.put(registryName, type);
    }

    public final boolean hasType(final ResourceLocation registryName) {
        return this.typeRegistry.containsKey(registryName);
    }

    @Nullable
    public final T getType(final ResourceLocation registryName) {
        return this.typeRegistry.get(registryName);
    }

    public final T getDefaultType() {
        return defaultType;
    }

    /**
     * Posts a {@link TypeRegistryEvent} to the mod event bus. Note that this is posted using
     * {@link ModLoader#postEvent(Event)} and as such should only be called during the initial
     * loading phase.
     */
    @SuppressWarnings("unchecked")
    public final void postTypeRegistryEvent() {
        ModLoader.get().postEvent(new TypeRegistryEvent<>((TypedRegistry<V, EntryType<V>>) this));
    }

    /**
     * Handles creation of the registry entry. Custom types can be registered via
     * {@link #registerType(ResourceLocation, EntryType)}.
     *
     * @param <V> The {@link RegistryEntry} sub-class.
     */
    public static abstract class EntryType<V extends RegistryEntry<V>> {}

}
