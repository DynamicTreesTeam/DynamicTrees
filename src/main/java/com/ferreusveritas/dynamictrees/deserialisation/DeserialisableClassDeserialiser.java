package com.ferreusveritas.dynamictrees.deserialisation;

import com.ferreusveritas.dynamictrees.deserialisation.result.Result;
import com.google.gson.JsonElement;

/**
 * @author Harley O'Connor
 */
public final class DeserialisableClassDeserialiser implements JsonDeserialiser<Class<?>> {

    @Override
    public Result<Class<?>, JsonElement> deserialise(JsonElement input) {
        return JsonDeserialisers.STRING.deserialise(input)
                .map(typeString -> JsonDeserialisers.getDeserialisableClasses().stream()
                        .filter(deserialisableClass ->
                                deserialisableClass.getSimpleName().equalsIgnoreCase(typeString) ||
                                        deserialisableClass.getName().equalsIgnoreCase(typeString)
                        )
                        .findFirst()
                        .orElseThrow(() ->
                                new DeserialisationException("Could not find deserialisable class with name \"" +
                                        typeString + "\".")
                        ));
    }

}
