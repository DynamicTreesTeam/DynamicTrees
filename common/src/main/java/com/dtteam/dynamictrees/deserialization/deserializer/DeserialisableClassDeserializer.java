package com.dtteam.dynamictrees.deserialization.deserializer;

import com.dtteam.dynamictrees.deserialization.DeserializationException;
import com.dtteam.dynamictrees.deserialization.JsonDeserializers;
import com.dtteam.dynamictrees.deserialization.result.Result;
import com.google.gson.JsonElement;

/**
 * @author Harley O'Connor
 */
public final class DeserialisableClassDeserializer implements JsonDeserializer<Class<?>> {

    @Override
    public Result<Class<?>, JsonElement> deserialise(JsonElement input) {
        return JsonDeserializers.STRING.deserialise(input)
                .map(typeString -> JsonDeserializers.getDeserialisableClasses().stream()
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
