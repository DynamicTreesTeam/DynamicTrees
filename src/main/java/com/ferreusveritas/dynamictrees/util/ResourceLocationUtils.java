package com.ferreusveritas.dynamictrees.util;

import net.minecraft.util.ResourceLocation;

/**
 * @author Harley O'Connor
 */
public final class ResourceLocationUtils {

    public static ResourceLocation namespace(final ResourceLocation resourceLocation, final String namespace) {
        return new ResourceLocation(namespace, resourceLocation.getPath());
    }

    public static ResourceLocation prefix(final ResourceLocation resourceLocation, final String prefix) {
        return new ResourceLocation(resourceLocation.getNamespace(), prefix + resourceLocation.getPath());
    }

    public static ResourceLocation suffix(final ResourceLocation resourceLocation, final String suffix) {
        return new ResourceLocation(resourceLocation.getNamespace(), resourceLocation.getPath() + suffix);
    }

}
