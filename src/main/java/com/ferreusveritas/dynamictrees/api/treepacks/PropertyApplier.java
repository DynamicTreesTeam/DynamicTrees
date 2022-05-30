package com.ferreusveritas.dynamictrees.api.treepacks;

import com.google.gson.JsonElement;

import javax.annotation.Nullable;

/**
 * Manages applying a property (of type <tt>V</tt>) to an object (of type <tt>T</tt>).
 *
 * @param <O> the object the property should be applied to
 * @param <V> the type of the value of the property to apply
 * @author Harley O'Connor
 */
public abstract class PropertyApplier<O, V, I> {

    protected final String key;
    protected final Class<O> objectClass;
    protected final Class<V> valueClass;
    protected final Applier<O, V> applier;

    public PropertyApplier(final String key, final Class<O> objectClass, final Class<V> valueClass, final VoidApplier<O, V> propertyApplier) {
        this(key, objectClass, valueClass, (Applier<O, V>) propertyApplier);
    }

    public PropertyApplier(final String key, final Class<O> objectClass, final Class<V> valueClass, final Applier<O, V> applier) {
        this.key = key;
        this.objectClass = objectClass;
        this.valueClass = valueClass;
        this.applier = applier;
    }

    /**
     * Invokes {@link Applier#apply(Object, Object)} if it should be called - or in other words if the given key equaled
     * {@link #key} and the object given is an instance of the {@link #objectClass} value, and the {@link JsonElement}
     * given contained a value that can be converted to the {@link #valueClass}.
     *
     * @param key    the key for the specified {@code input}
     * @param object the object to apply to
     * @param input  the json element corresponding to the specified {@code key}
     * @return the result; otherwise {@code null} if the specified {@code key} mismatched or the specified
     * {@code object} was not of type {@link O}.
     */
    @Nullable
    public PropertyApplierResult applyIfShould(final String key, final Object object, final I input) {
        if (!this.key.equalsIgnoreCase(key) || !this.objectClass.isInstance(object)) {
            return null;
        }

        return this.applyIfShould(object, input, this.valueClass, this.applier);
    }

    @Nullable
    protected abstract <S, R> PropertyApplierResult applyIfShould(final Object object, final I input, final Class<R> valueClass, final Applier<S, R> applier);

    public Class<O> getObjectClass() {
        return objectClass;
    }

    public Class<V> getValueClass() {
        return valueClass;
    }

}
