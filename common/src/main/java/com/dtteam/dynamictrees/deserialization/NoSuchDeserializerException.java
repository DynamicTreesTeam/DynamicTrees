package com.dtteam.dynamictrees.deserialization;

import com.dtteam.dynamictrees.deserialization.deserializer.Deserializer;

/**
 * Thrown to indicate that a {@link Deserializer} for a specified output type did not exist.
 *
 * @author Harley O'Connor
 */
public final class NoSuchDeserializerException extends RuntimeException {

    /**
     * Constructs a {@code NoSuchDeserializerException} with the specified detail {@code message}.
     *
     * @param message the detail message
     */
    public NoSuchDeserializerException(String message) {
        super(message);
    }

}
