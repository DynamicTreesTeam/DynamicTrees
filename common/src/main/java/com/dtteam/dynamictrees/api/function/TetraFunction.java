package com.dtteam.dynamictrees.api.function;

/**
 * Similar to {@link java.util.function.BiFunction}, but takes 4 parameters in apply.
 */
@FunctionalInterface
public interface TetraFunction<T, U, V, S, R> {
    R apply(T t, U u, V v, S r);
}
