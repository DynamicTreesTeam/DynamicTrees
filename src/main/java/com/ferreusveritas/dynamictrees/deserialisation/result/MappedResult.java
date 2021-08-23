package com.ferreusveritas.dynamictrees.deserialisation.result;

import com.ferreusveritas.dynamictrees.deserialisation.NoSuchDeserialiserException;

import java.util.Objects;
import java.util.function.Predicate;

/**
 * Represents a {@link Result} whose value has been mapped.
 *
 * @param <T> the type of the mapped result
 * @param <I> the type of the original input
 * @author Harley O'Connor
 */
public interface MappedResult<T, I> extends Result<T, I> {

    /**
     * If this is not already successful, attempts to deserialise the original input value as the specified {@code
     * type}, mapping it using the specified {@code mapper} if successful and returning the resulting {@link
     * MappedResult}.
     *
     * @param type the type to attempt to deserialise
     * @param mapper a mapper that maps the deserialised value to a new value
     * @param <V> the type to attempt to deserialise
     * @return the mapped result
     * @throws NoSuchDeserialiserException if the specified {@code type} did not have a registered deserialiser
     */
    default <V> MappedResult<T, I> elseMapIfType(Class<V> type, SimpleMapper<V, T> mapper) {
        return this.elseMapIfType(type, mapper.fullMapper());
    }

    /**
     * If this is not already successful, attempts to deserialise the original input value as the specified {@code
     * type}, mapping it using the specified {@code mapper} if successful and returning the resulting {@link
     * MappedResult}.
     *
     * @param type the type to attempt to deserialise
     * @param mapper a mapper that maps the deserialised value to a new value
     * @param <V> the type to attempt to deserialise
     * @return the mapped result
     * @throws NoSuchDeserialiserException if the specified {@code type} did not have a registered deserialiser
     */
    <V> MappedResult<T, I> elseMapIfType(Class<V> type, Mapper<V, T> mapper);

    /**
     * Sets a type error as this result's error if a value could not be deserialised from the input.
     *
     * @return the mapped result
     */
    default MappedResult<T, I> elseTypeError() {
        return this.elseError(Objects::nonNull, "Unsupported type for input \"" + this.getInput() +
                "\".");
    }

    /**
     * Sets the specified {@code invalidError} as this result's error if the deserialised value does not pass the
     * specified {@code validator}.
     *
     * @param validator the predicate by which to test the deserialised value
     * @param invalidError the error message to set if the {@code validator} is not passed
     * @return the mapped result
     */
    MappedResult<T, I> elseError(Predicate<T> validator, String invalidError);

}
