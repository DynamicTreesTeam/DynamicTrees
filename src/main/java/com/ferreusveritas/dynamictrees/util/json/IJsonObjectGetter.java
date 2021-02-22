package com.ferreusveritas.dynamictrees.util.json;

import com.google.gson.JsonElement;

/**
 * Handles getting an object of type <tt>T</tt> from a {@link JsonElement}.
 *
 * @author Harley O'Connor
 */
public interface IJsonObjectGetter<T> {

    /**
     * Attempts to fetch the object from the given {@link JsonElement}.
     *
     * @param jsonElement The {@link JsonElement}.
     * @return An {@link ObjectFetchResult}, containing an object obtained or an error message if it failed.
     */
    ObjectFetchResult<T> get (final JsonElement jsonElement);

}
