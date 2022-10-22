package com.ferreusveritas.dynamictrees.deserialisation;

import com.ferreusveritas.dynamictrees.api.configuration.PropertyDefinition;
import com.ferreusveritas.dynamictrees.deserialisation.result.Result;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.util.function.Consumer;

/**
 * @author Harley O'Connor
 */
public final class PropertyDefinitionDeserialiser implements JsonDeserialiser<PropertyDefinition<?>> {

    @Override
    public Result<PropertyDefinition<?>, JsonElement> deserialise(JsonElement input) {
        return JsonDeserialisers.JSON_OBJECT.deserialise(input)
                .map(this::deserialiseDefinition);
    }

    private <T> PropertyDefinition<T> deserialiseDefinition(JsonObject object, Consumer<String> warningAppender)
            throws DeserialisationException {

        final String key = object.get("key").getAsString();
        @SuppressWarnings("unchecked")
        final Class<T> type = (Class<T>) JsonDeserialisers.DESERIALISABLE_CLASS.deserialise(object.get("type"))
                .forEachWarning(warningAppender)
                .orElseThrow();
        final T defaultValue = JsonDeserialisers.get(type).deserialise(object.get("default"))
                .forEachWarning(warningAppender)
                .orElse(null);

        return new PropertyDefinition<>(key, type, defaultValue);
    }

}
