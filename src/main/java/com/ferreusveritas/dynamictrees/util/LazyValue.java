package com.ferreusveritas.dynamictrees.util;

import java.util.function.Supplier;

/**
 * @author Harley O'Connor
 */
@FunctionalInterface
public interface LazyValue<T> {

    T get();

    static <T> LazyValue<T> of(T value) {
        return () -> value;
    }

    static <T> LazyValue<T> supplied(Supplier<T> supplier) {
        return new SuppliedLazyValue<>(supplier);
    }

}