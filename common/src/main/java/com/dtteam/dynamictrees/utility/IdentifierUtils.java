package com.dtteam.dynamictrees.utility;

import com.dtteam.dynamictrees.DynamicTrees;
import net.minecraft.resources.Identifier;

import java.util.Optional;

/**
 * @author Harley O'Connor
 */
public final class IdentifierUtils {

    public static Identifier parse(String string, final String defaultNamespace) {
        if (!string.contains(":")) {
            string = defaultNamespace + ":" + string;
        }
        return Identifier.parse(string);
    }

    public static Identifier namespace(final Identifier identifier, final String namespace) {
        return Identifier.fromNamespaceAndPath(namespace, identifier.getPath());
    }

    public static Identifier prefix(final Identifier identifier, final String prefix) {
        return Identifier.fromNamespaceAndPath(identifier.getNamespace(), prefix + identifier.getPath());
    }

    public static Identifier suffix(final Identifier identifier, final String suffix) {
        return Identifier.fromNamespaceAndPath(identifier.getNamespace(), identifier.getPath() + suffix);
    }

    public static Identifier surround(final Identifier identifier, final String prefix, final String suffix) {
        return Identifier.fromNamespaceAndPath(identifier.getNamespace(), prefix + identifier.getPath() + suffix);
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
     * @param identifier The {@link Identifier} to parse.
     * @return The {@link Identifier} object.
     */
    public static Identifier parseDTLocation(final Identifier identifier) {
        return DynamicTrees.MINECRAFT.equals(identifier.getNamespace()) ?
                DynamicTrees.location(identifier.getPath()) : identifier;
    }

}
