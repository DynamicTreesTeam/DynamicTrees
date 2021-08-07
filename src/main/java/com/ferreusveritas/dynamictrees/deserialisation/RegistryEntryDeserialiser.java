package com.ferreusveritas.dynamictrees.deserialisation;

import com.ferreusveritas.dynamictrees.api.registry.Registry;
import com.ferreusveritas.dynamictrees.api.registry.RegistryEntry;
import com.google.gson.JsonElement;

/**
 * Gets {@link RegistryEntry} object of type {@link T} from the given {@link Registry} object.
 *
 * @author Harley O'Connor
 */
public final class RegistryEntryDeserialiser<T extends RegistryEntry<T>> implements JsonDeserialiser<T> {

    private final Registry<T> registry;

    public RegistryEntryDeserialiser(Registry<T> registry) {
        this.registry = registry;
    }

    @Override
    public DeserialisationResult<T> deserialise(JsonElement jsonElement) {
        return JsonDeserialisers.DT_RESOURCE_LOCATION.deserialise(jsonElement).map(this.registry::get,
                RegistryEntry::isValid, "Could not find " + this.registry.getName() +
                        " for registry name '{previous_value}'.");
    }

}
