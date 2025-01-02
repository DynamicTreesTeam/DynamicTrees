package com.dtteam.dynamictrees.deserialization.deserializer;

import com.dtteam.dynamictrees.deserialization.DeserializationException;
import com.dtteam.dynamictrees.deserialization.result.Result;

import java.util.function.Consumer;

/**
 * A deserializer that handles converting the input object of type {@link I} to an output object of type {@link O}.
 *
 * @param <I> the type of the input to deserialise from
 * @param <O> the type of the output to deserialise to
 * @author Harley O'Connor
 */
public interface Deserializer<I, O> {

    /**
     * Attempts to deserialize the specified {@code input} object to an the output type {@link O}.
     *
     * @param input the input object to deserialise
     * @return the deserialization result
     */
    Result<O, I> deserialize(I input);

    /**
     * Returns {@code true} if this {@link Deserializer} is valid. A deserializer is considered invalid if {@link
     * #deserialize(Object)} always results in a failure.
     *
     * @return {@code true} if this {@link Deserializer} is valid; {@code false} otherwise
     */
    default boolean isValid() {
        return true;
    }

    /**
     * Passes the result of calling {@link #deserialize(Object)} on the specified {@code input} to the specified {@code
     * consumer} if this {@link Deserializer} is considered valid.
     *
     * @param input    the input object to deserialise
     * @param consumer the consumer for the deserialisation result if this {@link Deserializer} is valid
     * @return {@code true} if this {@link Deserializer} is valid; {@code false} otherwise
     * @see #isValid()
     */
    default boolean deserializeIfValid(I input, Consumer<Result<O, I>> consumer) throws DeserializationException {
        if (this.isValid()) {
            consumer.accept(this.deserialize(input));
            return true;
        }
        return false;
    }

}
