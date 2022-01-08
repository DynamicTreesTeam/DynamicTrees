package com.ferreusveritas.dynamictrees.deserialisation;

/**
 * Thrown to indicate that a {@link Deserialiser} for a specified output type did not exist.
 *
 * @author Harley O'Connor
 */
public final class NoSuchDeserialiserException extends RuntimeException {

    /**
     * Constructs a {@code NoSuchDeserialiserException} with the specified detail {@code message}.
     *
     * @param message the detail message
     */
    public NoSuchDeserialiserException(String message) {
        super(message);
    }

}
