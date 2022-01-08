package com.ferreusveritas.dynamictrees.util.function;

import java.util.function.Function;

/**
 * Copy of {@link Function}, except {@link #apply(Object)} throws {@link T}.
 *
 * @author Harley O'Connor
 */
@FunctionalInterface
public interface ThrowableFunction<I, R, T extends Throwable> {

    R apply(I i) throws T;

}
