package com.ferreusveritas.dynamictrees.api.treepacks;

/**
 * An {@link Applier} that is applied if a corresponding condition is met. This condition is defined separately by the
 * invoker of {@link #apply(Object)}.
 * <p>
 * This is a {@link FunctionalInterface} whose functional method is {@link #apply(Object)}.
 *
 * @param <O> the type of the object to apply to
 * @author Harley O'Connor
 */
@FunctionalInterface
public interface IfTrueApplier<O> {

    /**
     * Applies a value to the specified {@code object}.
     * <p>
     * Should only be invoked when the corresponding condition is met.
     *
     * @param object the object to apply to
     */
    void apply(final O object);

}
