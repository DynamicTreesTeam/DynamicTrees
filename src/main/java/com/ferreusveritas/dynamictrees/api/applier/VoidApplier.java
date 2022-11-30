package com.ferreusveritas.dynamictrees.api.applier;

/**
 * An implementation of {@link Applier} that assumes the application was successful, calling {@link
 * VoidApplier#applySuccessful(Object, Object)} and returning {@link PropertyApplierResult#success()}.
 *
 * @author Harley O'Connor
 */
public interface VoidApplier<O, V> extends Applier<O, V> {

    @Override
    default PropertyApplierResult apply(final O object, final V value) {
        this.applySuccessful(object, value);
        return PropertyApplierResult.success();
    }

    /**
     * Applies the given property value to the given object, assuming the application was successful.
     *
     * @param object The object to apply the value to.
     * @param value  The value to apply.
     */
    void applySuccessful(final O object, final V value);

}
