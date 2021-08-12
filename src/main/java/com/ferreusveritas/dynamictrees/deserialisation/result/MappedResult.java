package com.ferreusveritas.dynamictrees.deserialisation.result;

import java.util.Objects;
import java.util.function.Predicate;

/**
 * @param <T> the type of the mapped result
 * @param <I> the type of the original input
 * @author Harley O'Connor
 */
public interface MappedResult<T, I> extends Result<T, I> {

    default <V> MappedResult<T, I> elseMapIfType(Class<V> vClass, SimpleMapper<V, T> mapper) {
        return this.elseMapIfType(vClass, mapper.fullMapper());
    }

    <V> MappedResult<T, I> elseMapIfType(Class<V> vClass, Mapper<V, T> mapper);

    default MappedResult<T, I> elseTypeError() {
        return this.elseError(Objects::nonNull, "Unsupported type for input \"" + this.getInput() +
                "\".");
    }

    MappedResult<T, I> elseError(Predicate<T> validator, String invalidError);

}
