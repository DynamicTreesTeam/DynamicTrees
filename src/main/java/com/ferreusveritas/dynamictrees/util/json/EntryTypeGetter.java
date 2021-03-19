package com.ferreusveritas.dynamictrees.util.json;

import com.ferreusveritas.dynamictrees.api.TreeRegistry;
import com.ferreusveritas.dynamictrees.api.registry.TypedRegistry;
import com.google.gson.JsonElement;
import net.minecraft.util.ResourceLocation;

/**
 * Gets an {@link TypedRegistry.EntryType} from its registry name.
 *
 * @author Harley O'Connor
 */
public final class EntryTypeGetter<T extends TypedRegistry.EntryType<?>> implements IJsonObjectGetter<T> {

    private final TypedRegistry<?, T> registry;

    public EntryTypeGetter(final TypedRegistry<?, T> registry) {
        this.registry = registry;
    }

    @Override
    public ObjectFetchResult<T> get(JsonElement jsonElement) {
        final ObjectFetchResult<ResourceLocation> resLocFetchResult = JsonObjectGetters.RESOURCE_LOCATION_GETTER.get(jsonElement);

        if (!resLocFetchResult.wasSuccessful())
            return ObjectFetchResult.failureFromOther(resLocFetchResult);

        // Get and process registry name, so that if the namespace is 'minecraft' we instead default to 'dynamictrees'.
        final ResourceLocation registryName = TreeRegistry.processResLoc(resLocFetchResult.getValue());

        return ObjectFetchResult.successOrFailure(this.registry.getType(registryName),
                "Could not find " + registry.getName() + " for registry name '" + registryName + "'.");
    }

}
