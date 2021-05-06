package com.ferreusveritas.dynamictrees.util.json;

import com.google.gson.JsonElement;
import net.minecraft.util.ResourceLocation;

/**
 * An {@link IJsonObjectGetter} for {@link ResourceLocation}s, but if no namespace is
 * defined it defaults to the specified {@link #defaultNamespace} given in
 * {@link #ResourceLocationGetter(String)}.
 *
 * Main instance stored in {@link JsonObjectGetters#RESOURCE_LOCATION} for fetching
 * resource locations with default namespace {@code minecraft}.
 *
 * @author Harley O'Connor
 */
public final class ResourceLocationGetter implements IJsonObjectGetter<ResourceLocation> {

    private final String defaultNamespace;

    public ResourceLocationGetter(String defaultNamespace) {
        this.defaultNamespace = defaultNamespace;
    }

    @Override
    public ObjectFetchResult<ResourceLocation> get(JsonElement jsonElement) {
        return JsonObjectGetters.STRING.get(jsonElement)
                .mapIfValid(ResourceLocation::isValidResourceLocation,
                        "Invalid resource location '{value}'. Namespace Constraints: [a-z0-9_.-] Path Constraints: [a-z0-9/._-]",
                        this::decode);
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

    public static ResourceLocationGetter create() {
        return new ResourceLocationGetter("minecraft");
    }

    public static ResourceLocationGetter create(final String defaultNamespace) {
        return new ResourceLocationGetter(defaultNamespace);
    }

}
