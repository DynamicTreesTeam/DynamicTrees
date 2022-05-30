package com.ferreusveritas.dynamictrees.api.registry;

import net.minecraft.util.ResourceLocation;
import net.minecraftforge.eventbus.api.GenericEvent;
import net.minecraftforge.fml.event.lifecycle.IModBusEvent;

/**
 * Any objects using {@link SimpleRegistry} can be registered using this registry event. Gives full access to the {@link
 * SimpleRegistry}, allowing things like {@link SimpleRegistry#has(ResourceLocation)} to be checked. There is no need to check if
 * the {@link SimpleRegistry} is locked as it should always be unlocked before calling this event.
 *
 * <p>Please note that this event should not be used for registering
 * {@link TypedRegistry.EntryType} objects for {@link TypedRegistry} objects. See {@link TypeRegistryEvent} for
 * that.</p>
 *
 * <p>This is an implementation of {@link IModBusEvent}, therefore firing on the mod bus.</p>
 *
 * @param <V> The {@link RegistryEntry} sub-class of the relevant {@link SimpleRegistry}.
 * @author Harley O'Connor
 */
public final class RegistryEvent<V extends RegistryEntry<V>> extends GenericEvent<V> implements IModBusEvent {

    private final Registry<V> registry;

    public RegistryEvent(final Registry<V> registry) {
        super(registry.getType());
        this.registry = registry;
    }

    public Registry<V> getRegistry() {
        return registry;
    }

}
