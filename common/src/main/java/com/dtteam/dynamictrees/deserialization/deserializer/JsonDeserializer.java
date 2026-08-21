package com.dtteam.dynamictrees.deserialization.deserializer;

import com.dtteam.dynamictrees.deserialization.result.Result;
import com.google.gson.JsonElement;

import java.util.function.Consumer;

/**
 * A deserialiser for Json that handles converting the input {@link JsonElement} to an output object of type {@link O}.
 *
 * @param <O> the type of the output to deserialise to
 * @author Harley O'Connor
 */
@FunctionalInterface
public interface JsonDeserializer<O> extends Deserializer<JsonElement, O> {

    /**
     * {@inheritDoc}
     *
     * @param input    the input object to deserialize
     * @param consumer the consumer for the deserialization result if this {@link Deserializer} is valid
     * @return {@code true} if this {@link Deserializer} is valid; {@code false} otherwise
     * This implementation assumes this is always valid. Implementing invalid deserializers should override
     * and return {@code false}.
     */
    default boolean deserializeIfValid(JsonElement input, Consumer<Result<O, JsonElement>> consumer) {
        consumer.accept(this.deserialize(input));
        return true;
    }

}
