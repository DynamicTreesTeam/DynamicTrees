package com.dtteam.dynamictrees.api.function;

import org.apache.commons.lang3.function.TriFunction;

import java.util.Objects;
import java.util.function.Function;

/**
 * Similar to {@link java.util.function.BiFunction}, but takes 4 parameters in apply.
 */
@FunctionalInterface
public interface TriPredicate<T, U, V> extends TriFunction<T, U, V, Boolean> {
    default Boolean test(T t, U u, V v) {
        return apply(t, u, v);
    }
}
