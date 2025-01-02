package com.dtteam.dynamictrees.deserialization.deserializer;

import com.dtteam.dynamictrees.api.configuration.PropertyDefinition;
import com.dtteam.dynamictrees.deserialization.DeserializationException;
import com.dtteam.dynamictrees.deserialization.JsonDeserializers;
import com.dtteam.dynamictrees.deserialization.result.Result;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.util.function.Consumer;

/**
 * @author Harley O'Connor
 */
public final class PropertyDefinitionDeserializer implements JsonDeserializer<PropertyDefinition<?>> {

    @Override
    public Result<PropertyDefinition<?>, JsonElement> deserialize(JsonElement input) {
        return JsonDeserializers.JSON_OBJECT.deserialize(input)
                .map(this::deserializeDefinition);
    }

    private <T> PropertyDefinition<T> deserializeDefinition(JsonObject object, Consumer<String> warningAppender)
            throws DeserializationException {

        final String key = object.get("key").getAsString();
        @SuppressWarnings("unchecked")
        final Class<T> type = (Class<T>) JsonDeserializers.DESERIALIZABLE_CLASS.deserialize(object.get("type"))
                .forEachWarning(warningAppender)
                .orElseThrow();
        final T defaultValue = JsonDeserializers.get(type).deserialize(object.get("default"))
                .forEachWarning(warningAppender)
                .orElse(null);

        return new PropertyDefinition<>(key, type, defaultValue);
    }

}
