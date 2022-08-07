package com.ferreusveritas.dynamictrees.util;

import net.minecraft.util.ResourceLocation;

/**
 * @author Harley O'Connor
 */
public final class ResourceLocationUtils {

    public static ResourceLocation parse(String string, final String defaultNamespace) {
        if (!string.contains(":")) {
            string = defaultNamespace + ":" + string;
        }
        return new ResourceLocation(string);
    }

    public static ResourceLocation namespace(final ResourceLocation resourceLocation, final String namespace) {
        return new ResourceLocation(namespace, resourceLocation.getPath());
    }

    public static ResourceLocation prefix(final ResourceLocation resourceLocation, final String prefix) {
        return new ResourceLocation(resourceLocation.getNamespace(), prefix + resourceLocation.getPath());
    }

    public static ResourceLocation suffix(final ResourceLocation resourceLocation, final String suffix) {
        return new ResourceLocation(resourceLocation.getNamespace(), resourceLocation.getPath() + suffix);
    }

    public static ResourceLocation surround(final ResourceLocation resourceLocation, final String prefix, final String suffix) {
        return new ResourceLocation(resourceLocation.getNamespace(), prefix + resourceLocation.getPath() + suffix);
    }

    public static ResourceLocation removeSuffix(final ResourceLocation resourceLocation, final String suffix) {
        final String path = resourceLocation.getPath();
        if (path.endsWith(suffix)) {
            return new ResourceLocation(resourceLocation.getNamespace(), path.substring(0, path.length() - suffix.length()));
        }
        return resourceLocation;
    }

}
