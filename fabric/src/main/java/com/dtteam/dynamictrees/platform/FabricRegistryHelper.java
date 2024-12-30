package com.dtteam.dynamictrees.platform;

import com.dtteam.dynamictrees.api.registry.AbstractRegistry;
import com.dtteam.dynamictrees.api.registry.RegistryEntry;
import com.dtteam.dynamictrees.api.registry.RegistryHandler;
import com.dtteam.dynamictrees.api.registry.TypedRegistry;
import com.dtteam.dynamictrees.platform.services.IRegistryHelper;
import net.minecraft.resources.ResourceLocation;

public class FabricRegistryHelper implements IRegistryHelper {

    @Override
    public <V extends RegistryEntry<V>> void postRegistryEvent(AbstractRegistry<V> registry) {
        //RegistryEvent.EVENT.invoker().
    }

    @Override
    public <V extends RegistryEntry<V>> void postTypedRegistryEvent(TypedRegistry<V> registry) {

    }

    @Override
    public RegistryHandler newRegistryHandler() {
        return null;
    }

    @Override
    public RegistryHandler newRegistryHandler(String modId) {
        return null;
    }

}