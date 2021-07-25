package com.ferreusveritas.dynamictrees.util.json;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

/**
 * @author Harley O'Connor
 */
public final class MapGetter<K, V> implements IJsonObjectGetter<Map<K, V>> {

    private final IJsonObjectGetter<K> keyGetter;
    private final IJsonObjectGetter<V> valueGetter;
    private final Supplier<Map<K, V>> mapSupplier;

    public MapGetter(IJsonObjectGetter<K> keyGetter, IJsonObjectGetter<V> valueGetter) {
        this(keyGetter, valueGetter, HashMap::new);
    }

    public MapGetter(IJsonObjectGetter<K> keyGetter, IJsonObjectGetter<V> valueGetter, Supplier<Map<K, V>> mapSupplier) {
        this.keyGetter = keyGetter;
        this.valueGetter = valueGetter;
        this.mapSupplier = mapSupplier;
    }

    @Override
    public ObjectFetchResult<Map<K, V>> get(JsonElement jsonElement) {
        final String[] errorMsg = {null};
        return JsonObjectGetters.JSON_OBJECT.get(jsonElement).map(object -> {
            final Map<K, V> map = this.mapSupplier.get();
            object.entrySet().forEach(entry ->
                    this.valueGetter.get(entry.getValue()).ifSuccessful(value ->
                            this.keyGetter.get(new JsonPrimitive(entry.getKey()))
                                    .ifSuccessful(key -> map.put(key, value)).elseIfError(err -> errorMsg[0] = err)
                    ).elseIfError(err -> {if (errorMsg[0] == null) errorMsg[0] = err;} )
            );
            return map;
        }).setErrorMessage(errorMsg[0]);
    }

    public static <K, V> Class<Map<K, V>> getMapClass (Class<K> keyClass, Class<V> valueClass) {
        return getMapClass(keyClass, valueClass, HashMap::new);
    }

    @SuppressWarnings({"MismatchedQueryAndUpdateOfCollection", "unchecked"})
    public static <K, V> Class<Map<K, V>> getMapClass (Class<K> keyClass, Class<V> valueClass, Supplier<Map<K, V>> mapSupplier) {
        return (Class<Map<K, V>>) mapSupplier.get().getClass();
    }

}
