package com.ferreusveritas.dynamictrees.api.treepacks;

import java.util.List;

/**
 * @author Harley O'Connor
 */
public interface ArrayApplier<O, V> {

    /**
     * Applies the specified {@code values} to the given object.
     *
     * @param object the object to apply to
     * @param values the values to apply
     * @return the result of the application
     */
    PropertyApplierResult apply(O object, V values);

}
