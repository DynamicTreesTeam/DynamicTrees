package com.ferreusveritas.dynamictrees.util.json;

import com.ferreusveritas.dynamictrees.api.TreeRegistry;
import com.ferreusveritas.dynamictrees.api.registry.Registry;
import com.ferreusveritas.dynamictrees.api.registry.RegistryEntry;
import com.google.gson.JsonElement;

/**
 * Gets {@link RegistryEntry} object of type {@link T} from the given {@link Registry} object.
 *
 * @author Harley O'Connor
 */
public final class RegistryEntryGetter<T extends RegistryEntry<T>> implements IJsonObjectGetter<T> {

    private final Registry<T> registry;

    public RegistryEntryGetter(Registry<T> registry) {
        this.registry = registry;
    }

    @Override
    public ObjectFetchResult<T> get(JsonElement jsonElement) {
        return JsonObjectGetters.RESOURCE_LOCATION_GETTER.get(jsonElement).map(registryName -> this.registry.get(TreeRegistry.processResLoc(registryName)),
                RegistryEntry::isValid, "Could not find " + this.registry.getName() + " for registry name '{previous_value}'.");
    }

}
