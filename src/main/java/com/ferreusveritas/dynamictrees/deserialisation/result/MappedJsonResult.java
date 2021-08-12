package com.ferreusveritas.dynamictrees.deserialisation.result;

import com.google.gson.JsonElement;

import javax.annotation.Nullable;
import java.util.List;
import java.util.function.Predicate;

/**
 * @author Harley O'Connor
 */
public class MappedJsonResult<T> extends JsonResult<T> implements MappedResult<T, JsonElement> {

    public MappedJsonResult(JsonElement input, @Nullable T value, @Nullable String error) {
        super(input, value, error);
    }

    public MappedJsonResult(JsonElement input, @Nullable T value, @Nullable String error, List<String> warnings) {
        super(input, value, error, warnings);
    }

    @Override
    public <V> MappedResult<T, JsonElement> elseMapIfType(Class<V> vClass, Mapper<V, T> mapper) {
        return this.value != null ? map(this.value, this) : super.mapIfType(vClass, mapper);
    }

    @Override
    public MappedResult<T, JsonElement> elseError(Predicate<T> validator, String invalidError) {
        return validator.test(this.value) ? this : errorneousMap(invalidError, this);
    }

    static <T> MappedJsonResult<T> mapErrorneous(JsonResult<?> result) {
        return new MappedJsonResult<>(result.input, null, result.error, result.warnings);
    }

    static <V> MappedJsonResult<V> map(V value, JsonResult<?> unmapped) {
        return new MappedJsonResult<>(unmapped.input, value, null, unmapped.warnings);
    }

    static <T> MappedJsonResult<T> errorneousMap(String error, JsonResult<?> unmapped) {
        return new MappedJsonResult<>(unmapped.input, null, error, unmapped.warnings);
    }

}
