package com.dtteam.dynamictrees.event;

import com.dtteam.dynamictrees.api.registry.RegistryEntry;
import com.dtteam.dynamictrees.api.registry.SimpleRegistry;
import com.dtteam.dynamictrees.api.registry.TypedRegistry;
import net.minecraft.resources.Identifier;
import net.neoforged.bus.api.Event;
import net.neoforged.fml.event.IModBusEvent;

/**
 * Allows for registering {@link TypedRegistry.EntryType} objects to {@link SimpleRegistry} objects. Subscribers should use
 * {@link #registerType(Identifier, TypedRegistry.EntryType)} to register their {@link TypedRegistry.EntryType}
 * objects, as full access to the {@link SimpleRegistry} is not given to prevent misuse of this event, for full access to
 * register {@link RegistryEntry} objects, use {@link RegistryEvent}.
 *
 * <p>This is an implementation of {@link IModBusEvent}, therefore firing on the mod bus.</p>
 *
 * @param <V> The {@link RegistryEntry} sub-class of the relevant {@link SimpleRegistry}.
 * @author Harley O'Connor
 */
public final class TypeRegistryEvent<V extends RegistryEntry<V>> extends Event implements IModBusEvent {

    private final TypedRegistry<V> registry;

    private final Class<V> type;

    public TypeRegistryEvent(final TypedRegistry<V> registry) {
        super();
        this.registry = registry;
        this.type = registry.getType();
    }

    public boolean isEntryOfType(Class<?> typeTest){
        return type.isAssignableFrom(typeTest);
    }

    /**
     * Registers a custom {@link TypedRegistry.EntryType}, allowing custom sub-classes of the registry entry to be
     * created and then referenced from Json via the registry name {@link Identifier}.
     *
     * @param registryName The registry name {@link Identifier}.
     * @param type         The {@link TypedRegistry.EntryType} to register.
     */
    public void registerType(final Identifier registryName, final TypedRegistry.EntryType<V> type) {
        this.registry.registerType(registryName, type);
    }

}
