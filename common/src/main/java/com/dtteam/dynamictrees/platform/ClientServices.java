package com.dtteam.dynamictrees.platform;

import com.dtteam.dynamictrees.DynamicTrees;
import com.dtteam.dynamictrees.platform.services.IClientHelper;

import java.util.ServiceLoader;


public class ClientServices {

    public static final IClientHelper CLIENT = load(IClientHelper.class);

    public static <T> T load(Class<T> clazz) {
        final T loadedService = ServiceLoader.load(clazz)
                .findFirst()
                .orElseThrow(() -> new NullPointerException("Failed to load service for " + clazz.getName()));
        DynamicTrees.LOG.debug("Loaded {} for service {}", loadedService, clazz);
        return loadedService;
    }
}
