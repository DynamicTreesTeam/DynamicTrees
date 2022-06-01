package com.ferreusveritas.dynamictrees.deserialisation;

/**
 * Thrown to indicate that there was an error whilst deserialising a type.
 *
 * <p>This is a checked exception that should be caught and logged with the value returned by {@link #getMessage()}.
 * </p>
 *
 * @author Harley O'Connor
 */
public class DeserialisationException extends Exception {

    public DeserialisationException(String message) {
        super(message);
    }

    public static DeserialisationException error(String message) {
        return new DeserialisationException(message);
    }

}
