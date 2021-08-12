package com.ferreusveritas.dynamictrees.deserialisation.result;

import com.ferreusveritas.dynamictrees.deserialisation.DeserialisationException;
import com.ferreusveritas.dynamictrees.util.function.ThrowableBiFunction;
import com.ferreusveritas.dynamictrees.util.function.ThrowableFunction;

import javax.annotation.Nullable;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.function.*;

/**
 * @param <T> the type of the result
 * @param <I> the type of the original input
 * @author Harley O'Connor
 */
public interface Result<T, I> {

    I getInput();

    T get() throws NoSuchElementException;

    default Result<T, I> ifSuccess(Consumer<T> consumer) {
        if (this.success()) {
            consumer.accept(this.get());
        }
        return this;
    }

    default Result<T, I> ifSuccessOrElse(Consumer<T> consumer, Consumer<String> errorConsumer,
                                         Consumer<String> warningConsumer) {
        if (this.success()) {
            consumer.accept(this.get());
        } else {
            errorConsumer.accept(this.getError());
        }
        this.getWarnings().forEach(warningConsumer);
        return this;
    }

    default Result<T, I> ifSuccessOrElseThrow(Consumer<T> consumer, Consumer<String> warningConsumer)
            throws DeserialisationException {
        if (this.success()) {
            consumer.accept(this.get());
        } else {
            throw new DeserialisationException(this.getError());
        }
        this.getWarnings().forEach(warningConsumer);
        return this;
    }

    T orElse(T other);

    default T orElseApply(Function<String, T> function, T other) {
        if (this.success()) {
            return this.get();
        } else if (this.getError() != null) {
            return function.apply(this.getError());
        }
        return other;
    }

    default T orElseApply(Function<String, T> function, BiConsumer<T, List<String>> warningAppender, T other) {
        if (this.success()) {
            warningAppender.accept(this.get(), this.getWarnings());
            return this.get();
        } else if (this.getError() != null) {
            other = function.apply(this.getError());
            warningAppender.accept(other, this.getWarnings());
        }
        return other;
    }

    default T orElse(T other, Consumer<String> errorConsumer, Consumer<String> warningConsumer) {
        if (!this.success()) {
            errorConsumer.accept(this.getError());
        }
        this.getWarnings().forEach(warningConsumer);
        return this.orElse(other);
    }

    T orElseGet(Supplier<T> other);

    T orElseThrow() throws DeserialisationException;

    @Nullable
    String getError();

    List<String> getWarnings();

    boolean success();

    Result<T, I> removeError();

    default <V> MappedResult<V, I> map(SimpleMapper<T, V> mapper) {
        return this.map(mapper.fullMapper());
    }

    <V> MappedResult<V, I> map(Mapper<T, V> mapper);

    default <V> MappedResult<V, I> map(SimpleMapper<T, V> mapper, String nullError) {
        return this.map(mapper.fullMapper(), nullError);
    }

    default <V> MappedResult<V, I> map(Mapper<T, V> mapper, String nullError) {
        return this.map(mapper, Objects::nonNull, nullError);
    }

    default <V> MappedResult<V, I> map(SimpleMapper<T, V> mapper, Predicate<V> validator, String invalidError) {
        return this.map(mapper.fullMapper(), validator, invalidError);
    }

    <V> MappedResult<V, I> map(Mapper<T, V> mapper, Predicate<V> validator, String invalidError);

    default <V> MappedResult<V, I> mapIfValid(Predicate<T> validator, final String invalidError,
                                              SimpleMapper<T, V> mapper) {
        return this.mapIfValid(validator, invalidError, mapper.fullMapper());
    }

    <V> MappedResult<V, I> mapIfValid(Predicate<T> validator, final String invalidError,
                                      Mapper<T, V> mapper);

    default <V, N> MappedResult<N, I> mapIfType(Class<V> type, SimpleMapper<V, N> mapper) {
        return this.mapIfType(type, mapper.fullMapper());
    }

    <V, N> MappedResult<N, I> mapIfType(Class<V> type, Mapper<V, N> mapper);

    <E> MappedResult<List<E>, I> mapToListOfType(Class<E> element);

    default <V, E> MappedResult<List<E>, I> mapEachIfArray(Class<V> type, Class<E> mappedType, SimpleMapper<V, E> mapper) {
        return this.mapEachIfArray(type, mappedType, mapper.fullMapper());
    }

    <V, E> MappedResult<List<E>, I> mapEachIfArray(Class<V> type, Class<E> mappedType, Mapper<V, E> mapper);

    @FunctionalInterface
    interface SimpleMapper<T, V> extends ThrowableFunction<T, V, DeserialisationException> {
        default Mapper<T, V> fullMapper() {
            return (value, warningConsumer) -> this.apply(value);
        }
    }

    @FunctionalInterface
    interface Mapper<T, V> extends ThrowableBiFunction<T, Consumer<String>, V, DeserialisationException> {
    }

}
