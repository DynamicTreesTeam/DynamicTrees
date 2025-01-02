package com.dtteam.dynamictrees.event;

import com.dtteam.dynamictrees.api.registry.Registry;
import com.dtteam.dynamictrees.api.registry.RegistryEntry;
import net.neoforged.bus.api.Event;
import net.neoforged.fml.event.IModBusEvent;

public final class RegistryEvent<V extends RegistryEntry<V>> extends Event implements IModBusEvent {

    private final Registry<V> registry;

    public RegistryEvent(final Registry<V> registry) {
        super();
        this.registry = registry;
    }

    public Registry<V> getRegistry() {
        return registry;
    }

}
