package com.ferreusveritas.dynamictrees.util.function;

/**
 * Similar to {@link java.util.function.BiFunction}, but takes 4 parameters in apply.
 */
@FunctionalInterface
public interface TetraFunction<T, U, V, S, R> {
    R apply(T t, U u, V v, S r);
}
