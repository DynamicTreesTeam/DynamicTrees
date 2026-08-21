package com.dtteam.dynamictrees.deserialization.deserializer;

import com.dtteam.dynamictrees.api.registry.Registry;
import com.dtteam.dynamictrees.api.registry.RegistryEntry;
import com.dtteam.dynamictrees.api.registry.SimpleRegistry;
import com.dtteam.dynamictrees.deserialization.JsonDeserializers;
import com.dtteam.dynamictrees.deserialization.result.Result;
import com.google.gson.JsonElement;

/**
 * Gets {@link RegistryEntry} object of type {@link T} from the given {@link SimpleRegistry} object.
 *
 * @author Harley O'Connor
 */
public final class RegistryEntryDeserializer<T extends RegistryEntry<T>> implements JsonDeserializer<T> {

    private final Registry<T> registry;

    public RegistryEntryDeserializer(Registry<T> registry) {
        this.registry = registry;
    }

    public Result<T, JsonElement> deserialize(JsonElement jsonElement) {
        return JsonDeserializers.DT_RESOURCE_LOCATION.deserialize(jsonElement)
                .map(
                        this.registry::get,
                        RegistryEntry::isValid,
                        "Could not find " + this.registry.getName() + " for registry name '{}'."
                );
    }

}
