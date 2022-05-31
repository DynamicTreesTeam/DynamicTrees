package com.ferreusveritas.dynamictrees.api.registry;

import net.minecraftforge.eventbus.api.GenericEvent;
import net.minecraftforge.fml.event.IModBusEvent;


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
