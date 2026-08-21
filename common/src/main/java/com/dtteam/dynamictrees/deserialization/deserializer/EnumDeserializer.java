package com.dtteam.dynamictrees.deserialization.deserializer;

import com.dtteam.dynamictrees.deserialization.JsonDeserializers;
import com.dtteam.dynamictrees.deserialization.result.Result;
import com.google.gson.JsonElement;

import java.util.Locale;

/**
 * An {@link JsonDeserializer} for getting the given {@link Enum} of type {@link T} from a {@link JsonElement}.
 *
 * @param <T> The {@link Enum} type.
 * @author Harley O'Connor
 */
public final class EnumDeserializer<T extends Enum<T>> implements JsonDeserializer<T> {

    private final Class<T> enumType;

    public EnumDeserializer(Class<T> enumType) {
        this.enumType = enumType;
    }

    public Result<T, JsonElement> deserialize(JsonElement jsonElement) {
        return JsonDeserializers.STRING.deserialize(jsonElement).map(enumStr -> Enum.valueOf(enumType, enumStr.toUpperCase(Locale.ENGLISH)),
                "Couldn't get enum " + this.enumType + " from value '{previous_value}'.");
    }


}
