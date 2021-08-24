package com.ferreusveritas.dynamictrees.deserialisation.result;

import com.ferreusveritas.dynamictrees.deserialisation.DeserialisationException;
import com.ferreusveritas.dynamictrees.deserialisation.JsonDeserialiser;
import com.ferreusveritas.dynamictrees.deserialisation.JsonDeserialisers;
import com.ferreusveritas.dynamictrees.deserialisation.NoSuchDeserialiserException;
import com.google.common.collect.Lists;
import com.google.gson.JsonElement;

import javax.annotation.Nullable;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Predicate;

/**
 * An implementation of {@link Result} for deserialised {@link JsonElement}s.
 *
 * @param <T> the type of the deserialised value
 * @author Harley O'Connor
 */
public class JsonResult<T> extends AbstractResult<T, JsonElement> {

    public JsonResult(JsonElement input, @Nullable T value, @Nullable String error) {
        super(input, value, error);
    }

    public JsonResult(JsonElement input, @Nullable T value, @Nullable String error, List<String> warnings) {
        super(input, value, error, warnings);
    }

    /**
     * {@inheritDoc}
     *
     * @return this result for chaining
     */
    @Override
    public Result<T, JsonElement> removeError() {
        return new JsonResult<>(this.input, this.value, null, this.warnings);
    }

    /**
     * {@inheritDoc}
     *
     * @param mapper a mapper that maps the deserialised value to a new value
     * @param <V> the type to map to
     * @return the mapped result
     */
    @Override
    public <V> MappedResult<V, JsonElement> map(Mapper<T, V> mapper) {
        try {
            return this.value == null ? MappedJsonResult.mapErrorneous(this) :
                    MappedJsonResult.map(mapper.apply(this.value, this.warnings::add), this);
        } catch (DeserialisationException e) {
            return MappedJsonResult.errorneousMap(e.getMessage(), this);
        }
    }

    /**
     * {@inheritDoc}
     *
     * @param mapper a mapper that maps the deserialised value to a new value
     * @param validator the predicate by which to test the mapped value
     * @param invalidError the error message to use if the mapped deserialised value does not pass the {@code validator}
     * @param <V> the type to map to
     * @return the mapped result
     */
    @Override
    public <V> MappedResult<V, JsonElement> map(Mapper<T, V> mapper, Predicate<V> validator, String invalidError) {
        if (this.value == null) {
            return MappedJsonResult.mapErrorneous(this);
        }

        try {
            final V value = mapper.apply(this.value, this.warnings::add);
            return validator.test(value) ? MappedJsonResult.map(value, this) :
                    MappedJsonResult.errorneousMap(
                            invalidError.replaceFirst("\\{}", this.value.toString()),
                            this
                    );
        } catch (DeserialisationException e) {
            return MappedJsonResult.errorneousMap(e.getMessage(), this);
        }
    }

    /**
     * {@inheritDoc}
     *
     * @param validator the predicate by which to test the deserialised value
     * @param invalidError the error message to use if this value does not pass the {@code validator}
     * @param mapper a mapper that maps the deserialised value to a new value
     * @param <V> the type to map to
     * @return the mapped result
     */
    @Override
    public <V> MappedResult<V, JsonElement> mapIfValid(Predicate<T> validator, String invalidError, Mapper<T, V> mapper) {
        return this.value == null ? MappedJsonResult.mapErrorneous(this) :
                validator.test(this.value) ?
                        this.map(mapper, "Unexpected error occurred. This should not be possible.") :
                        MappedJsonResult.errorneousMap(
                                invalidError.replaceFirst("\\{}", this.value.toString()),
                                this
                        );
    }

    /**
     * {@inheritDoc}
     *
     * @param type the type to attempt to deserialise
     * @param mapper a mapper that maps the deserialised value to a new value
     * @param <V> the type to attempt to deserialise
     * @param <N> the type to map to
     * @return the mapped result
     * @throws NoSuchDeserialiserException if the specified {@code type} did not have a registered deserialiser
     */
    @Override
    public <V, N> MappedResult<N, JsonElement> mapIfType(Class<V> type, Mapper<V, N> mapper) {
        return JsonDeserialisers.getOrThrow(type).deserialise(this.input).map(mapper);
    }

    /**
     * {@inheritDoc}
     *
     * @param elementType the type of element to map to a list of
     * @param <E> the type of the element of the list to map to
     * @return the mapped result
     * @throws NoSuchDeserialiserException if the specified {@code elementType} did not have a registered deserialiser
     */
    @Override
    public <E> MappedResult<List<E>, JsonElement> mapToListOfType(Class<E> elementType) {
        return JsonDeserialisers.JSON_ARRAY.deserialise(this.input).map(array -> {
            final JsonDeserialiser<E> deserialiser = JsonDeserialisers.getOrThrow(elementType);
            final List<E> list = new LinkedList<>();

            for (JsonElement element : array) {
                list.add(deserialiser.deserialise(element).orElseThrow());
            }

            return list;
        });
    }

    /**
     * {@inheritDoc}
     *
     * @param elementType the initial type of element to map to a list of
     * @param mappedType the type to map each element to
     * @param mapper a mapper that maps each deserialised value to the {@code mappedType}
     * @param <V> the initial type of element to map to a list of
     * @param <E> the type to map each element to
     * @return the mapper result
     * @throws NoSuchDeserialiserException if the specified {@code elementType} did not have a registered deserialiser
     */
    @Override
    public <V, E> MappedResult<List<E>, JsonElement> mapEachIfArray(Class<V> elementType, Class<E> mappedType, Mapper<V, E> mapper) {
        return JsonDeserialisers.JSON_ARRAY.deserialise(this.input).map(array -> {
            final JsonDeserialiser<V> deserialiser = JsonDeserialisers.getOrThrow(elementType);
            final List<E> list = new LinkedList<>();

            for (JsonElement element : array) {
                list.add(mapper.apply(deserialiser.deserialise(element).orElseThrow(), this.warnings::add));
            }

            return list;
        });
    }

    /**
     * {@inheritDoc}
     *
     * @param key the key for the value to map
     * @param type the required type to be mapped
     * @param mapper a mapper that maps the deserialised value to type {@link V}
     * @param <E> the type to attempt to deserialise the key's corresponding value to
     * @param <V> the type to map the deserialised value to
     * @return the mapped result
     */
    @Override
    public <E, V> MappedResult<V, JsonElement> mapIfContains(String key, Class<E> type, Mapper<E, V> mapper) {
        return JsonDeserialisers.JSON_OBJECT.deserialise(this.input).map(object -> {
            final JsonElement element = object.get(key);

            if (element == null) {
                throw new DeserialisationException("No value for key \"" + key + "\".");
            }

            return mapper.apply(
                    JsonDeserialisers.getOrThrow(type).deserialise(element).orElseThrow(),
                    this.warnings::add
            );
        });
    }

    @Override
    public <E, V> MappedResult<V, JsonElement> mapIfContains(String key, Class<E> type, Mapper<E, V> mapper, V defaultValue) {
        return JsonDeserialisers.JSON_OBJECT.deserialise(this.input).map(object -> {
            final JsonElement element = object.get(key);

            if (element == null) {
                return defaultValue;
            }

            return mapper.apply(
                    JsonDeserialisers.getOrThrow(type).deserialise(element).orElseThrow(),
                    this.warnings::add
            );
        });
    }

    /**
     * Creates an empty json result for the specified {@code input}. This can be used for mapping the input using {@link
     * #mapIfType(Class, SimpleMapper)}.
     *
     * @param input the input json element
     * @return the empty json result
     */
    public static JsonResult<JsonElement> forInput(JsonElement input) {
        return new JsonResult<>(input, null, null);
    }

    /**
     * Creates a successful json result for the specified {@code json} input and {@code value}.
     *
     * @param json the original json input
     * @param value the deserialised value
     * @param <T> the type of the deserialised value
     * @return the successful json result
     */
    public static <T> JsonResult<T> success(JsonElement json, T value) {
        return new JsonResult<>(json, value, null);
    }

    /**
     * Creates a failure json result for the specified {@code json} input and {@code error}.
     *
     * @param json the original json input
     * @param error the error message
     * @param <T> the type of the deserialised value
     * @return the failure json result
     */
    public static <T> JsonResult<T> failure(JsonElement json, String error) {
        return failure(json, error, Lists.newArrayList());
    }

    /**
     * Creates a failure json result for the specified {@code json} input, {@code error}, and {@code warnings}.
     *
     * @param json the original json input
     * @param error the error message
     * @param warnings the warnings
     * @param <T> the type of the deserialised value
     * @return the failure json result
     */
    public static <T> JsonResult<T> failure(JsonElement json, String error, List<String> warnings) {
        return new JsonResult<>(json, null, error, warnings);
    }

}
