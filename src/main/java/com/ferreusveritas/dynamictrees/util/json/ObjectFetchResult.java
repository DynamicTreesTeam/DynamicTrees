package com.ferreusveritas.dynamictrees.util.json;

import org.apache.logging.log4j.LogManager;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * Stores the value or the error that came from trying to fetch an object from a
 * {@link com.google.gson.JsonElement}, mainly used by {@link IJsonObjectGetter}.
 *
 * @author Harley O'Connor
 */
public final class ObjectFetchResult<T> {

    private T value;
    private String errorMessage;

    private final List<String> warnings = new ArrayList<>();

    public ObjectFetchResult() {}

    public ObjectFetchResult(final T value) {
        this.value = value;
    }

    public ObjectFetchResult(final String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public boolean wasSuccessful () {
        return this.value != null;
    }

    public ObjectFetchResult<T> addWarning(final String warning) {
        this.warnings.add(warning);
        return this;
    }

    public ObjectFetchResult<T> ifSuccessful (final Consumer<T> valueConsumer) {
        if (this.wasSuccessful())
            valueConsumer.accept(this.value);
        return this;
    }

    public ObjectFetchResult<T> otherwise (final Consumer<String> errorConsumer) {
        if (!this.wasSuccessful())
            errorConsumer.accept(this.errorMessage);
        return this;
    }

    public ObjectFetchResult<T> otherwise (final Runnable runnable) {
        if (!this.wasSuccessful())
            runnable.run();
        return this;
    }

    public ObjectFetchResult<T> otherwiseWarn(final String warnPrefix) {
        if (!this.wasSuccessful())
            LogManager.getLogger().warn(warnPrefix + this.getErrorMessage());
        return this;
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

    public ObjectFetchResult<T> setValue (final T value) {
        this.value = value;
        return this;
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

    public ObjectFetchResult<T> setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
        return this;
    }

    public List<String> getWarnings() {
        return warnings;
    }

    public void copyFrom(final ObjectFetchResult<T> otherFetchResult) {
        this.value = otherFetchResult.value;
        this.errorMessage = otherFetchResult.getErrorMessage();
        this.warnings.addAll(otherFetchResult.warnings);
    }

    public static <T> ObjectFetchResult<T> success (final T value) {
        return new ObjectFetchResult<>(value);
    }

    public static <T> ObjectFetchResult<T> failure (final String errorMessage) {
        return new ObjectFetchResult<>(errorMessage);
    }

    public static <T> ObjectFetchResult<T> successOrFailure(@Nullable final T value, final String errorMessage) {
        return value == null ? ObjectFetchResult.failure(errorMessage) : ObjectFetchResult.success(value);
    }

    public static <T> ObjectFetchResult<T> failureFromOther (final ObjectFetchResult<?> otherFetchResult) {
        return ObjectFetchResult.failure(otherFetchResult.getErrorMessage());
    }

}
