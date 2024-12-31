package com.dtteam.dynamictrees.platform.services;

import com.dtteam.dynamictrees.api.registry.AbstractRegistry;
import com.dtteam.dynamictrees.api.registry.RegistryEntry;
import com.dtteam.dynamictrees.api.registry.RegistryHandler;
import com.dtteam.dynamictrees.api.registry.TypedRegistry;
import com.dtteam.dynamictrees.init.RegistryLoader;

public interface IRegistryHelper {

    RegistryLoader getRegistryLoader();

    <V extends RegistryEntry<V>> void postRegistryEvent(AbstractRegistry<V> registry);
    <V extends RegistryEntry<V>> void postTypedRegistryEvent(TypedRegistry<V> registry);

    RegistryHandler newRegistryHandler();
    RegistryHandler newRegistryHandler(String modId);
}
