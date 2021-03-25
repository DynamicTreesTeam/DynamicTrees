package com.ferreusveritas.dynamictrees.util.json;

import com.ferreusveritas.dynamictrees.api.TreeRegistry;
import com.ferreusveritas.dynamictrees.api.registry.TypedRegistry;
import com.google.gson.JsonElement;

/**
 * Gets an {@link TypedRegistry.EntryType} from its registry name.
 *
 * @author Harley O'Connor
 */
public final class RegistryEntryTypeGetter<T extends TypedRegistry.EntryType<?>> implements IJsonObjectGetter<T> {

    private final TypedRegistry<?, T> registry;

    public RegistryEntryTypeGetter(final TypedRegistry<?, T> registry) {
        this.registry = registry;
    }

    @Override
    public ObjectFetchResult<T> get(JsonElement jsonElement) {
        return JsonObjectGetters.RESOURCE_LOCATION_GETTER.get(jsonElement).map(registryName -> this.registry.getType(TreeRegistry.processResLoc(registryName)),
                "Could not find '" + this.registry.getName() + "' type for registry name '{previous_value}'.");
    }

}
