package com.dtteam.dynamictrees.util;

import net.minecraft.resources.ResourceLocation;

/**
 * @author Harley O'Connor
 */
public final class ResourceLocationUtils {

    public static ResourceLocation parse(String string, final String defaultNamespace) {
        if (!string.contains(":")) {
            string = defaultNamespace + ":" + string;
        }
        return ResourceLocation.parse(string);
    }

    public static ResourceLocation namespace(final ResourceLocation resourceLocation, final String namespace) {
        return ResourceLocation.fromNamespaceAndPath(namespace, resourceLocation.getPath());
    }

    public static ResourceLocation prefix(final ResourceLocation resourceLocation, final String prefix) {
        return ResourceLocation.fromNamespaceAndPath(resourceLocation.getNamespace(), prefix + resourceLocation.getPath());
    }

    public static ResourceLocation suffix(final ResourceLocation resourceLocation, final String suffix) {
        return ResourceLocation.fromNamespaceAndPath(resourceLocation.getNamespace(), resourceLocation.getPath() + suffix);
    }

    public static ResourceLocation surround(final ResourceLocation resourceLocation, final String prefix, final String suffix) {
        return ResourceLocation.fromNamespaceAndPath(resourceLocation.getNamespace(), prefix + resourceLocation.getPath() + suffix);
    }

}
