package com.ferreusveritas.dynamictrees.deserialisation.result;

import com.ferreusveritas.dynamictrees.deserialisation.DeserialisationException;
import com.ferreusveritas.dynamictrees.deserialisation.Deserialiser;
import com.ferreusveritas.dynamictrees.deserialisation.NoSuchDeserialiserException;
import com.ferreusveritas.dynamictrees.util.function.ThrowableBiFunction;
import com.ferreusveritas.dynamictrees.util.function.ThrowableFunction;

import javax.annotation.Nullable;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.function.*;

/**
 * Represents the result of a deserialisation attempt.
 * <p>
 * This may represent a successful deserialisation attempt, or an unsuccessful one. This can be checked by {@link
 * #success()}. Generally a successful result is one that has a deserialised value associated, and otherwise an
 * unsuccessful result should have an error. Both types may have warnings.
 *
 * @param <T> the type of the deserialised value
 * @param <I> the type of the original input
 * @author Harley O'Connor
 * @see MappedResult
 */
public interface Result<T, I> {

    /**
     * @return the original input object
     */
    I getInput();

    /**
     * @return the deserialised value
     * @throws NoSuchElementException if this is an unsuccessful result
     */
    T get() throws NoSuchElementException;

    /**
     * Executes the specified {@code consumer} with the deserialised value if this is a successful result.
     *
     * @param consumer the deserialised value consumer. Only consumed if this is a successful result.
     * @return this result for chaining
     */
    default Result<T, I> ifSuccess(Consumer<T> consumer) {
        if (this.success()) {
            consumer.accept(this.get());
        }
        return this;
    }

    /**
     * Executes the specified {@code consumer} with the deserialised value if this is a successful result, otherwise
     * executes the specified {@code errorConsumer} with the result's error. Then executes the specified {@code
     * warningConsumer} for each warning regardless of this being a successful or unsuccessful result.
     *
     * @param consumer the deserialised value consumer. Only consumed if this is a successful result.
     * @param errorConsumer the error consumer. Only consumed if this is an unsuccessful result.
     * @param warningConsumer the warning consumer. Consumed for each warning.
     * @return this result for chaining
     */
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

    /**
     * Executes the specified {@code consumer} with the deserialised value if this is a successful result, otherwise
     * throws a {@link DeserialisationException} with the result's error as the detail message. Then executes the
     * specified {@code warningConsumer} for each warning (if successful).
     *
     * @param consumer the deserialised value consumer. Only consumed if this is a successful result.
     * @param warningConsumer the warning consumer. Consumed for each warning.
     * @return this result for chaining
     */
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

    /**
     * Returns the deserialised value if this is a successful result, or the specified {@code other} value if not.
     *
     * @param other the value to return if this is an unsuccessful result
     * @return the deserialised value, or {@code other} if this is an unsuccessful result
     */
    T orElse(T other);

    /**
     * Returns the deserialised value if this is a successful result, or the specified other value if not.
     * <p>
     * If this result is unsuccessful, the specified {@code errorConsumer} is also accepted with the result's error.
     * The specified {@code warningConsumer} is then executed for each warning, regardless of the result being
     * successful.
     *
     * @param other the value to return if this is an unsuccessful result
     * @param errorConsumer the error consumer. Only consumed if this is an unsuccessful result.
     * @param warningConsumer the warning consumer. Consumed for each warning.
     * @return the deserialised value, or {@code other} if this is an unsuccessful result
     */
    default T orElse(T other, Consumer<String> errorConsumer, Consumer<String> warningConsumer) {
        if (!this.success()) {
            errorConsumer.accept(this.getError());
        }
        this.getWarnings().forEach(warningConsumer);
        return this.orElse(other);
    }

    /**
     * Returns the deserialised value if this is a successful result, or returns the result of applying the specified
     * {@code function} with the error otherwise. If no error is set the specified {@code other} value is used.
     *
     * @param function the function to apply if this is unsuccessful
     * @param other the value to use if this is unsuccessful and an error is not set
     * @return the deserialised value, or the value from applying the specified {@code function} if this is
     * unsuccessful. Otherwise {@code other} if no error was set.
     */
    default T orElseApply(Function<String, T> function, T other) {
        if (this.success()) {
            return this.get();
        } else if (this.getError() != null) {
            return function.apply(this.getError());
        }
        return other;
    }

    /**
     * Returns the deserialised value if this is a successful result, or returns the result of applying the specified
     * {@code function} with the error otherwise. If no error is set the specified {@code other} value is used.
     *
     * @param function the function to apply if this is unsuccessful
     * @param warningAppender a function to apply the returned value and warnings to
     * @param other the value to use if this is unsuccessful and an error is not set
     * @return the deserialised value, or the value from applying the specified {@code function} if this is
     * unsuccessful. Otherwise {@code other} if no error was set.
     */
    default T orElseApply(Function<String, T> function, BiConsumer<T, List<String>> warningAppender, T other) {
        if (this.success()) {
            warningAppender.accept(this.get(), this.getWarnings());
            return this.get();
        } else if (this.getError() != null) {
            other = function.apply(this.getError());
        }
        warningAppender.accept(other, this.getWarnings());
        return other;
    }

    /**
     * Returns the deserialised value if this is a successful result, or gets the value from the specified {@code other}
     * supplier if not.
     *
     * @param other the value to return if this is an unsuccessful result
     * @return the deserialised value, or the result of invoking {@code other} if this is an unsuccessful result
     */
    T orElseGet(Supplier<T> other);

    /**
     * Returns the deserialised value if this is a successful result, or throws a {@link DeserialisationException} if
     * not.
     *
     * @return the deserialised value
     * @throws DeserialisationException if this is an unsuccessful result
     */
    T orElseThrow() throws DeserialisationException;

    /**
     * @return the error if this is an unsuccessful result; otherwise {@code null}
     */
    @Nullable
    String getError();

    /**
     * @return a list of warnings associated with this result
     */
    List<String> getWarnings();

    /**
     * @return {@code true} if this result is successful; {@code false} otherwise
     */
    boolean success();

    /**
     * Removes the error from this result. This is a very niche method usually used for customising the error message
     * in {@link #orElseApply(Function, Object)} when mapping the deserialised value to its own result.
     *
     * @return this result for chaining
     */
    Result<T, I> removeError();

    /**
     * Applies the specified {@code mapper} to the deserialised value, if this is a successful result, returning
     * the resulting {@link MappedResult}.
     * <p>
     * Otherwise, if this result was not successful in the first place, the error and warnings are carried to a new
     * mapped result.
     *
     * @param mapper a simple mapper that maps the deserialised value to a new value
     * @param <V> the type to map to
     * @return the mapped result
     */
    default <V> MappedResult<V, I> map(SimpleMapper<T, V> mapper) {
        return this.map(mapper.fullMapper());
    }

    /**
     * Applies the specified {@code mapper} to the deserialised value, if this is a successful result, returning
     * the resulting {@link MappedResult}.
     * <p>
     * Otherwise, if this result was not successful in the first place, the error and warnings are carried to a new
     * mapped result.
     *
     * @param mapper a mapper that maps the deserialised value to a new value
     * @param <V> the type to map to
     * @return the mapped result
     */
    <V> MappedResult<V, I> map(Mapper<T, V> mapper);

    /**
     * Applies the specified {@code mapper} to the deserialised value, if this is a successful result, returning the
     * resulting {@link MappedResult}. If the result of the application is {@code null}, the specified {@code nullError}
     * is taken as the error for the result.
     * <p>
     * Otherwise, if this result was not successful in the first place, the error and warnings are carried to a new
     * mapped result.
     *
     * @param mapper a mapper that maps the deserialised value to a new value
     * @param nullError the error message to use if the mapped deserialised value is {@code null}
     * @param <V> the type to map to
     * @return the mapped result
     */
    default <V> MappedResult<V, I> map(SimpleMapper<T, V> mapper, String nullError) {
        return this.map(mapper.fullMapper(), nullError);
    }

    /**
     * Applies the specified {@code mapper} to the deserialised value, if this is a successful result, returning the
     * resulting {@link MappedResult}. If the result of the application is {@code null}, the specified {@code nullError}
     * is taken as the error for the result.
     * <p>
     * Otherwise, if this result was not successful in the first place, the error and warnings are carried to a new
     * mapped result.
     *
     * @param mapper a mapper that maps the deserialised value to a new value
     * @param nullError the error message to use if the mapped deserialised value is {@code null}
     * @param <V> the type to map to
     * @return the mapped result
     */
    default <V> MappedResult<V, I> map(Mapper<T, V> mapper, String nullError) {
        return this.map(mapper, Objects::nonNull, nullError);
    }

    /**
     * Applies the specified {@code mapper} to the deserialised value, if this is a successful result, returning the
     * resulting {@link MappedResult}. If the result of testing the mapped value to the specified {@code validator} is
     * {@code false}, the specified {@code invalidError} is taken as the error for the result and no deserialised value
     * assigned to the mapped result.
     * <p>
     * Otherwise, if this result was not successful in the first place, the error and warnings are carried to a new
     * mapped result.
     *
     * @param mapper a mapper that maps the deserialised value to a new value
     * @param validator the predicate by which to test the mapped value
     * @param invalidError the error message to use if the mapped deserialised value does not pass the {@code validator}
     * @param <V> the type to map to
     * @return the mapped result
     */
    default <V> MappedResult<V, I> map(SimpleMapper<T, V> mapper, Predicate<V> validator, String invalidError) {
        return this.map(mapper.fullMapper(), validator, invalidError);
    }

    /**
     * Applies the specified {@code mapper} to the deserialised value, if this is a successful result, returning the
     * resulting {@link MappedResult}. If the result of testing the mapped value to the specified {@code validator} is
     * {@code false}, the specified {@code invalidError} is taken as the error for the result and no deserialised value
     * assigned to the mapped result.
     * <p>
     * Otherwise, if this result was not successful in the first place, the error and warnings are carried to a new
     * mapped result.
     *
     * @param mapper a mapper that maps the deserialised value to a new value
     * @param validator the predicate by which to test the mapped value
     * @param invalidError the error message to use if the mapped deserialised value does not pass the {@code validator}
     * @param <V> the type to map to
     * @return the mapped result
     */
    <V> MappedResult<V, I> map(Mapper<T, V> mapper, Predicate<V> validator, String invalidError);

    /**
     * Applies the specified {@code mapper} to the deserialised value, if this is a successful result and the specified
     * {@code validator} returns {@code true}, returning the resulting {@link MappedResult}. If the {@code validator}
     * was not passed, the specified {@code invalidError} will be taken as the error of the mapped result and no value
     * assigned.
     * <p>
     * Otherwise, if this result was not successful in the first place, the error and warnings are carried to a new
     * mapped result.
     *
     * @param validator the predicate by which to test the deserialised value
     * @param invalidError the error message to use if this value does not pass the {@code validator}
     * @param mapper a mapper that maps the deserialised value to a new value
     * @param <V> the type to map to
     * @return the mapped result
     */
    default <V> MappedResult<V, I> mapIfValid(Predicate<T> validator, final String invalidError,
                                              SimpleMapper<T, V> mapper) {
        return this.mapIfValid(validator, invalidError, mapper.fullMapper());
    }

    /**
     * Applies the specified {@code mapper} to the deserialised value, if this is a successful result and the specified
     * {@code validator} returns {@code true}, returning the resulting {@link MappedResult}. If the {@code validator}
     * was not passed, the specified {@code invalidError} will be taken as the error of the mapped result and no value
     * assigned.
     * <p>
     * Otherwise, if this result was not successful in the first place, the error and warnings are carried to a new
     * mapped result.
     *
     * @param validator the predicate by which to test the deserialised value
     * @param invalidError the error message to use if this value does not pass the {@code validator}
     * @param mapper a mapper that maps the deserialised value to a new value
     * @param <V> the type to map to
     * @return the mapped result
     */
    <V> MappedResult<V, I> mapIfValid(Predicate<T> validator, final String invalidError, Mapper<T, V> mapper);

    /**
     * Attempts to deserialise the original input value as the specified {@code type}, mapping it using the specified
     * {@code mapper} if successful and returning the resulting {@link MappedResult}.
     *
     * @param type the type to attempt to deserialise
     * @param mapper a mapper that maps the deserialised value to a new value
     * @param <V> the type to attempt to deserialise
     * @param <N> the type to map to
     * @return the mapped result
     * @throws NoSuchDeserialiserException if {@code type} did not have a registered deserialiser
     */
    default <V, N> MappedResult<N, I> mapIfType(Class<V> type, SimpleMapper<V, N> mapper) {
        return this.mapIfType(type, mapper.fullMapper());
    }

    /**
     * Attempts to deserialise the original input value as the specified {@code type}, mapping it using the specified
     * {@code mapper} if successful and returning the resulting {@link MappedResult}.
     *
     * @param type the type to attempt to deserialise
     * @param mapper a mapper that maps the deserialised value to a new value
     * @param <V> the type to attempt to deserialise
     * @param <N> the type to map to
     * @return the mapped result
     * @throws NoSuchDeserialiserException if the specified {@code type} did not have a registered deserialiser
     */
    <V, N> MappedResult<N, I> mapIfType(Class<V> type, Mapper<V, N> mapper);

    /**
     * Attempts to map the original input value to a list of the specified {@code elementType}.
     *
     * @param elementType the type of element to map to a list of
     * @param <E> the type of the element of the list to map to
     * @return the mapped result
     * @throws NoSuchDeserialiserException if the specified {@code elementType} did not have a registered deserialiser
     */
    <E> MappedResult<List<E>, I> mapToListOfType(Class<E> elementType);

    /**
     * Attempts to map the original input value to a list of the specified {@code elementType}, then mappings each
     * entry to the specified {@code mappedType} using the specified {@code mapper}.
     *
     * @param elementType the initial type of element to map to a list of
     * @param mappedType the type to map each element to
     * @param mapper a mapper that maps each deserialised value to the {@code mappedType}
     * @param <V> the initial type of element to map to a list of
     * @param <E> the type to map each element to
     * @return the mapper result
     * @throws NoSuchDeserialiserException if the specified {@code elementType} did not have a registered deserialiser
     */
    default <V, E> MappedResult<List<E>, I> mapEachIfArray(Class<V> elementType, Class<E> mappedType,
                                                           SimpleMapper<V, E> mapper) {
        return this.mapEachIfArray(elementType, mappedType, mapper.fullMapper());
    }

    /**
     * Attempts to map the original input value to a list of the specified {@code elementType}, then mappings each
     * entry to the specified {@code mappedType} using the specified {@code mapper}.
     *
     * @param elementType the initial type of element to map to a list of
     * @param mappedType the type to map each element to
     * @param mapper a mapper that maps each deserialised value to the {@code mappedType}
     * @param <V> the initial type of element to map to a list of
     * @param <E> the type to map each element to
     * @return the mapper result
     * @throws NoSuchDeserialiserException if the specified {@code elementType} did not have a registered deserialiser
     */
    <V, E> MappedResult<List<E>, I> mapEachIfArray(Class<V> elementType, Class<E> mappedType, Mapper<V, E> mapper);

    /**
     * A {@link ThrowableBiFunction} that handles mapping a deserialised value to another value. The input is the
     * deserialised value being mapped, with the return being the mapped value. The application can also throw a {@link
     * DeserialisationException}, whose message should be used as the mapped result's error if thrown.
     * <p>
     * This is a convenience mapper for invokers who needn't access a warning consumer. It can be easily converted to a
     * "full" {@link Mapper} by {@link #fullMapper()}.
     *
     * @param <T> the type of the original deserialised value
     * @param <V> the type this mapper maps to
     */
    @FunctionalInterface
    interface SimpleMapper<T, V> extends ThrowableFunction<T, V, DeserialisationException> {
        /**
         * @return this wrapper in a {@link Mapper}
         */
        default Mapper<T, V> fullMapper() {
            return (value, warningConsumer) -> this.apply(value);
        }
    }

    /**
     * A {@link ThrowableBiFunction} that handles mapping a deserialised value to another value. The first input is
     * the deserialised value being mapped, the second a consumer for warnings, with the return being the mapped value.
     * The application can also throw a {@link DeserialisationException}, whose message should be used as the mapped
     * result's error if thrown.
     *
     * @param <T> the type of the original deserialised value
     * @param <V> the type this mapper maps to
     */
    @FunctionalInterface
    interface Mapper<T, V> extends ThrowableBiFunction<T, Consumer<String>, V, DeserialisationException> {
    }

}
