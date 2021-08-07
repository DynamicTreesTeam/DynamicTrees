package com.ferreusveritas.dynamictrees.api.treepacks;

/**
 * @param <O> the type of the object to apply to
 * @param <V> the type of the value of the property
 * @author Harley O'Connor
 */
@FunctionalInterface
public interface Applier<O, V> {

    /**
     * Applies the specified {@code value} to the given object.
     *
     * @param object the object to apply to
     * @param value  the value to apply
     * @return the result of the application
     */
    PropertyApplierResult apply(final O object, final V value);

}
