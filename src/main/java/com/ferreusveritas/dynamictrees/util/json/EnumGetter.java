package com.ferreusveritas.dynamictrees.util.json;

import com.electronwill.nightconfig.core.EnumGetMethod;
import com.google.gson.JsonElement;

/**
 * An {@link JsonGetter} for getting the given {@link Enum} of type {@link T} from a {@link JsonElement}.
 *
 * @param <T> The {@link Enum} type.
 * @author Harley O'Connor
 */
public final class EnumGetter<T extends Enum<T>> implements JsonGetter<T> {

    private final Class<T> enumType;

    public EnumGetter(Class<T> enumType) {
        this.enumType = enumType;
    }

    @Override
    public FetchResult<T> get(JsonElement jsonElement) {
        return JsonGetters.STRING.get(jsonElement).map(enumStr -> EnumGetMethod.NAME_IGNORECASE.get(enumStr, this.enumType),
                "Couldn't get enum " + this.enumType + " from value '{previous_value}'.");
    }


}
