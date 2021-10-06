package com.ferreusveritas.dynamictrees.util;

/**
 * Thrown to indicate that the subroutine was purposefully halted (not due to any form of error or exception).
 *
 * @author Harley O'Connor
 */
public final class IgnoreThrowable extends Throwable {

    public static final IgnoreThrowable INSTANCE = new IgnoreThrowable();

    private IgnoreThrowable() {

    }

}
