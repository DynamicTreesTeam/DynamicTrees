package com.ferreusveritas.dynamictrees.deserialisation;

import com.google.gson.JsonElement;

import java.util.function.Consumer;

/**
 * A deserialiser for Json that handles converting the input {@link JsonElement} to an output object of type {@link O}.
 *
 * @param <O> the type of the output to deserialise to
 * @author Harley O'Connor
 */
@FunctionalInterface
public interface JsonDeserialiser<O> extends Deserialiser<JsonElement, O> {

    /**
     * {@inheritDoc}
     *
     * @param input    the input object to deserialise
     * @param consumer the consumer for the deserialisation result if this {@link Deserialiser} is valid
     * @return {@code true} if this {@link Deserialiser} is valid; {@code false} otherwise
     * @implNote This implementation assumes this is always valid. Implementing invalid deserialisers should override
     * and return {@code false}.
     */
    @Override
    default boolean deserialiseIfValid(JsonElement input, Consumer<DeserialisationResult<O>> consumer) {
        consumer.accept(this.deserialise(input));
        return true;
    }

}
