package com.dtteam.dynamictrees.platform.services;

import com.dtteam.dynamictrees.api.registry.AbstractRegistry;
import com.dtteam.dynamictrees.api.registry.RegistryEntry;
import com.dtteam.dynamictrees.api.registry.RegistryHandler;
import com.dtteam.dynamictrees.api.registry.TypedRegistry;
import net.minecraft.resources.ResourceLocation;

public interface IRegistryHelper {

    <V extends RegistryEntry<V>> void postRegistryEvent(AbstractRegistry<V> registry);
    <V extends RegistryEntry<V>> void postTypedRegistryEvent(TypedRegistry<V> registry);

    RegistryHandler newRegistryHandler(String modid);
}
