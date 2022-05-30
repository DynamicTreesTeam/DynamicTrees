package com.ferreusveritas.dynamictrees.util;

import java.util.function.Supplier;

/**
 * @author Harley O'Connor
 */
public class SuppliedLazyValue<T> implements LazyValue<T> {

    protected T object;
    protected Supplier<T> supplier;

    SuppliedLazyValue(Supplier<T> supplier) {
        this.supplier = supplier;
    }

    public T get() {
        if (this.object == null) {
            this.object = supplier.get();
        }
        return this.object;
    }

}