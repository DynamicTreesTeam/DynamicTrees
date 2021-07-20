package com.ferreusveritas.dynamictrees.util;

/**
 * Similar to {@link java.util.function.BiFunction}, but takes 3 parameters in apply.
 *
 * @author Harley O'Connor
 */
@FunctionalInterface
public interface TriFunction<T, U, R, S> {

    /**
     * Applies this function to the given arguments.
     *
     * @param t the first function argument
     * @param u the second function argument
     * @return the function result
     */
    S apply(T t, U u, R r);

}