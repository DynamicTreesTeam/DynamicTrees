package com.ferreusveritas.dynamictrees.util;

import java.lang.reflect.Field;

/**
 * @author Harley O'Connor
 */
public final class ReflectionHelper {

    public static <T> T getPrivateFieldUnchecked(final Class<?> clazz, final String name) {
        try {
            return getPrivateField(clazz, name);
        } catch (final NoSuchFieldException | IllegalAccessException | ClassCastException e) {
            throw new RuntimeException(e);
        }
    }

    @SuppressWarnings("unchecked")
    private static <T> T getPrivateField(final Class<?> clazz, final String name) throws NoSuchFieldException, IllegalAccessException, ClassCastException {
        final Field field = clazz.getDeclaredField(name);
        field.setAccessible(true);
        return (T) field.get(null);
    }

}
