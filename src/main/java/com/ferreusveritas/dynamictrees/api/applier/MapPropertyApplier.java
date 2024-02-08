package com.ferreusveritas.dynamictrees.api.applier;

import com.ferreusveritas.dynamictrees.deserialisation.Deserialiser;
import com.ferreusveritas.dynamictrees.deserialisation.JsonDeserialisers;
import com.ferreusveritas.dynamictrees.util.LazyValue;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.apache.logging.log4j.LogManager;

import javax.annotation.Nullable;
import java.util.*;
import java.util.function.Function;

/**
 * @author Max Hyper
 */
public class MapPropertyApplier<T, V, I> extends PropertyApplier<T, Map<String,V>, I> {

    private final Function<I, JsonObject> jsonObjectDeserializer;
    private final LazyValue<Deserialiser<JsonElement, V>> valueDeserialiser;

    public MapPropertyApplier(String key, Class<T> objectClass, Applier<T, Map<String,V>> applier,
                              Function<I, JsonObject> jsonObjectDeserializer,
                              LazyValue<Deserialiser<JsonElement, V>> valueDeserialiser) {
        super(key, objectClass, applier);
        this.jsonObjectDeserializer = jsonObjectDeserializer;
        this.valueDeserialiser = valueDeserialiser;
    }

    @Nullable
    @Override
    protected PropertyApplierResult applyIfShould(T object, I input, Applier<T, Map<String,V>> applier) {
        HashMap<String, V> values = new HashMap<>();
        jsonObjectDeserializer.apply(input).entrySet().forEach((entry)->
                valueDeserialiser.get().deserialise(entry.getValue()).ifSuccessOrElse(v->values.put(entry.getKey(),v), error -> LogManager.getLogger().error(error), warning -> LogManager.getLogger().warn(warning)));
        return applier.apply(object, values);
    }

    public static <T, V> MapPropertyApplier<T, V, JsonElement> json(String key, Class<T> objectClass,
                                                                    Class<V> valueClass,
                                                                    Applier<T, Map<String,V>> applier) {
        return new MapPropertyApplier<>(key, objectClass, applier,
                element -> JsonDeserialisers.JSON_OBJECT.deserialise(element).get(),
                LazyValue.supplied(() -> JsonDeserialisers.getOrThrow(valueClass)));
    }

}
