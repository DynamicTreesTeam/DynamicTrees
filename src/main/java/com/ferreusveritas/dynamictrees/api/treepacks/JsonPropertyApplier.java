package com.ferreusveritas.dynamictrees.api.treepacks;

import com.ferreusveritas.dynamictrees.deserialisation.JsonDeserialisers;
import com.google.gson.JsonElement;

import javax.annotation.Nullable;
import java.util.List;
import java.util.function.BiFunction;

/**
 * @author Harley O'Connor
 */
public final class JsonPropertyApplier<O, V> extends PropertyApplier<O, V, JsonElement> {

    public JsonPropertyApplier(String key, Class<O> objectClass, Class<V> valueClass, VoidApplier<O, V> propertyApplier) {
        super(key, objectClass, valueClass, propertyApplier);
    }

    public JsonPropertyApplier(String key, Class<O> objectClass, Class<V> valueClass, Applier<O, V> applier) {
        super(key, objectClass, valueClass, applier);
    }

    @SuppressWarnings("unchecked")
    @Nullable
    @Override
    protected <S, R> PropertyApplierResult applyIfShould(Object object, JsonElement input, Class<R> valueClass, Applier<S, R> applier) {
        return JsonDeserialisers.getOrThrow(valueClass).deserialise(input)
                .map(value -> applier.apply((S) object, value))
                .orElseApply(
                        PropertyApplierResult::failure,
                        PropertyApplierResult::addWarnings,
                        null
                );
    }

}
