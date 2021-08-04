package com.ferreusveritas.dynamictrees.api.registry;

import net.minecraft.util.ResourceLocation;
import net.minecraftforge.eventbus.api.GenericEvent;
import net.minecraftforge.fml.event.lifecycle.IModBusEvent;

/**
 * Allows for registering {@link TypedRegistry.EntryType} objects to {@link Registry} objects. Subscribers should use
 * {@link #registerType(ResourceLocation, TypedRegistry.EntryType)} to register their {@link TypedRegistry.EntryType}
 * objects, as full access to the {@link Registry} is not given to prevent misuse of this event, for full access to
 * register {@link RegistryEntry} objects, use {@link RegistryEvent}.
 *
 * <p>This is an implementation of {@link IModBusEvent}, therefore firing on the mod bus.</p>
 *
 * @param <V> The {@link RegistryEntry} sub-class of the relevant {@link Registry}.
 * @author Harley O'Connor
 */
public final class TypeRegistryEvent<V extends RegistryEntry<V>> extends GenericEvent<V> implements IModBusEvent {

    private final TypedRegistry<V> registry;

    public TypeRegistryEvent(final TypedRegistry<V> registry) {
        super(registry.getType());
        this.registry = registry;
    }

    /**
     * Registers a custom {@link TypedRegistry.EntryType}, allowing custom sub-classes of the registry entry to be
     * created and then referenced from Json via the registry name {@link ResourceLocation}.
     *
     * @param registryName The registry name {@link ResourceLocation}.
     * @param type         The {@link TypedRegistry.EntryType} to register.
     */
    public final void registerType(final ResourceLocation registryName, final TypedRegistry.EntryType<V> type) {
        this.registry.registerType(registryName, type);
    }

}
