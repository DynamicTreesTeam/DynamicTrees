package com.dtteam.dynamictrees.platform.services;

import com.dtteam.dynamictrees.api.registry.RegistryHandler;
import com.dtteam.dynamictrees.registry.RegistryLoader;

public interface IRegistryHelper {

    RegistryLoader getRegistryLoader();

    RegistryHandler newRegistryHandler();
    RegistryHandler newRegistryHandler(String modId);
}
