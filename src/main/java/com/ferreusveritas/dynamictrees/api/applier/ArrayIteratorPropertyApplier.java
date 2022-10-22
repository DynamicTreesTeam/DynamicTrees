package com.ferreusveritas.dynamictrees.api.applier;

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
public class ArrayIteratorPropertyApplier<T, V, I> extends PropertyApplier<T, V, I> {

    private final PropertyApplier<T, V, I> propertyApplier;
    private final Function<I, Result<Iterator<I>, I>> iteratorDeserialiser;

    public ArrayIteratorPropertyApplier(String key, Class<T> objectClass, Class<V> valueClass, PropertyApplier<T, V, I> applier,
                                        Function<I, Result<Iterator<I>, I>> iteratorDeserialiser) {
        super(key, objectClass, applier.applier);
        this.propertyApplier = applier;
        this.iteratorDeserialiser = iteratorDeserialiser;
    }

    /**
     * @deprecated not used
     */
    @Deprecated
    @Nullable
    @Override
    protected PropertyApplierResult applyIfShould(T object, I input, Applier<T, V> applier) {
        final Result<Iterator<I>, I> iteratorResult = this.iteratorDeserialiser.apply(input);
        if (!iteratorResult.success()) {
            return null;
        }
        final List<String> warnings = new ArrayList<>();
        final Iterator<I> iterator = iteratorResult.get();
        while (iterator.hasNext()) {
            Null.consumeIfNonnull(
                    this.propertyApplier.applyIfShould(object, iterator.next(), applier),
                    result -> {
                        result.getError().ifPresent(warnings::add);
                        warnings.addAll(result.getWarnings());
                    }
            );
        }
        return PropertyApplierResult.success(warnings);
    }

    public static <T, V> ArrayIteratorPropertyApplier<T, V, JsonElement> json(String key, Class<T> objectClass,
                                                                              Class<V> valueClass,
                                                                              PropertyApplier<T, V, JsonElement> applier) {
        return new ArrayIteratorPropertyApplier<>(key, objectClass, valueClass, applier,
                element -> JsonDeserialisers.JSON_ARRAY.deserialise(element).map(JsonArray::iterator));
    }

}
