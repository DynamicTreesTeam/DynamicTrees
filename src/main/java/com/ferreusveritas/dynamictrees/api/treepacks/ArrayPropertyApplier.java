package com.ferreusveritas.dynamictrees.api.treepacks;

import com.ferreusveritas.dynamictrees.deserialisation.JsonDeserialisers;
import com.ferreusveritas.dynamictrees.deserialisation.result.Result;
import com.ferreusveritas.dynamictrees.util.Null;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.function.Function;

/**
 * @author Harley O'Connor
 */
public class ArrayPropertyApplier<T, V, I> extends PropertyApplier<T, V, I> {

    private final PropertyApplier<T, V, I> applier;
    private final Function<I, Result<Iterator<I>, I>> iteratorDeserialiser;

    public ArrayPropertyApplier(String key, Class<T> objectClass, Class<V> valueClass, PropertyApplier<T, V, I> applier, Function<I, Result<Iterator<I>, I>> iteratorDeserialiser) {
        super(key, objectClass, valueClass, applier.applier);
        this.applier = applier;
        this.iteratorDeserialiser = iteratorDeserialiser;
    }

    @Nullable
    @Override
    public PropertyApplierResult applyIfShould(String key, Object object, I input) {
        final Result<Iterator<I>, I> iteratorResult = this.iteratorDeserialiser.apply(input);

        if (!this.key.equalsIgnoreCase(key) || !this.objectClass.isInstance(object) || !iteratorResult.success()) {
            return null;
        }

        final List<String> warnings = new ArrayList<>();

        for (Iterator<I> it = iteratorResult.get(); it.hasNext(); ) {
            Null.consumeIfNonnull(
                    this.applier.applyIfShould(object, it.next(), this.valueClass, this.applier.applier),
                    result -> {
                        result.getError().ifPresent(warnings::add);
                        warnings.addAll(result.getWarnings());
                    }
            );
        }

        return PropertyApplierResult.success(warnings);
    }

    /**
     * @deprecated not used
     */
    @Deprecated
    @Nullable
    @Override
    protected <S, R> PropertyApplierResult applyIfShould(Object object, I input, Class<R> valueClass, Applier<S, R> applier) {
        return PropertyApplierResult.success();
    }

    public static <T, V> ArrayPropertyApplier<T, V, JsonElement> json(String key, Class<T> objectClass, Class<V> valueClass, PropertyApplier<T, V, JsonElement> applier) {
        return new ArrayPropertyApplier<>(key, objectClass, valueClass, applier,
                element -> JsonDeserialisers.JSON_ARRAY.deserialise(element).map(JsonArray::iterator));
    }

}
