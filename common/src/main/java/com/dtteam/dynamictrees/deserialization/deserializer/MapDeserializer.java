package com.dtteam.dynamictrees.deserialization.deserializer;

import com.dtteam.dynamictrees.deserialization.JsonDeserializers;
import com.dtteam.dynamictrees.deserialization.result.Result;
import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

/**
 * @author Harley O'Connor
 */
public final class MapDeserializer<K, V> implements JsonDeserializer<Map<K, V>> {

    private final JsonDeserializer<K> keyGetter;
    private final JsonDeserializer<V> valueGetter;
    private final Supplier<Map<K, V>> mapSupplier;

    public MapDeserializer(JsonDeserializer<K> keyGetter, JsonDeserializer<V> valueGetter) {
        this(keyGetter, valueGetter, HashMap::new);
    }

    public MapDeserializer(JsonDeserializer<K> keyGetter, JsonDeserializer<V> valueGetter, Supplier<Map<K, V>> mapSupplier) {
        this.keyGetter = keyGetter;
        this.valueGetter = valueGetter;
        this.mapSupplier = mapSupplier;
    }

    @Override
    public Result<Map<K, V>, JsonElement> deserialize(JsonElement jsonElement) {
        return JsonDeserializers.JSON_OBJECT.deserialize(jsonElement).map((object, warningConsumer) -> {
            final Map<K, V> map = this.mapSupplier.get();
            for (Map.Entry<String, JsonElement> entry : object.entrySet()) {
                this.valueGetter.deserialize(entry.getValue()).map(
                        value -> this.keyGetter.deserialize(new JsonPrimitive(entry.getKey()))
                                .ifSuccessOrElseThrow(key -> map.put(key, value), warningConsumer)
                ).orElseThrow();
            }
            return map;
        });
    }

    public static <K, V> Class<Map<K, V>> getMapClass(Class<K> keyClass, Class<V> valueClass) {
        return getMapClass(keyClass, valueClass, HashMap::new);
    }

    @SuppressWarnings({"MismatchedQueryAndUpdateOfCollection", "unchecked"})
    public static <K, V> Class<Map<K, V>> getMapClass(Class<K> keyClass, Class<V> valueClass, Supplier<Map<K, V>> mapSupplier) {
        return (Class<Map<K, V>>) mapSupplier.get().getClass();
    }

}
