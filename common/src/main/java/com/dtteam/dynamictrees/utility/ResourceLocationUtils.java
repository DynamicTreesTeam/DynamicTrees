package com.dtteam.dynamictrees.utility;

import com.dtteam.dynamictrees.DynamicTrees;
import net.minecraft.resources.ResourceLocation;

import java.util.Optional;

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

    /**
     * Parses resource location and  processes it via {@link #parseDTLocation(ResourceLocation)}. If it could not be
     * parsed, returns {@link DynamicTrees#NULL}.
     *
     * @param resLocString The {@link ResourceLocation} {@link String} to parse.
     * @return The parsed and processed {@link ResourceLocation} object.
     */
    public static ResourceLocation parseDTLocation(final String resLocString) {
        return Optional.ofNullable(ResourceLocation.tryParse(resLocString))
                .orElse(DynamicTrees.NULL);
    }

    /**
     * Changes namespace of resource location to "dynamictrees" as a default if it is set to Minecraft. This is safe
     * since Minecraft won't (or shouldn't) have used any of our registries.
     *
     * @param resourceLocation The {@link ResourceLocation} to parse.
     * @return The {@link ResourceLocation} object.
     */
    public static ResourceLocation parseDTLocation(final ResourceLocation resourceLocation) {
        return DynamicTrees.MINECRAFT.equals(resourceLocation.getNamespace()) ?
                DynamicTrees.location(resourceLocation.getPath()) : resourceLocation;
    }

}
