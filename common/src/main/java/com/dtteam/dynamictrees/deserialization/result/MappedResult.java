package com.dtteam.dynamictrees.deserialization.result;

import com.dtteam.dynamictrees.deserialization.NoSuchDeserializerException;

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
     * If this is not already successful, attempts to deserialize the original input value as the specified {@code
     * type}, mapping it using the specified {@code mapper} if successful and returning the resulting {@link
     * MappedResult}.
     *
     * @param type the type to attempt to deserialize
     * @param mapper a mapper that maps the deserialized value to a new value
     * @param <V> the type to attempt to deserialize
     * @return the mapped result
     * @throws NoSuchDeserializerException if the specified {@code type} did not have a registered deserializer
     */
    default <V> MappedResult<T, I> elseMapIfType(Class<V> type, SimpleMapper<V, T> mapper) {
        return this.elseMapIfType(type, mapper.fullMapper());
    }

    /**
     * If this is not already successful, attempts to deserialize the original input value as the specified {@code
     * type}, mapping it using the specified {@code mapper} if successful and returning the resulting {@link
     * MappedResult}.
     *
     * @param type the type to attempt to deserialize
     * @param mapper a mapper that maps the deserialized value to a new value
     * @param <V> the type to attempt to deserialize
     * @return the mapped result
     * @throws NoSuchDeserializerException if the specified {@code type} did not have a registered deserializer
     */
    <V> MappedResult<T, I> elseMapIfType(Class<V> type, Mapper<V, T> mapper);

    /**
     * If this is not already successful, gets the value for the specified {@code key} if the input is a map-like
     * structure, attempting to map it to the specified {@code type} and then to type {@link T} using the specified
     * {@code mapper}.
     *
     * @param key the key for the value to map
     * @param type the required type to be mapped
     * @param mapper a mapper that maps the deserialized value to type {@link T}
     * @param <V> the type to map the deserialized value to
     * @return the mapped result
     */
    default <V> MappedResult<T, I> elseMapIfContains(String key, Class<V> type, SimpleMapper<V, T> mapper) {
        return this.elseMapIfContains(key, type, mapper.fullMapper());
    }

    /**
     * If this is not already successful, gets the value for the specified {@code key} if the input is a map-like
     * structure, attempting to map it to the specified {@code type} and then to type {@link T} using the specified
     * {@code mapper}.
     *
     * @param key the key for the value to map
     * @param type the required type to be mapped
     * @param mapper a mapper that maps the deserialized value to type {@link T}
     * @param <V> the type to map the deserialized value to
     * @return the mapped result
     */
    <V> MappedResult<T, I> elseMapIfContains(String key, Class<V> type, Mapper<V, T> mapper);

    /**
     * If this is not already successful, gets the value for the specified {@code key} if the input is a map-like
     * structure, attempting to map it to the specified {@code type} and then to type {@link T} using the specified
     * {@code mapper}.
     *
     * @param key the key for the value to map
     * @param type the required type to be mapped
     * @param mapper a mapper that maps the deserialized value to type {@link T}
     * @param defaultValue the value to use if the map-like structure doesn't contain the {@code key}
     * @param <V> the type to map the deserialized value to
     * @return the mapped result
     */
    default <V> MappedResult<T, I> elseMapIfContains(String key, Class<V> type, SimpleMapper<V, T> mapper,
                                                     T defaultValue) {
        return this.mapIfContains(key, type, mapper.fullMapper(), defaultValue);
    }

    /**
     * If this is not already successful, gets the value for the specified {@code key} if the input is a map-like
     * structure, attempting to map it to the specified {@code type} and then to type {@link T} using the specified
     * {@code mapper}.
     *
     * @param key the key for the value to map
     * @param type the required type to be mapped
     * @param mapper a mapper that maps the deserialized value to type {@link T}
     * @param defaultValue the value to use if the map-like structure doesn't contain the {@code key}
     * @param <V> the type to map the deserialized value to
     * @return the mapped result
     */
    <V> MappedResult<T, I> elseMapIfContains(String key, Class<V> type, Mapper<V, T> mapper, T defaultValue);

    /**
     * Sets a type error as this result's error if a value could not be deserialized from the input.
     *
     * @return the mapped result
     */
    default MappedResult<T, I> elseTypeError() {
        return this.elseError(Objects::nonNull, "Unsupported type for input " + this.getInput() + ".");
    }

    /**
     * Sets the specified {@code invalidError} as this result's error if the deserialized value does not pass the
     * specified {@code validator}.
     *
     * @param validator the predicate by which to test the deserialized value
     * @param invalidError the error message to set if the {@code validator} is not passed
     * @return the mapped result
     */
    MappedResult<T, I> elseError(Predicate<T> validator, String invalidError);

}
