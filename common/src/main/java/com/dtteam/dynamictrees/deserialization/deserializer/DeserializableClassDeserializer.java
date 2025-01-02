package com.dtteam.dynamictrees.deserialization.deserializer;

import com.dtteam.dynamictrees.deserialization.DeserializationException;
import com.dtteam.dynamictrees.deserialization.JsonDeserializers;
import com.dtteam.dynamictrees.deserialization.result.Result;
import com.google.gson.JsonElement;

/**
 * @author Harley O'Connor
 */
public final class DeserializableClassDeserializer implements JsonDeserializer<Class<?>> {

    @Override
    public Result<Class<?>, JsonElement> deserialize(JsonElement input) {
        return JsonDeserializers.STRING.deserialize(input)
                .map(typeString -> JsonDeserializers.getDeserializableClasses().stream()
                        .filter(deserialisableClass ->
                                deserialisableClass.getSimpleName().equalsIgnoreCase(typeString) ||
                                        deserialisableClass.getName().equalsIgnoreCase(typeString)
                        )
                        .findFirst()
                        .orElseThrow(() ->
                                new DeserializationException("Could not find deserialisable class with name \"" +
                                        typeString + "\".")
                        ));
    }

}
