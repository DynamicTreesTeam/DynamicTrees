package com.ferreusveritas.dynamictrees.api.datapacks;

/**
 * @author Harley O'Connor
 */
public interface IPropertyApplier<T, V> {

    void apply (final T object, final V value);

}
