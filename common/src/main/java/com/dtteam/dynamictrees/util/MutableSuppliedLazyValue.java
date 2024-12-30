package com.dtteam.dynamictrees.util;

import org.jetbrains.annotations.NotNull;

import java.util.Objects;
import java.util.function.Supplier;

/**
 * @author Harley O'Connor
 */
public final class MutableSuppliedLazyValue<T> extends SuppliedLazyValue<T> implements MutableLazyValue<T> {

    public MutableSuppliedLazyValue(Supplier<T> supplier) {
        super(supplier);
    }

    @Override
    public void reset(Supplier<T> supplier) {
        this.supplier = supplier;
        this.object = null;
    }

    @Override
    public void set(@NotNull T value) {
        Objects.requireNonNull(value);
        this.object = value;
    }

}
