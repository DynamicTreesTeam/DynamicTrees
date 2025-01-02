package com.dtteam.dynamictrees.platform;

import com.dtteam.dynamictrees.api.registry.*;
import com.dtteam.dynamictrees.registry.NeoForgeRegistryLoader;
import com.dtteam.dynamictrees.registry.RegistryLoader;
import com.dtteam.dynamictrees.platform.services.IRegistryHelper;
import com.dtteam.dynamictrees.registry.NeoForgeRegistryHandler;

public class NeoForgeRegistryHelper implements IRegistryHelper {

    private static RegistryLoader registriesInstance;
    @Override
    public RegistryLoader getRegistryLoader() {
        if (registriesInstance == null){
            registriesInstance = new NeoForgeRegistryLoader();
        }
        return registriesInstance;
    }

    @Override
    public RegistryHandler newRegistryHandler() {
        return new NeoForgeRegistryHandler();
    }
    @Override
    public RegistryHandler newRegistryHandler(String modId) {
        return new NeoForgeRegistryHandler(modId);
    }

}