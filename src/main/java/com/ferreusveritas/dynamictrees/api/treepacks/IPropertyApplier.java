package com.ferreusveritas.dynamictrees.api.treepacks;

/**
 * @author Harley O'Connor
 */
@FunctionalInterface
public interface IPropertyApplier<T, V> {

    /**
     * Applies the given property value to the given object.
     *
     * @param object The object to apply the value to.
     * @param value The value to apply.
     * @return A {@link PropertyApplierResult} object.
     */
    PropertyApplierResult apply (final T object, final V value);

}
