package com.ferreusveritas.dynamictrees.deserialisation.result;

import com.ferreusveritas.dynamictrees.deserialisation.DeserialisationException;
import com.ferreusveritas.dynamictrees.deserialisation.JsonDeserialiser;
import com.ferreusveritas.dynamictrees.deserialisation.JsonDeserialisers;
import com.google.common.collect.Lists;
import com.google.gson.JsonElement;

import javax.annotation.Nullable;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Predicate;

/**
 * @author Harley O'Connor
 */
public class JsonResult<T> extends AbstractResult<T, JsonElement> {

    public JsonResult(JsonElement input, @Nullable T value, @Nullable String error) {
        super(input, value, error);
    }

    public JsonResult(JsonElement input, @Nullable T value, @Nullable String error, List<String> warnings) {
        super(input, value, error, warnings);
    }

    @Override
    public Result<T, JsonElement> removeError() {
        return new JsonResult<>(this.input, this.value, null, this.warnings);
    }

    @Override
    public <V> MappedResult<V, JsonElement> map(Mapper<T, V> mapper) {
        try {
            return this.value == null ? MappedJsonResult.mapErrorneous(this) :
                    MappedJsonResult.map(mapper.apply(this.value, this.warnings::add), this);
        } catch (DeserialisationException e) {
            return MappedJsonResult.errorneousMap(e.getMessage(), this);
        }
    }

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

    @Override
    public <V> MappedResult<V, JsonElement> mapIfValid(Predicate<T> validator, String invalidError, Mapper<T, V> mapper) {
        return this.value == null ? MappedJsonResult.mapErrorneous(this) :
                validator.test(this.value) ? this.map(mapper, "Unexpected error occurred.") :
                        MappedJsonResult.errorneousMap(
                                invalidError.replaceFirst("\\{}", this.value.toString()),
                                this
                        );
    }

    @Override
    public <V, N> MappedResult<N, JsonElement> mapIfType(Class<V> type, Mapper<V, N> mapper) {
        return JsonDeserialisers.getOrThrow(type).deserialise(this.input).map(mapper);
    }

    @Override
    public <E> MappedResult<List<E>, JsonElement> mapToListOfType(Class<E> type) {
        return JsonDeserialisers.JSON_ARRAY.deserialise(this.input).map(array -> {
            final JsonDeserialiser<E> deserialiser = JsonDeserialisers.getOrThrow(type);
            final List<E> list = new LinkedList<>();

            for (JsonElement element : array) {
                list.add(deserialiser.deserialise(element).orElseThrow());
            }

            return list;
        });
    }

    @Override
    public <V, E> MappedResult<List<E>, JsonElement> mapEachIfArray(Class<V> type, Class<E> mappedType, Mapper<V, E> mapper) {
        return JsonDeserialisers.JSON_ARRAY.deserialise(this.input).map(array -> {
            final JsonDeserialiser<V> deserialiser = JsonDeserialisers.getOrThrow(type);
            final List<E> list = new LinkedList<>();

            for (JsonElement element : array) {
                list.add(mapper.apply(deserialiser.deserialise(element).orElseThrow(), this.warnings::add));
            }

            return list;
        });
    }

    public static JsonResult<JsonElement> forInput(JsonElement input) {
        return new JsonResult<>(input, null, null);
    }

    public static <T> JsonResult<T> success(JsonElement json, T value) {
        return new JsonResult<>(json, value, null);
    }

    public static <T> JsonResult<T> failure(JsonElement json, String error) {
        return failure(json, error, Lists.newArrayList());
    }

    public static <T> JsonResult<T> failure(JsonElement json, String error, List<String> warnings) {
        return new JsonResult<>(json, null, error, warnings);
    }

}
