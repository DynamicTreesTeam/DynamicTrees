package com.dtteam.dynamictrees.deserialization.applier;

import com.dtteam.dynamictrees.deserialization.deserializer.Deserializer;
import com.dtteam.dynamictrees.deserialization.JsonDeserializers;
import com.dtteam.dynamictrees.deserialization.result.Result;
import com.dtteam.dynamictrees.utility.lazyvalue.LazyValue;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import org.apache.logging.log4j.LogManager;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.function.Function;

/**
 * @author Harley O'Connor
 */
public class ArrayPropertyApplier<T, V, I> extends PropertyApplier<T, List<V>, I> {

    private final Function<I, Result<Iterator<I>, I>> iteratorDeserialiser;
    private final LazyValue<Deserializer<I, V>> valueDeserialiser;

    public ArrayPropertyApplier(String key, Class<T> objectClass, Applier<T, List<V>> applier,
                                Function<I, Result<Iterator<I>, I>> iteratorDeserialiser,
                                LazyValue<Deserializer<I, V>> valueDeserialiser) {
        super(key, objectClass, applier);
        this.iteratorDeserialiser = iteratorDeserialiser;
        this.valueDeserialiser = valueDeserialiser;
    }

    @SuppressWarnings("unchecked")
    @Nullable
    @Override
    protected PropertyApplierResult applyIfShould(T object, I input, Applier<T, List<V>> applier) {
        final Result<Iterator<I>, I> iteratorResult = this.iteratorDeserialiser.apply(input);
        if (!iteratorResult.success()) {
            return null;
        }
        final List<V> values = new ArrayList<>();
        final Iterator<I> iterator = iteratorResult.get();
        while (iterator.hasNext()) {
            valueDeserialiser.get().deserialize(iterator.next()).ifSuccessOrElse(values::add, error -> LogManager.getLogger().error(error), warning -> LogManager.getLogger().warn(warning));
        }
        return applier.apply(object, values);
    }

    public static <T, V> ArrayPropertyApplier<T, V, JsonElement> json(String key, Class<T> objectClass,
                                                                      Class<V> valueClass,
                                                                      Applier<T, List<V>> applier) {
        return new ArrayPropertyApplier<>(key, objectClass, applier,
                element -> JsonDeserializers.JSON_ARRAY.deserialize(element).map(JsonArray::iterator),
                LazyValue.supplied(() -> JsonDeserializers.getOrThrow(valueClass)));
    }

}
