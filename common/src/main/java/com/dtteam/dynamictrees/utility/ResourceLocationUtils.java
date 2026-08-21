package com.dtteam.dynamictrees.utility;

import com.dtteam.dynamictrees.DynamicTrees;
import net.minecraft.resources.Identifier;

import java.util.Optional;

/**
 * @author Harley O'Connor
 */
public final class ResourceLocationUtils {

    public static Identifier parse(String string, final String defaultNamespace) {
        if (!string.contains(":")) {
            string = defaultNamespace + ":" + string;
        }
        return Identifier.parse(string);
    }

    public static Identifier namespace(final Identifier resourceLocation, final String namespace) {
        return Identifier.fromNamespaceAndPath(namespace, resourceLocation.getPath());
    }

    public static Identifier prefix(final Identifier resourceLocation, final String prefix) {
        return Identifier.fromNamespaceAndPath(resourceLocation.getNamespace(), prefix + resourceLocation.getPath());
    }

    public static Identifier suffix(final Identifier resourceLocation, final String suffix) {
        return Identifier.fromNamespaceAndPath(resourceLocation.getNamespace(), resourceLocation.getPath() + suffix);
    }

    public static Identifier surround(final Identifier resourceLocation, final String prefix, final String suffix) {
        return Identifier.fromNamespaceAndPath(resourceLocation.getNamespace(), prefix + resourceLocation.getPath() + suffix);
    }

    /**
     * Parses resource location and  processes it via {@link #parseDTLocation(Identifier)}. If it could not be
     * parsed, returns {@link DynamicTrees#NULL}.
     *
     * @param resLocString The {@link Identifier} {@link String} to parse.
     * @return The parsed and processed {@link Identifier} object.
     */
    public static Identifier parseDTLocation(final String resLocString) {
        return Optional.ofNullable(Identifier.tryParse(resLocString))
                .orElse(DynamicTrees.NULL);
    }

    /**
     * Changes namespace of resource location to "dynamictrees" as a default if it is set to Minecraft. This is safe
     * since Minecraft won't (or shouldn't) have used any of our registries.
     *
     * @param resourceLocation The {@link Identifier} to parse.
     * @return The {@link Identifier} object.
     */
    public static Identifier parseDTLocation(final Identifier resourceLocation) {
        return DynamicTrees.MINECRAFT.equals(resourceLocation.getNamespace()) ?
                DynamicTrees.location(resourceLocation.getPath()) : resourceLocation;
    }

}
