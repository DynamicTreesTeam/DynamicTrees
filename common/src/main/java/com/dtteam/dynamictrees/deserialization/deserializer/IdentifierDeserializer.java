package com.dtteam.dynamictrees.deserialization.deserializer;

import com.dtteam.dynamictrees.deserialization.JsonDeserializers;
import com.dtteam.dynamictrees.deserialization.result.Result;
import com.google.gson.JsonElement;
import net.minecraft.resources.Identifier;
import org.apache.commons.lang3.StringUtils;

import java.util.Locale;

/**
 * An {@link JsonDeserializer} for {@link Identifier}s, but if no namespace is defined it defaults to the
 * specified {@link #defaultNamespace} given in {@link #IdentifierDeserializer(String)}.
 * <p>
 * Main instance stored in {@link JsonDeserializers#RESOURCE_LOCATION} for fetching resource locations with default
 * namespace {@code minecraft}.
 *
 * @author Harley O'Connor
 */
public final class IdentifierDeserializer implements JsonDeserializer<Identifier> {

    private final String defaultNamespace;

    public IdentifierDeserializer(String defaultNamespace) {
        this.defaultNamespace = defaultNamespace;
    }

    @Override
    public Result<Identifier, JsonElement> deserialize(JsonElement jsonElement) {
        return JsonDeserializers.STRING.deserialize(jsonElement)
                .map(string -> string.toLowerCase(Locale.ENGLISH))
                .mapIfValid(IdentifierDeserializer::isValidIdentifier,
                        "Invalid resource location '{value}'. Namespace Constraints: [a-z0-9_.-] Path Constraints: [a-z0-9/._-]",
                        this::decode);
    }

    public static boolean isValidIdentifier(String loc) {
        final Identifier resLoc = Identifier.parse(loc);
        return Identifier.isValidNamespace(StringUtils.isEmpty(resLoc.getNamespace()) ? "minecraft" : resLoc.getNamespace())
                && Identifier.isValidPath(resLoc.getPath());
    }

    private Identifier decode(final String resLocStr) {
        final String[] namespaceAndPath = new String[]{this.defaultNamespace, resLocStr};
        final int colonIndex = resLocStr.indexOf(':');
        if (colonIndex >= 0) {
            namespaceAndPath[1] = resLocStr.substring(colonIndex + 1);
            if (colonIndex >= 1) {
                namespaceAndPath[0] = resLocStr.substring(0, colonIndex);
            }
        }

        return Identifier.fromNamespaceAndPath(namespaceAndPath[0], namespaceAndPath[1]);
    }

    public static IdentifierDeserializer create() {
        return new IdentifierDeserializer("minecraft");
    }

    public static IdentifierDeserializer create(final String defaultNamespace) {
        return new IdentifierDeserializer(defaultNamespace);
    }

}
