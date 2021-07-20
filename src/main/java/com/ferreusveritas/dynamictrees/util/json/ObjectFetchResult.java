package com.ferreusveritas.dynamictrees.util.json;

import com.google.gson.JsonElement;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.DataResult;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.util.StackLocatorUtil;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

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

    public <V> ObjectFetchResult<V> map (final Function<T, V> conversionFunction) {
        final ObjectFetchResult<V> mappedFetchResult = new ObjectFetchResult<V>().copyErrorsFrom(this);
        if (this.value != null)
            mappedFetchResult.value = conversionFunction.apply(this.value);
        return mappedFetchResult;
    }

    public <V> ObjectFetchResult<V> map (final Function<T, V> conversionFunction, final String nullError) {
        // The validator returns true as there is already a null check.
        return this.map(conversionFunction, value -> true, nullError);
    }

    public <V> ObjectFetchResult<V> map (final Function<T, V> conversionFunction, final Predicate<V> validator, final String invalidError) {
        final ObjectFetchResult<V> mappedFetchResult = new ObjectFetchResult<V>().copyErrorsFrom(this);
        if (this.value != null) {
            final String previousValue = this.value.toString();
            final V value = conversionFunction.apply(this.value);

            if (value != null && validator.test(value))
                mappedFetchResult.setValue(value);
            else mappedFetchResult.setErrorMessage(invalidError.replace("{previous_value}", previousValue));
        }
        return mappedFetchResult;
    }

    public <V> ObjectFetchResult<V> mapIfValid(final Predicate<T> validator, final String invalidError, final Function<T, V> conversionFunction) {
        ObjectFetchResult<V> mappedFetchResult = new ObjectFetchResult<V>().copyErrorsFrom(this);
        if (this.value != null) {
            if (validator.test(this.value)) {
                mappedFetchResult = this.map(conversionFunction, "Internal error.");
            } else mappedFetchResult.setErrorMessage(invalidError.replace("{value}", this.value.toString()));
        }
        return mappedFetchResult;
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
            LogManager.getLogger(StackLocatorUtil.getCallerClass(1)).warn(warnPrefix + this.getErrorMessage());
        return this;
    }

    public T orDefault(final T defaultValue) {
        return this.wasSuccessful() ? this.getValue() : defaultValue;
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

    public ObjectFetchResult<T> setValueOrFailure (@Nullable final T value, final String errorMessage) {
        if (value == null)
            this.errorMessage = errorMessage;
        else this.value = value;
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

    public ObjectFetchResult<T> setErrorMessageIfUnsetAndNull(String errorMessage) {
        if (this.value == null && this.errorMessage == null)
            this.setErrorMessage(errorMessage);
        return this;
    }

    public List<String> getWarnings() {
        return warnings;
    }

    public ObjectFetchResult<T> copyFrom(final ObjectFetchResult<T> otherFetchResult) {
        this.value = otherFetchResult.value;
        this.copyErrorsFrom(otherFetchResult);
        return this;
    }

    public ObjectFetchResult<T> copyErrorsFrom(final ObjectFetchResult<?> otherFetchResult) {
        this.errorMessage = otherFetchResult.errorMessage;
        this.warnings.addAll(otherFetchResult.warnings);
        return this;
    }

    public static <T> ObjectFetchResult<T> from(final DataResult<Pair<T, JsonElement>> dataResult) {
        final ObjectFetchResult<T> fetchResult = new ObjectFetchResult<>();
        dataResult.get()
                .ifLeft(pair -> fetchResult.value = pair.getFirst())
                .ifRight(partialResult -> fetchResult.setErrorMessage(partialResult.message()));
        return fetchResult;
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
