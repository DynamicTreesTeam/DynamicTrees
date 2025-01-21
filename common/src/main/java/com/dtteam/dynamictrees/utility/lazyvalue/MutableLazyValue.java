package com.dtteam.dynamictrees.utility.lazyvalue;

import org.jetbrains.annotations.NotNull;

import java.util.function.Supplier;

/**
 * @author Harley O'Connor
 */
public interface MutableLazyValue<T> {

    T get();

    void reset(Supplier<T> supplier);

    void set(@NotNull T value);

    static <T> MutableLazyValue<T> supplied(Supplier<T> supplier) {
        return new MutableSuppliedLazyValue<>(supplier);
    }

}