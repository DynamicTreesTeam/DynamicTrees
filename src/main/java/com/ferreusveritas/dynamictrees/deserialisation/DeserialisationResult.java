package com.ferreusveritas.dynamictrees.deserialisation;

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
 * Stores the value or the error that came from trying to fetch an object from a {@link com.google.gson.JsonElement},
 * mainly used by {@link JsonDeserialiser}.
 *
 * @author Harley O'Connor
 */
public final class DeserialisationResult<T> {

    private T value;
    private String errorMessage;

    private final List<String> warnings = new ArrayList<>();

    public DeserialisationResult() {
    }

    public DeserialisationResult(final T value) {
        this.value = value;
    }

    public DeserialisationResult(final String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public boolean wasSuccessful() {
        return this.value != null;
    }

    public <V> DeserialisationResult<V> map(final Function<T, V> conversionFunction) {
        final DeserialisationResult<V> mappedResult = new DeserialisationResult<V>().copyErrorsFrom(this);
        if (this.value != null) {
            mappedResult.value = conversionFunction.apply(this.value);
        }
        return mappedResult;
    }

    public <V> DeserialisationResult<V> map(final Function<T, V> conversionFunction, final String nullError) {
        // The validator returns true as there is already a null check.
        return this.map(conversionFunction, value -> true, nullError);
    }

    public <V> DeserialisationResult<V> map(final Function<T, V> conversionFunction, final Predicate<V> validator, final String invalidError) {
        final DeserialisationResult<V> mappedResult = new DeserialisationResult<V>().copyErrorsFrom(this);
        if (this.value != null) {
            final String previousValue = this.value.toString();
            final V value = conversionFunction.apply(this.value);

            if (value != null && validator.test(value)) {
                mappedResult.setValue(value);
            } else {
                mappedResult.setErrorMessage(invalidError.replace("{previous_value}", previousValue));
            }
        }
        return mappedResult;
    }

    public <V> DeserialisationResult<V> mapIfValid(final Predicate<T> validator, final String invalidError, final Function<T, V> conversionFunction) {
        DeserialisationResult<V> mappedResult = new DeserialisationResult<V>().copyErrorsFrom(this);
        if (this.value != null) {
            if (validator.test(this.value)) {
                mappedResult = this.map(conversionFunction, "Internal error.");
            } else {
                mappedResult.setErrorMessage(invalidError.replace("{value}", this.value.toString()));
            }
        }
        return mappedResult;
    }

    public DeserialisationResult<T> addWarning(final String warning) {
        this.warnings.add(warning);
        return this;
    }

    public DeserialisationResult<T> ifSuccessful(final Consumer<T> valueConsumer) {
        if (this.wasSuccessful()) {
            valueConsumer.accept(this.value);
        }
        return this;
    }

    public DeserialisationResult<T> elseIfError(final Consumer<String> errorConsumer) {
        if (!this.wasSuccessful()) {
            errorConsumer.accept(this.errorMessage);
        }
        return this;
    }

    public DeserialisationResult<T> elseIfError(final Runnable runnable) {
        if (!this.wasSuccessful()) {
            runnable.run();
        }
        return this;
    }

    public DeserialisationResult<T> forEachWarning(final Consumer<String> warningConsumer) {
        if (!this.wasSuccessful()) {
            for (final String warning : this.warnings) {
                warningConsumer.accept(warning);
            }
        }
        return this;
    }

    public DeserialisationResult<T> otherwiseWarn(final String warnPrefix) {
        if (!this.wasSuccessful()) {
            LogManager.getLogger(StackLocatorUtil.getCallerClass(1)).warn(warnPrefix + this.getErrorMessage());
        }
        return this;
    }

    public T orDefault(final T defaultValue) {
        return this.wasSuccessful() ? this.getValue() : defaultValue;
    }

    /**
     * Gets the value. {@link #wasSuccessful()} should be checked first, as this will be null if the fetch was not
     * successful.
     *
     * @return The value, or null if the fetch was unsuccessful.
     */
    public T getValue() {
        return value;
    }

    public DeserialisationResult<T> setValue(final T value) {
        this.value = value;
        return this;
    }

    public DeserialisationResult<T> setValueOrFailure(@Nullable final T value, final String errorMessage) {
        if (value == null) {
            this.errorMessage = errorMessage;
        } else {
            this.value = value;
        }
        return this;
    }

    /**
     * Gets the error message. {@link #wasSuccessful()} should be checked first, as if the fetch succeeded this will be
     * null.
     *
     * @return The error message, or null if the fetch was successful.
     */
    public String getErrorMessage() {
        return errorMessage;
    }

    public DeserialisationResult<T> setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
        return this;
    }

    public DeserialisationResult<T> setErrorMessageIfUnsetAndNull(String errorMessage) {
        if (this.value == null && this.errorMessage == null) {
            this.setErrorMessage(errorMessage);
        }
        return this;
    }

    public List<String> getWarnings() {
        return warnings;
    }

    public DeserialisationResult<T> copyFrom(final DeserialisationResult<T> otherResult) {
        this.value = otherResult.value;
        this.copyErrorsFrom(otherResult);
        return this;
    }

    public DeserialisationResult<T> copyErrorsFrom(final DeserialisationResult<?> otherResult) {
        this.errorMessage = otherResult.errorMessage;
        this.warnings.addAll(otherResult.warnings);
        return this;
    }

    public static <T> DeserialisationResult<T> from(final DataResult<Pair<T, JsonElement>> dataResult) {
        final DeserialisationResult<T> result = new DeserialisationResult<>();
        dataResult.get()
                .ifLeft(pair -> result.value = pair.getFirst())
                .ifRight(partialResult -> result.setErrorMessage(partialResult.message()));
        return result;
    }

    public static <T> DeserialisationResult<T> success(final T value) {
        return new DeserialisationResult<>(value);
    }

    public static <T> DeserialisationResult<T> failure(final String errorMessage) {
        return new DeserialisationResult<>(errorMessage);
    }

    public static <T> DeserialisationResult<T> successOrFailure(@Nullable final T value, final String errorMessage) {
        return value == null ? DeserialisationResult.failure(errorMessage) : DeserialisationResult.success(value);
    }

    public static <T> DeserialisationResult<T> failureFromOther(final DeserialisationResult<?> otherResult) {
        return DeserialisationResult.failure(otherResult.getErrorMessage());
    }

}
