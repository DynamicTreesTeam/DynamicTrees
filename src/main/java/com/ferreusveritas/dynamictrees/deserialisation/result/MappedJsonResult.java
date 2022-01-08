package com.ferreusveritas.dynamictrees.deserialisation.result;

import com.ferreusveritas.dynamictrees.deserialisation.NoSuchDeserialiserException;
import com.google.gson.JsonElement;

import javax.annotation.Nullable;
import java.util.List;
import java.util.function.Predicate;

/**
 * An implementation of {@link MappedResult} for mapped {@link JsonResult}s.
 *
 * @param <T> the type of the mapped value
 * @author Harley O'Connor
 */
public class MappedJsonResult<T> extends JsonResult<T> implements MappedResult<T, JsonElement> {

    public MappedJsonResult(JsonElement input, @Nullable T value, @Nullable String error) {
        super(input, value, error);
    }

    public MappedJsonResult(JsonElement input, @Nullable T value, @Nullable String error, List<String> warnings) {
        super(input, value, error, warnings);
    }

    /**
     * {@inheritDoc}
     *
     * @param type the type to attempt to deserialise
     * @param mapper a mapper that maps the deserialised value to a new value
     * @param <V> the type to attempt to deserialise
     * @return the mapped result
     * @throws NoSuchDeserialiserException if the specified {@code type} did not have a registered deserialiser
     */
    @Override
    public <V> MappedResult<T, JsonElement> elseMapIfType(Class<V> type, Mapper<V, T> mapper) {
        return this.value != null ? map(this.value, this) : super.mapIfType(type, mapper);
    }

    /**
     * {@inheritDoc}
     *
     * @param key the key for the value to map
     * @param type the required type to be mapped
     * @param mapper a mapper that maps the deserialised value to type {@link T}
     * @param <V> the type to map the deserialised value to
     * @return the mapped result
     */
    @Override
    public <V> MappedResult<T, JsonElement> elseMapIfContains(String key, Class<V> type, Mapper<V, T> mapper) {
        return this.value != null ? map(this.value, this) : super.mapIfContains(key, type, mapper);
    }

    /**
     * {@inheritDoc}
     *
     * @param key the key for the value to map
     * @param type the required type to be mapped
     * @param mapper a mapper that maps the deserialised value to type {@link T}
     * @param defaultValue the value to use if the map-like structure doesn't contain the {@code key}
     * @param <V> the type to map the deserialised value to
     * @return the mapped result
     */
    @Override
    public <V> MappedResult<T, JsonElement> elseMapIfContains(String key, Class<V> type, Mapper<V, T> mapper,
                                                              T defaultValue) {
        return this.value != null ? map(this.value, this) :
                super.mapIfContains(key, type, mapper, defaultValue);
    }

    /**
     * {@inheritDoc}
     *
     * @param validator the predicate by which to test the deserialised value
     * @param invalidError the error message to set if the {@code validator} is not passed
     * @return the mapped result
     */
    @Override
    public MappedResult<T, JsonElement> elseError(Predicate<T> validator, String invalidError) {
        return validator.test(this.value) ? this : errorneousMap(invalidError, this);
    }

    /**
     * Creates a mapped json result from the specified {@code result} that is already errorneous, copying the input,
     * error, and warnings.
     *
     * @param result the result to map from
     * @param <T> the type to map to
     * @return the mapped json result
     */
    static <T> MappedJsonResult<T> mapErrorneous(JsonResult<?> result) {
        return new MappedJsonResult<>(result.input, null, result.error, result.warnings);
    }

    /**
     * Creates a mapped json result for the specified {@code value} with the specified {@code unmapped} result's input
     * and warnings.
     *
     * @param value the mapped value
     * @param unmapped the result to map from
     * @param <V> the type to map to
     * @return the mapped json result
     */
    static <V> MappedJsonResult<V> map(V value, JsonResult<?> unmapped) {
        return new MappedJsonResult<>(unmapped.input, value, null, unmapped.warnings);
    }

    /**
     * Creates an errorneous mapped json result for the specified {@code error} message with the specified {@code
     * unmapped} result's input and warnings.
     *
     * @param error the error message
     * @param unmapped the result to map from
     * @param <T> the type to map to
     * @return the mapped json result
     */
    static <T> MappedJsonResult<T> errorneousMap(String error, JsonResult<?> unmapped) {
        return new MappedJsonResult<>(unmapped.input, null, error, unmapped.warnings);
    }

}
