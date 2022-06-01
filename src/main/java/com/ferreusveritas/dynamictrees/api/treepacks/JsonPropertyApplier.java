package com.ferreusveritas.dynamictrees.api.treepacks;

import com.ferreusveritas.dynamictrees.deserialisation.JsonDeserialiser;
import com.ferreusveritas.dynamictrees.deserialisation.JsonDeserialisers;
import com.ferreusveritas.dynamictrees.util.LazyValue;
import com.google.gson.JsonElement;

import javax.annotation.Nullable;

/**
 * @author Harley O'Connor
 */
public final class JsonPropertyApplier<O, V> extends PropertyApplier<O, V, JsonElement> {

    private final LazyValue<JsonDeserialiser<V>> deserialiser;

    public JsonPropertyApplier(String key, Class<O> objectClass, Class<V> valueClass, VoidApplier<O, V> propertyApplier) {
        this(key, objectClass, valueClass, (Applier<O, V>) propertyApplier);
    }

    public JsonPropertyApplier(String key, Class<O> objectClass, Class<V> valueClass, Applier<O, V> applier) {
        super(key, objectClass, applier);
        this.deserialiser = LazyValue.supplied(() -> JsonDeserialisers.getOrThrow(valueClass));
    }

    @SuppressWarnings("unchecked")
    @Nullable
    @Override
    protected PropertyApplierResult applyIfShould(O object, JsonElement input,
                                                  Applier<O, V> applier) {
        return deserialiser.get().deserialise(input)
                .map(value -> this.applier.apply(object, value))
                .orElseApply(
                        PropertyApplierResult::failure,
                        PropertyApplierResult::addWarnings,
                        null
                );
    }

}
