package com.ferreusveritas.dynamictrees.deserialisation;

import com.ferreusveritas.dynamictrees.deserialisation.result.Result;
import com.google.gson.JsonElement;
import net.minecraft.util.ResourceLocation;
import org.apache.commons.lang3.StringUtils;

import java.util.Locale;

/**
 * An {@link JsonDeserialiser} for {@link ResourceLocation}s, but if no namespace is defined it defaults to the
 * specified {@link #defaultNamespace} given in {@link #ResourceLocationDeserialiser(String)}.
 * <p>
 * Main instance stored in {@link JsonDeserialisers#RESOURCE_LOCATION} for fetching resource locations with default
 * namespace {@code minecraft}.
 *
 * @author Harley O'Connor
 */
public final class ResourceLocationDeserialiser implements JsonDeserialiser<ResourceLocation> {

    private final String defaultNamespace;

    public ResourceLocationDeserialiser(String defaultNamespace) {
        this.defaultNamespace = defaultNamespace;
    }

    @Override
    public Result<ResourceLocation, JsonElement> deserialise(JsonElement jsonElement) {
        return JsonDeserialisers.STRING.deserialise(jsonElement)
                .map(string -> string.toLowerCase(Locale.ROOT))
                .mapIfValid(ResourceLocationDeserialiser::isValidResourceLocation,
                        "Invalid resource location '{value}'. Namespace Constraints: [a-z0-9_.-] Path Constraints: [a-z0-9/._-]",
                        this::decode);
    }

    public static boolean isValidResourceLocation(String p_217855_0_) {
        final String[] namespaceAndPath = ResourceLocation.decompose(p_217855_0_, ':');
        return ResourceLocation.isValidNamespace(StringUtils.isEmpty(namespaceAndPath[0]) ? "minecraft" : namespaceAndPath[0])
                && ResourceLocation.isValidPath(namespaceAndPath[1]);
    }

    private ResourceLocation decode(final String resLocStr) {
        final String[] namespaceAndPath = new String[]{this.defaultNamespace, resLocStr};
        final int colonIndex = resLocStr.indexOf(':');
        if (colonIndex >= 0) {
            namespaceAndPath[1] = resLocStr.substring(colonIndex + 1);
            if (colonIndex >= 1) {
                namespaceAndPath[0] = resLocStr.substring(0, colonIndex);
            }
        }

        return new ResourceLocation(namespaceAndPath[0], namespaceAndPath[1]);
    }

    public static ResourceLocationDeserialiser create() {
        return new ResourceLocationDeserialiser("minecraft");
    }

    public static ResourceLocationDeserialiser create(final String defaultNamespace) {
        return new ResourceLocationDeserialiser(defaultNamespace);
    }

}
