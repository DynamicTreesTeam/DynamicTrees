package com.ferreusveritas.dynamictrees.api.treepacks;

/**
 * An {@link Applier} that is applied if the corresponding value is {@code true}.
 *
 * @param <O> the type of the object to apply to
 * @author Harley O'Connor
 */
public interface IfTrueApplier<O> {

    void apply(final O object);

}
