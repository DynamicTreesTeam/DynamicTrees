package com.ferreusveritas.dynamictrees.util;

import net.minecraftforge.eventbus.api.GenericEvent;
import net.minecraftforge.fml.event.lifecycle.IModBusEvent;

/**
 * @author Harley O'Connor
 */
public final class TypeRegistryEvent<V extends RegistryEntry<V>> extends GenericEvent<V> implements IModBusEvent {

    private final TypedRegistry<V, TypedRegistry.EntryType<V>> registry;

    public TypeRegistryEvent(final TypedRegistry<V, TypedRegistry.EntryType<V>> registry) {
        super(registry.getType());
        this.registry = registry;
    }

    public TypedRegistry<V, TypedRegistry.EntryType<V>> getRegistry() {
        return registry;
    }

}
