package com.ferreusveritas.dynamictrees.api.treepacks;

/**
 * An implementation of {@link IPropertyApplier} that assumes the application was
 * successful, calling {@link IVoidPropertyApplier#applySuccessful(Object, Object)}
 * and returning {@link PropertyApplierResult#SUCCESS}.
 *
 * @author Harley O'Connor
 */
public interface IVoidPropertyApplier<T, V> extends IPropertyApplier<T, V> {

    @Override
    default PropertyApplierResult apply(final T object, final V value) {
        this.applySuccessful(object, value);
        return PropertyApplierResult.SUCCESS;
    }

    /**
     * Applies the given property value to the given object, assuming the application was successful.
     *
     * @param object The object to apply the value to.
     * @param value The value to apply.
     */
    void applySuccessful (final T object, final V value);

}
