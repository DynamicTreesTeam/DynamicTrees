package com.ferreusveritas.dynamictrees.deserialisation.result;

import com.ferreusveritas.dynamictrees.deserialisation.DeserialisationException;
import com.google.common.collect.Lists;

import javax.annotation.Nullable;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.function.Supplier;

/**
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

    @Override
    public I getInput() {
        return this.input;
    }

    @Override
    public T get() throws NoSuchElementException {
        if (this.value == null) {
            throw new NoSuchElementException("No value present in deserialisation result.");
        }
        return this.value;
    }

    @Override
    public T orElse(T other) {
        return this.value == null ? other : this.value;
    }

    @Override
    public T orElseGet(Supplier<T> other) {
        return this.value == null ? other.get() : this.value;
    }

    @Override
    public T orElseThrow() throws DeserialisationException {
        if (this.value == null) {
            throw DeserialisationException.error(String.valueOf(this.error));
        }
        return this.value;
    }

    @Nullable
    @Override
    public String getError() {
        return this.error;
    }

    @Override
    public List<String> getWarnings() {
        return this.warnings;
    }

    @Override
    public boolean success() {
        return this.value != null;
    }

}
