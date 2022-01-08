package com.ferreusveritas.dynamictrees.deserialisation;

import com.ferreusveritas.dynamictrees.deserialisation.result.Result;

import java.util.function.Consumer;

/**
 * A deserialiser that handles converting the input object of type {@link I} to an output object of type {@link O}.
 *
 * @param <I> the type of the input to deserialise from
 * @param <O> the type of the output to deserialise to
 * @author Harley O'Connor
 */
public interface Deserialiser<I, O> {

    /**
     * Attempts to deserialise the specified {@code input} object to an the output type {@link O}.
     *
     * @param input the input object to deserialise
     * @return the deserialisation result
     */
    Result<O, I> deserialise(I input);

    /**
     * Returns {@code true} if this {@link Deserialiser} is valid. A deserialiser is considered invalid if {@link
     * #deserialise(Object)} always results in a failure.
     *
     * @return {@code true} if this {@link Deserialiser} is valid; {@code false} otherwise
     */
    default boolean isValid() {
        return true;
    }

    /**
     * Passes the result of calling {@link #deserialise(Object)} on the specified {@code input} to the specified {@code
     * consumer} if this {@link Deserialiser} is considered valid.
     *
     * @param input    the input object to deserialise
     * @param consumer the consumer for the deserialisation result if this {@link Deserialiser} is valid
     * @return {@code true} if this {@link Deserialiser} is valid; {@code false} otherwise
     * @see #isValid()
     */
    default boolean deserialiseIfValid(I input, Consumer<Result<O, I>> consumer) throws DeserialisationException {
        if (this.isValid()) {
            consumer.accept(this.deserialise(input));
            return true;
        }
        return false;
    }

}
