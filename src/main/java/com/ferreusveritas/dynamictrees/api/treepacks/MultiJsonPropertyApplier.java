package com.ferreusveritas.dynamictrees.api.treepacks;

import com.google.gson.JsonElement;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

/**
 * An applier for applying multiple {@link JsonPropertyApplier} with different values for
 * the same key.
 *
 * @author Harley O'Connor
 */
public class MultiJsonPropertyApplier<T> extends JsonPropertyApplier<T, Object> {

    private final List<JsonPropertyApplier<T, ?>> appliers = new ArrayList<>();

    @SafeVarargs
    public MultiJsonPropertyApplier (final String key, final Class<T> objectClass, final JsonPropertyApplier<T, ?>... appliers) {
        super(key, objectClass, Object.class, (object, value) -> {});
        this.appliers.addAll(Arrays.asList(appliers));
    }

    public void addApplier (final JsonPropertyApplier<T, ?> applier) {
        this.appliers.add(applier);
    }

    @Nullable
    @Override
    public PropertyApplierResult applyIfShould(String keyIn, Object object, JsonElement jsonElement) {
        if (!this.key.equalsIgnoreCase(keyIn) || !this.objectClass.isInstance(object))
            return null;

        final Iterator<JsonPropertyApplier<T, ?>> iterator = appliers.iterator();
        PropertyApplierResult applierResult;

        do {
            applierResult = this.applyIfShould(object, jsonElement, iterator.next());
        } while (applierResult == null || !applierResult.wasSuccessful());

        return applierResult;
    }

    @Nullable
    private <S, R> PropertyApplierResult applyIfShould(final Object object, final JsonElement jsonElement, final JsonPropertyApplier<S, R> applier) {
        return this.applyIfShould(object, jsonElement, applier.getValueClass(), applier.propertyApplier);
    }

}
