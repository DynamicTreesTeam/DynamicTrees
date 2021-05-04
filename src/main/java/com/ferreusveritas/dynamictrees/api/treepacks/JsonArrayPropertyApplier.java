package com.ferreusveritas.dynamictrees.api.treepacks;

import com.ferreusveritas.dynamictrees.util.Null;
import com.ferreusveritas.dynamictrees.util.json.JsonObjectGetters;
import com.ferreusveritas.dynamictrees.util.json.ObjectFetchResult;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Harley O'Connor
 */
public class JsonArrayPropertyApplier<T, V> extends JsonPropertyApplier<T, V> {

    private final PropertyApplier<T, V> jsonApplier;

    public JsonArrayPropertyApplier(String key, Class<T> objectClass, Class<V> valueClass, JsonPropertyApplier<T, V> applier) {
        super(key, objectClass, valueClass, applier.propertyApplier);
        this.jsonApplier = new PropertyApplier<>(this, applier.propertyApplier);
    }

    @Nullable
    @Override
    public PropertyApplierResult applyIfShould(String keyIn, Object object, JsonElement jsonElement) {
        final ObjectFetchResult<JsonArray> arrayFetchResult = JsonObjectGetters.JSON_ARRAY.get(jsonElement);

        if (!this.key.equalsIgnoreCase(keyIn) || !this.objectClass.isInstance(object) || !arrayFetchResult.wasSuccessful())
            return null;

        final List<String> warnings = new ArrayList<>();
        final JsonArray jsonArray = arrayFetchResult.getValue();

        for (final JsonElement element : jsonArray)
            Null.consumeIfNonnull(this.jsonApplier.applyIfShould(object, element, this.valueClass, this.jsonApplier.propertyApplier), result -> warnings.addAll(result.getWarnings()));

        return PropertyApplierResult.success(warnings);
    }

    private static class PropertyApplier<T, V> extends JsonPropertyApplier<T, V> {
        public PropertyApplier(JsonArrayPropertyApplier<T, V> arrayApplier, IPropertyApplier<T, V> propertyApplier) {
            super(arrayApplier.key, arrayApplier.objectClass, arrayApplier.valueClass, propertyApplier);
        }
    }

}
