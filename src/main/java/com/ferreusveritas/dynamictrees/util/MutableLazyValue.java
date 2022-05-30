package com.ferreusveritas.dynamictrees.util;

import javax.annotation.Nonnull;
import java.util.function.Supplier;

/**
 * @author Harley O'Connor
 */
public interface MutableLazyValue<T> {

    T get();

    void reset(Supplier<T> supplier);

    void set(@Nonnull T value);

    static <T> MutableLazyValue<T> supplied(Supplier<T> supplier) {
        return new MutableSuppliedLazyValue<>(supplier);
    }

}