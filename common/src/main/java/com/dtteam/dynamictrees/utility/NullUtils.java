package com.dtteam.dynamictrees.utility;

import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;
import java.util.function.Function;

/**
 * Contains utility classes for operations relating to {@code null}.
 *
 * @author Harley O'Connor
 */
public final class NullUtils {

    /**
     * Checks if the given {@link Object} is {@code null}.
     *
     * @param obj The {@link Object} to check.
     * @return {@code true} if the given {@link Object} is {@code null}; {@code false} otherwise.
     */
    public static boolean isNotNull(@Nullable final Object obj) {
        return obj != null;
    }

    /**
     * Calls {@link Consumer#accept(Object)} if the given {@link Object} is not {@code null}, returning whether or not
     * it was.
     *
     * @param obj             The {@link Object} to check.
     * @param nonnullConsumer The {@link Consumer} to accept if {@code obj} is not {@code null}.
     * @param <T>             The type of the given {@link Object}.
     * @return {@code true} if the given {@link Object} is {@code null}; {@code false} otherwise.
     */
    public static <T> boolean consumeIfNonnull(@Nullable final T obj, final Consumer<T> nonnullConsumer) {
        if (isNotNull(obj)) {
            nonnullConsumer.accept(obj);
            return true;
        }
        return false;
    }

    @Nullable
    public static <T, R> R applyIfNonnull(@Nullable T obj, final Function<T, R> function) {
        if (isNotNull(obj)) {
            return function.apply(obj);
        }
        return null;
    }

    public static <T, R> R applyIfNonnull(@Nullable T obj, final Function<T, R> function, final R defaultReturn) {
        if (isNotNull(obj)) {
            return function.apply(obj);
        }
        return defaultReturn;
    }

}
