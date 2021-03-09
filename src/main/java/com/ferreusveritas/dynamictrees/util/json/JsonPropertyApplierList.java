package com.ferreusveritas.dynamictrees.util.json;

import com.ferreusveritas.dynamictrees.api.datapacks.*;
import com.ferreusveritas.dynamictrees.trees.Species;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author Harley O'Connor
 */
public final class JsonPropertyApplierList<T> {

    private final Class<T> objectType;
    private final List<JsonPropertyApplier<? extends T, ?>> appliers = new ArrayList<>();

    public JsonPropertyApplierList(final Class<T> objectType) {
        this.objectType = objectType;
    }

    public List<PropertyApplierResult> applyAll(final JsonObject jsonObject, final T object) {
        final List<PropertyApplierResult> failureResults = new ArrayList<>();

        for (Map.Entry<String, JsonElement> entry : jsonObject.entrySet()) {
            final PropertyApplierResult result = this.apply(object, entry.getKey(), entry.getValue());

            if (!result.wasSuccessful())
                failureResults.add(result);
        }

        return failureResults;
    }

    public PropertyApplierResult apply(final T object, final String key, final JsonElement jsonElement) {
        // If the element is a comment, ignore it and move onto next entry.
        if (JsonHelper.isComment(jsonElement))
            return PropertyApplierResult.SUCCESS;

        for (final JsonPropertyApplier<? extends T, ?> applier : this.appliers) {
            if (!applier.getObjectClass().isInstance(object))
                continue;

            final PropertyApplierResult result = applier.applyIfShould(key, object, jsonElement);

            // If the result is null, it's not the right applier, so move onto the next one.
            if (result == null)
                continue;

            // If the application wasn't successful, return the error.
            if (!result.wasSuccessful())
                return result;

            break; // We have read (or tried to read) this entry, so move onto the next.
        }

        return PropertyApplierResult.SUCCESS;
    }

    public <E extends T> JsonPropertyApplierList<T> register (final JsonPropertyApplier<E, ?> applier) {
        this.appliers.add(applier);
        return this;
    }

    public <V> JsonPropertyApplierList<T> register(final String key, final Class<V> valueClass, final IPropertyApplier<T, V> applier) {
        return this.register(key, this.objectType, valueClass, applier);
    }

    public <V> JsonPropertyApplierList<T> register(final String key, final Class<V> valueClass, final IVoidPropertyApplier<T, V> applier) {
        return this.register(key, this.objectType, valueClass, applier);
    }

    public <V> JsonPropertyApplierList<T> registerArrayApplier(final String key, final Class<V> valueClass, final IPropertyApplier<T, V> applier) {
        return this.registerArrayApplier(key, this.objectType, valueClass, applier);
    }

    public <V> JsonPropertyApplierList<T> registerArrayApplier(final String key, final Class<V> valueClass, final IVoidPropertyApplier<T, V> applier) {
        return this.registerArrayApplier(key, this.objectType, valueClass, applier);
    }

    public JsonPropertyApplierList<T> registerIfTrueApplier(final String key, final IIfTrueApplier<T> applier) {
        return this.registerIfTrueApplier(key, this.objectType, applier);
    }

    public <E extends T, V> JsonPropertyApplierList<T> register(final String key, final Class<E> subClass, final Class<V> valueClass, final IPropertyApplier<E, V> applier) {
        return this.register(new JsonPropertyApplier<>(key, subClass, valueClass, applier));
    }

    public <E extends T, V> JsonPropertyApplierList<T> register(final String key, final Class<E> subClass, final Class<V> valueClass, final IVoidPropertyApplier<E, V> applier) {
        return this.register(new JsonPropertyApplier<>(key, subClass, valueClass, applier));
    }

    public <E extends T> JsonPropertyApplierList<T> registerIfTrueApplier(final String key, final Class<E> subClass, final IIfTrueApplier<E> applier) {
        return this.register(key, subClass, Boolean.class, (object, value) -> { if (value) applier.apply(object); });
    }

    public <E extends T, V> JsonPropertyApplierList<T> registerArrayApplier(final String key, final Class<E> subClass, final Class<V> valueClass, final IPropertyApplier<E, V> applier) {
        return this.register(new JsonArrayPropertyApplier<>(key, subClass, valueClass, new JsonPropertyApplier<>("", subClass, valueClass , applier)));
    }

    public <E extends T, V> JsonPropertyApplierList<T> registerArrayApplier(final String key, final Class<E> subClass, final Class<V> valueClass, final IVoidPropertyApplier<E, V> applier) {
        return this.register(new JsonArrayPropertyApplier<>(key, subClass, valueClass, new JsonPropertyApplier<>("", subClass, valueClass , applier)));
    }

}
