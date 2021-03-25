package com.ferreusveritas.dynamictrees.util.json;

import com.google.gson.JsonElement;

/**
 * Handles getting an {@link Object} of type {@link T} from a {@link JsonElement}.
 *
 * @param <T> The type of the {@link Object} to be fetched.
 * @author Harley O'Connor
 */
@FunctionalInterface
public interface IJsonObjectGetter<T> {

    default boolean isValid() {
        return true;
    }

    /**
     * Attempts to fetch the object from the given {@link JsonElement}.
     *
     * @param jsonElement The {@link JsonElement}.
     * @return An {@link ObjectFetchResult}, containing an object obtained or an error message if it failed.
     */
    ObjectFetchResult<T> get (final JsonElement jsonElement);

}
