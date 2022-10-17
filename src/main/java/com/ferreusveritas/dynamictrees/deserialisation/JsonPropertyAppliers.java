package com.ferreusveritas.dynamictrees.deserialisation;

import com.ferreusveritas.dynamictrees.api.treepacks.*;
import com.google.common.collect.Lists;
import com.google.gson.JsonElement;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Manages a list of {@link PropertyApplier} objects of type {@link O}, allowing for an easy way of storing,
 * registering, and applying property appliers.
 *
 * @param <O> the type of object the appliers for this list handle applying to
 * @author Harley O'Connor
 */
public final class JsonPropertyAppliers<O> implements PropertyAppliers<O, JsonElement> {

    private final Class<O> objectType;
    private final List<PropertyApplier<? extends O, ?, JsonElement>> appliers = Lists.newLinkedList();

    public JsonPropertyAppliers(final Class<O> objectType) {
        this.objectType = objectType;
    }

    @Override
    public ResultList applyAll(final Map<String, JsonElement> json, final O object) {
        final ResultList results = new ResultList();

        for (Map.Entry<String, JsonElement> entry : json.entrySet()) {
            results.add(this.apply(object, entry.getKey(), entry.getValue()));
        }

        return results;
    }

    @Override
    public PropertyApplierResult apply(final O object, final String key, final JsonElement jsonElement) {
        // If the element is a comment, ignore it and move onto next entry.
        if (JsonHelper.isComment(jsonElement)) {
            return PropertyApplierResult.success();
        }

        final List<String> warnings = new ArrayList<>();

        for (final PropertyApplier<? extends O, ?, JsonElement> applier : this.appliers) {
            if (!applier.getObjectClass().isInstance(object)) {
                continue;
            }

            final PropertyApplierResult result = applier.applyIfShould(key, object, jsonElement);

            // If the result is null, it's not the right applier, so move onto the next one.
            if (result == null) {
                continue;
            }

            // If the application wasn't successful, return the error.
            if (!result.wasSuccessful()) {
                return result.addErrorPrefix("[" + key + "] ").addWarningsPrefix("[" + key + "] ");
            }

            warnings.addAll(result.getWarnings());
            break; // We have read (or tried to read) this entry, so move onto the next.
        }

        return PropertyApplierResult.success(warnings).addWarningsPrefix("[" + key + "] ");
    }

    @Override
    public <E extends O> JsonPropertyAppliers<O> register(final PropertyApplier<E, ?, JsonElement> applier) {
        this.appliers.add(applier);
        return this;
    }

    @Override
    public <V> JsonPropertyAppliers<O> register(final String key, final Class<V> valueClass,
                                                final Applier<O, V> applier) {
        return this.register(key, this.objectType, valueClass, applier);
    }

    @Override
    public <V> JsonPropertyAppliers<O> register(final String key, final Class<V> valueClass,
                                                final VoidApplier<O, V> applier) {
        return this.register(key, this.objectType, valueClass, applier);
    }

    @Override
    public <V> JsonPropertyAppliers<O> registerArrayApplier(final String key, final Class<V> valueClass,
                                                            final Applier<O, V> applier) {
        return this.registerArrayApplier(key, this.objectType, valueClass, applier);
    }

    @Override
    public <V> JsonPropertyAppliers<O> registerArrayApplier(final String key, final Class<V> valueClass,
                                                            final VoidApplier<O, V> applier) {
        return this.registerArrayApplier(key, this.objectType, valueClass, applier);
    }

    @Override
    public <V> PropertyAppliers<O, JsonElement> registerListApplier(String key, Class<V> valueClass,
                                                                    Applier<O, List<V>> applier) {
        return this.registerListApplier(key, this.objectType, valueClass, applier);
    }

    @Override
    public <V> PropertyAppliers<O, JsonElement> registerListApplier(String key, Class<V> valueClass,
                                                                    VoidApplier<O, List<V>> applier) {
        return this.registerListApplier(key, this.objectType, valueClass, applier);
    }

    @Override
    public JsonPropertyAppliers<O> registerIfTrueApplier(final String key, final IfTrueApplier<O> applier) {
        return this.registerIfTrueApplier(key, this.objectType, applier);
    }

    @Override
    public <E extends O, V> JsonPropertyAppliers<O> register(final String key, final Class<E> subClass,
                                                             final Class<V> valueClass, final Applier<E, V> applier) {
        return this.register(new JsonPropertyApplier<>(key, subClass, valueClass, applier));
    }

    @Override
    public <E extends O, V> JsonPropertyAppliers<O> register(final String key, final Class<E> subClass,
                                                             final Class<V> valueClass,
                                                             final VoidApplier<E, V> applier) {
        return this.register(new JsonPropertyApplier<>(key, subClass, valueClass, applier));
    }

    @Override
    public <E extends O> JsonPropertyAppliers<O> registerIfTrueApplier(final String key, final Class<E> subClass,
                                                                       final IfTrueApplier<E> applier) {
        return this.register(key, subClass, Boolean.class, (object, value) -> {
            if (value) {
                applier.apply(object);
            }
        });
    }

    @Override
    public <E extends O, V> JsonPropertyAppliers<O> registerArrayApplier(final String key, final Class<E> subClass,
                                                                         final Class<V> valueClass,
                                                                         final Applier<E, V> applier) {
        return this.register(ArrayIteratorPropertyApplier.json(key, subClass, valueClass,
                new JsonPropertyApplier<>("", subClass, valueClass, applier)));
    }

    @Override
    public <E extends O, V> JsonPropertyAppliers<O> registerArrayApplier(final String key, final Class<E> subClass,
                                                                         final Class<V> valueClass,
                                                                         final VoidApplier<E, V> applier) {
        return this.register(ArrayIteratorPropertyApplier.json(key, subClass, valueClass,
                new JsonPropertyApplier<>("", subClass, valueClass, applier)));
    }

    @Override
    public <E extends O, V> PropertyAppliers<O, JsonElement> registerListApplier(String key, Class<E> subClass,
                                                                                 Class<V> valueClass,
                                                                                 Applier<E, List<V>> applier) {
        return this.register(ArrayPropertyApplier.json(key, subClass, valueClass, applier));
    }

    @Override
    public <E extends O, V> PropertyAppliers<O, JsonElement> registerListApplier(String key, Class<E> subClass,
                                                                                 Class<V> valueClass,
                                                                                 VoidApplier<E, List<V>> applier) {
        return this.register(ArrayPropertyApplier.json(key, subClass, valueClass, applier));
    }

    @Override
    public Class<O> getObjectType() {
        return objectType;
    }

}
