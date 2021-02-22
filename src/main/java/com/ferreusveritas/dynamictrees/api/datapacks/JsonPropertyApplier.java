package com.ferreusveritas.dynamictrees.api.datapacks;

import com.ferreusveritas.dynamictrees.util.json.IJsonObjectGetter;
import com.ferreusveritas.dynamictrees.util.json.JsonObjectGetters;
import com.ferreusveritas.dynamictrees.util.json.ObjectFetchResult;
import com.google.gson.JsonElement;

import javax.annotation.Nullable;

/**
 * Manages applying a property (of type <tt>V</tt>) to an object (of type <tt>T</tt>).
 *
 * @author Harley O'Connor
 */
public class JsonPropertyApplier<T, V> {

    private final String key;
    private final Class<T> objectClass;
    private final Class<V> valueClass;
    private final IPropertyApplier<T, V> propertyApplier;

    public JsonPropertyApplier (final String key, final Class<T> objectClass, final Class<V> valueClass, final IVoidPropertyApplier<T, V> propertyApplier) {
        this(key, objectClass, valueClass, (IPropertyApplier<T, V>) propertyApplier);
    }

    public JsonPropertyApplier (final String key, final Class<T> objectClass, final Class<V> valueClass, final IPropertyApplier<T, V> propertyApplier) {
        this.key = key;
        this.objectClass = objectClass;
        this.valueClass = valueClass;
        this.propertyApplier = propertyApplier;
    }

    /**
     * Calls {@link IPropertyApplier#apply(Object, Object)} if it should be called - or in other
     * words if the given key equaled {@link #key} and the object given is an instance of the
     * {@link #objectClass} value, and the {@link JsonElement} given contained a value that can
     * be converted to the {@link #valueClass}.
     *
     * @param key The key for the current {@link JsonElement}.
     * @param object The {@link Object} being applied to.
     * @param jsonElement The {@link JsonElement} for the key given.
     * @return The {@link PropertyApplierResult}, or null if application was not necessary.
     */
    @SuppressWarnings("unchecked")
    @Nullable
    public PropertyApplierResult applyIfShould(final String key, final Object object, final JsonElement jsonElement) {
        final IJsonObjectGetter<V> valueGetter = JsonObjectGetters.getObjectGetter(this.valueClass);

        if (!this.key.equalsIgnoreCase(key) || !this.objectClass.isInstance(object) || valueGetter == null)
            return null;

        final ObjectFetchResult<V> fetchResult = valueGetter.get(jsonElement);

        return fetchResult.wasSuccessful() ? this.propertyApplier.apply((T) object, fetchResult.getValue()) :
                new PropertyApplierResult(fetchResult.getErrorMessage());
    }

    public Class<T> getObjectClass() {
        return objectClass;
    }

    public Class<V> getValueClass() {
        return valueClass;
    }

}
