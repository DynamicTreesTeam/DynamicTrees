package com.ferreusveritas.dynamictrees.util.json;

import com.electronwill.nightconfig.core.EnumGetMethod;
import com.google.gson.JsonElement;

/**
 * An {@link IJsonObjectGetter} for getting the given {@link Enum} of type {@link T} from a {@link JsonElement}.
 *
 * @param <T> The {@link Enum} type.
 * @author Harley O'Connor
 */
public final class EnumGetter<T extends Enum<T>> implements IJsonObjectGetter<T> {

    private final Class<T> enumType;

    public EnumGetter(Class<T> enumType) {
        this.enumType = enumType;
    }

    @Override
    public ObjectFetchResult<T> get(JsonElement jsonElement) {
        return JsonObjectGetters.STRING.get(jsonElement).map(enumStr -> EnumGetMethod.NAME_IGNORECASE.get(enumStr, this.enumType),
                "Couldn't get enum " + this.enumType + " from value '{previous_value}'.");
    }


}
