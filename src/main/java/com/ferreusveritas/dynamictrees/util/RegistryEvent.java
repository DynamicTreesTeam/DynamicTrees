package com.ferreusveritas.dynamictrees.util;

import net.minecraftforge.eventbus.api.GenericEvent;
import net.minecraftforge.fml.event.lifecycle.IModBusEvent;

/**
 * Any objects using {@link Registry} can be registered using this registry event,
 * with the type parameter being the registry object class.
 *
 * @author Harley O'Connor
 */
public final class RegistryEvent<T extends RegistryEntry<T>> extends GenericEvent<T> implements IModBusEvent {

    private final Registry<T> registry;

    public RegistryEvent(final Registry<T> registry) {
        super(registry.getType());
        this.registry = registry;
    }

    public Registry<T> getRegistry() {
        return registry;
    }

}
