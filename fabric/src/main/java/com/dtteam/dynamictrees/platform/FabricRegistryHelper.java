package com.dtteam.dynamictrees.platform;

import com.dtteam.dynamictrees.api.registry.AbstractRegistry;
import com.dtteam.dynamictrees.api.registry.RegistryEntry;
import com.dtteam.dynamictrees.api.registry.RegistryHandler;
import com.dtteam.dynamictrees.api.registry.TypedRegistry;
import com.dtteam.dynamictrees.init.FabricRegistryLoader;
import com.dtteam.dynamictrees.init.RegistryLoader;
import com.dtteam.dynamictrees.platform.services.IRegistryHelper;

public class FabricRegistryHelper implements IRegistryHelper {

    private static RegistryLoader registriesInstance;
    @Override
    public RegistryLoader getRegistryLoader() {
        if (registriesInstance == null){
            registriesInstance = new FabricRegistryLoader();
        }
        return registriesInstance;
    }

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