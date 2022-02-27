package com.ferreusveritas.dynamictrees.util.function;

import java.util.function.BiFunction;

/**
 * Copy of {@link BiFunction}, except {@link #apply(Object, Object)} throws {@link T}.
 *
 * @author Harley O'Connor
 */
@FunctionalInterface
public interface ThrowableBiFunction<A, B, R, T extends Throwable> {

    R apply(A a, B b) throws T;

}
