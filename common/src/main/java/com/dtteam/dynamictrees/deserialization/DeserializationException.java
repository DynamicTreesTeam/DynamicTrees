package com.dtteam.dynamictrees.deserialization;

/**
 * Thrown to indicate that there was an error whilst deserialising a type.
 *
 * <p>This is a checked exception that should be caught and logged with the value returned by {@link #getMessage()}.
 * </p>
 *
 * @author Harley O'Connor
 */
public class DeserializationException extends Exception {

    public DeserializationException(String message) {
        super(message);
    }

    public static DeserializationException error(String message) {
        return new DeserializationException(message);
    }

}
