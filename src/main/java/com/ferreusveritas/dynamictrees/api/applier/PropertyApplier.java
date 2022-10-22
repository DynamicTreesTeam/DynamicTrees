package com.ferreusveritas.dynamictrees.api.applier;

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
    protected final Applier<O, V> applier;

    public PropertyApplier(final String key, final Class<O> objectClass, final VoidApplier<O, V> propertyApplier) {
        this(key, objectClass, (Applier<O, V>) propertyApplier);
    }

    public PropertyApplier(final String key, final Class<O> objectClass, final Applier<O, V> applier) {
        this.key = key;
        this.objectClass = objectClass;
        this.applier = applier;
    }

    /**
     * Invokes {@link Applier#apply(Object, Object)} if it should be called - or in other words if the given key equaled
     * {@link #key} and the object given is an instance of the {@link #objectClass} value, and the {@link JsonElement}.
     *
     * @param key    the key for the specified {@code input}
     * @param object the object to apply to
     * @param input  the json element corresponding to the specified {@code key}
     * @return the result; otherwise {@code null} if the specified {@code key} mismatched or the specified
     * {@code object} was not of type {@link O}.
     */
    @SuppressWarnings("unchecked")
    @Nullable
    public PropertyApplierResult applyIfShould(final String key, final Object object, final I input) {
        if (!this.key.equalsIgnoreCase(key) || !this.objectClass.isInstance(object)) {
            return null;
        }

        return this.applyIfShould((O) object, input, applier);
    }

    @Nullable
    protected abstract PropertyApplierResult applyIfShould(final O object, final I input, Applier<O, V> applier);

    public Class<O> getObjectClass() {
        return objectClass;
    }

}
