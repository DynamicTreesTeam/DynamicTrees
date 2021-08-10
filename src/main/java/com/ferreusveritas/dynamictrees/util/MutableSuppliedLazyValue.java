package com.ferreusveritas.dynamictrees.util;

import javax.annotation.Nonnull;
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
    public void set(@Nonnull T value) {
        Objects.requireNonNull(value);
        this.object = value;
    }

}
