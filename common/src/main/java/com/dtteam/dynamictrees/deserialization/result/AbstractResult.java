package com.dtteam.dynamictrees.deserialization.result;

import com.dtteam.dynamictrees.deserialization.DeserializationException;
import com.google.common.collect.Lists;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * Skeletal implementation of {@link Result} minimising effort required to implement it.
 * <p>
 * This is a generic result and can be extended by a class providing a result for any input type.
 *
 * @param <T> the type of the deserialised value
 * @param <I> the type of the original input
 * @author Harley O'Connor
 */
public abstract class AbstractResult<T, I> implements Result<T, I> {

    protected final I input;
    @Nullable
    protected final T value;
    @Nullable
    protected final String error;
    protected final List<String> warnings;

    public AbstractResult(I input, @Nullable T value, @Nullable String error) {
        this(input, value, error, Lists.newLinkedList());
    }

    public AbstractResult(I input, @Nullable T value, @Nullable String error, List<String> warnings) {
        this.input = input;
        this.value = value;
        this.error = error;
        this.warnings = warnings;
    }

    /**
     * @return the original input object
     */
    public I getInput() {
        return this.input;
    }

    /**
     * @return the deserialised value
     * @throws NoSuchElementException if this is an unsuccessful result
     */
    public T get() throws NoSuchElementException {
        if (this.value == null) {
            throw new NoSuchElementException("No value present in deserialisation result.");
        }
        return this.value;
    }

    /**
     * {@inheritDoc}
     *
     * @param other the value to return if this is an unsuccessful result
     * @return the deserialised value, or {@code other} if this is an unsuccessful result
     */
    public T orElse(T other) {
        return this.value == null ? other : this.value;
    }

    /**
     * {@inheritDoc}
     *
     * @param other the value to return if this is an unsuccessful result
     * @return the deserialised value, or the result of invoking {@code other} if this is an unsuccessful result
     */
    public T orElseGet(Supplier<T> other) {
        return this.value == null ? other.get() : this.value;
    }

    /**
     * {@inheritDoc}
     *
     * @return the deserialised value
     * @throws DeserializationException if this is an unsuccessful result
     */
    public T orElseThrow() throws DeserializationException {
        if (this.value == null) {
            throw DeserializationException.error(String.valueOf(this.error));
        }
        return this.value;
    }

    /**
     * @return the error if this is an unsuccessful result; otherwise {@code null}
     */
    @Nullable
    public String getError() {
        return this.error;
    }

    /**
     * {@inheritDoc}
     *
     * @param action the action to accept for each warning
     * @return this result for chaining
     */
    public Result<T, I> forEachWarning(Consumer<String> action) {
        this.warnings.forEach(action);
        return this;
    }

    /**
     * @return a list of warnings associated with this result
     */
    public List<String> getWarnings() {
        return this.warnings;
    }

    /**
     * @return {@code true} if this result is successful; {@code false} otherwise
     */
    public boolean success() {
        return this.value != null;
    }

}
