package com.ferreusveritas.dynamictrees.util.json;

import javax.annotation.Nullable;

/**
 * @author Harley O'Connor
 */
public final class ObjectFetchResult<T> {

    private final T value;
    private final String errorMessage;

    public ObjectFetchResult(T value, String errorMessage) {
        this.value = value;
        this.errorMessage = errorMessage;
    }

    public boolean wasSuccessful () {
        return this.value != null;
    }

    /**
     * Gets the value. {@link #wasSuccessful()} should be checked first, as this will be null
     * if the fetch was not successful.
     *
     * @return The value, or null if the fetch was unsuccessful.
     */
    public T getValue() {
        return value;
    }

    /**
     * Gets the error message. {@link #wasSuccessful()} should be checked first, as if the fetch
     * succeeded this will be null.
     *
     * @return The error message, or null if the fetch was successful.
     */
    public String getErrorMessage() {
        return errorMessage;
    }

    public static <T> ObjectFetchResult<T> success (final T value) {
        return new ObjectFetchResult<>(value, null);
    }

    public static <T> ObjectFetchResult<T> failure (final String errorMessage) {
        return new ObjectFetchResult<>(null, errorMessage);
    }

    public static <T> ObjectFetchResult<T> successOrFailure(@Nullable final T value, final String errorMessage) {
        return value == null ? ObjectFetchResult.failure(errorMessage) : ObjectFetchResult.success(value);
    }

    public static <T> ObjectFetchResult<T> failureFromOther (final ObjectFetchResult<?> otherFetchResult) {
        return ObjectFetchResult.failure(otherFetchResult.getErrorMessage());
    }

}
