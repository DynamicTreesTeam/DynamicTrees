package com.ferreusveritas.dynamictrees.util.json;

import com.ferreusveritas.dynamictrees.api.registry.Registry;
import com.ferreusveritas.dynamictrees.api.registry.RegistryEntry;
import com.google.gson.JsonElement;

/**
 * Gets {@link RegistryEntry} object of type {@link T} from the given {@link Registry} object.
 *
 * @author Harley O'Connor
 */
public final class RegistryEntryGetter<T extends RegistryEntry<T>> implements JsonGetter<T> {

    private final Registry<T> registry;

    public RegistryEntryGetter(Registry<T> registry) {
        this.registry = registry;
    }

    @Override
    public FetchResult<T> get(JsonElement jsonElement) {
        return JsonGetters.DT_RESOURCE_LOCATION.get(jsonElement).map(this.registry::get,
                RegistryEntry::isValid, "Could not find " + this.registry.getName() +
                        " for registry name '{previous_value}'.");
    }

}
