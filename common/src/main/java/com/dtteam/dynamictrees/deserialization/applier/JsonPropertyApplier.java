package com.dtteam.dynamictrees.deserialization.applier;

import com.dtteam.dynamictrees.api.lazyvalue.LazyValue;
import com.dtteam.dynamictrees.deserialization.JsonDeserializers;
import com.dtteam.dynamictrees.deserialization.deserializer.JsonDeserializer;
import com.google.gson.JsonElement;
import org.jetbrains.annotations.Nullable;

/**
 * @author Harley O'Connor
 */
public final class JsonPropertyApplier<O, V> extends PropertyApplier<O, V, JsonElement> {

    private final LazyValue<JsonDeserializer<V>> deserializer;

    public JsonPropertyApplier(String key, Class<O> objectClass, Class<V> valueClass, VoidApplier<O, V> propertyApplier) {
        this(key, objectClass, valueClass, (Applier<O, V>) propertyApplier);
    }

    public JsonPropertyApplier(String key, Class<O> objectClass, Class<V> valueClass, Applier<O, V> applier) {
        super(key, objectClass, applier);
        this.deserializer = LazyValue.supplied(() -> JsonDeserializers.getOrThrow(valueClass));
    }

    @SuppressWarnings("unchecked")
    @Nullable
    @Override
    protected PropertyApplierResult applyIfShould(O object, JsonElement input,
                                                  Applier<O, V> applier) {
        if (JsonDeserializers.JSON_NULL.deserialize(input).success())
            return JsonDeserializers.JSON_NULL.deserialize(input)
                    .map(value -> this.applier.apply(object, null))
                    .orElseApply(
                            PropertyApplierResult::failure,
                            PropertyApplierResult::addWarnings,
                            null
                    );
        return deserializer.get().deserialize(input)
                .map(value -> this.applier.apply(object, value))
                .orElseApply(
                        PropertyApplierResult::failure,
                        PropertyApplierResult::addWarnings,
                        null
                );
    }

}
