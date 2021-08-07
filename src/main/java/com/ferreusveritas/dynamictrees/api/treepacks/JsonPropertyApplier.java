package com.ferreusveritas.dynamictrees.api.treepacks;

import com.ferreusveritas.dynamictrees.deserialisation.DeserialisationResult;
import com.ferreusveritas.dynamictrees.deserialisation.JsonDeserialiser;
import com.ferreusveritas.dynamictrees.deserialisation.JsonDeserialisers;
import com.google.gson.JsonElement;

import javax.annotation.Nullable;

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
        final JsonDeserialiser<R> valueGetter = JsonDeserialisers.get(valueClass);

        if (!valueGetter.isValid())
            return null;

        final DeserialisationResult<R> result = valueGetter.deserialise(input);

        return result.wasSuccessful() ? applier.apply((S) object, result.getValue()) :
                PropertyApplierResult.failure(result);
    }

}
