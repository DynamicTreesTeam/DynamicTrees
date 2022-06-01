package com.ferreusveritas.dynamictrees.api.resource.loading;

/**
 * Thrown to indicate that applying a loaded resource failed.
 *
 * @author Harley O'Connor
 */
public final class ApplicationException extends Exception {

    public ApplicationException(String message) {
        super(message);
    }

}
