package com.dtteam.dynamictrees.platform;

import com.dtteam.dynamictrees.DynamicTrees;
import com.dtteam.dynamictrees.platform.services.*;

import java.util.ServiceLoader;

// Service loaders are a built-in Java feature that allow us to locate implementations of an interface that vary from one
// environment to another. In the context of MultiLoader we use this feature to access a mock API in the common code that
// is swapped out for the platform specific implementation at runtime.
public class Services {

    public static final IPlatformHelper PLATFORM = load(IPlatformHelper.class);
    public static final IRegistryHelper REGISTRY = load(IRegistryHelper.class);
    public static final IConfigHelper CONFIG = load(IConfigHelper.class);
    public static final IItemHelper ITEM = load(IItemHelper.class);
    public static final ITextureHelper TEXTURE = load(ITextureHelper.class);

    public static <T> T load(Class<T> clazz) {

        final T loadedService = ServiceLoader.load(clazz)
                .findFirst()
                .orElseThrow(() -> new NullPointerException("Failed to load service for " + clazz.getName()));
        DynamicTrees.LOG.debug("Loaded {} for service {}", loadedService, clazz);
        return loadedService;
    }
}