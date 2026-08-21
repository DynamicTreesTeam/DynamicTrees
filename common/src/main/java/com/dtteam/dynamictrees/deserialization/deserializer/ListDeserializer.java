package com.dtteam.dynamictrees.deserialization.deserializer;

import com.dtteam.dynamictrees.deserialization.JsonDeserializers;
import com.dtteam.dynamictrees.deserialization.result.Result;
import com.google.gson.JsonElement;

import java.util.LinkedList;
import java.util.List;
import java.util.function.Supplier;

public final class ListDeserializer<T> implements JsonDeserializer<List<T>> {

    private final JsonDeserializer<T> thisGetter;
    private final Supplier<List<T>> listSupplier;

    public ListDeserializer(JsonDeserializer<T> thisGetter) {
        this(thisGetter, LinkedList::new);
    }

    public ListDeserializer(JsonDeserializer<T> thisGetter, Supplier<List<T>> listSupplier) {
        this.thisGetter = thisGetter;
        this.listSupplier = listSupplier;
    }

    public Result<List<T>, JsonElement> deserialize(JsonElement jsonElement) {
        return JsonDeserializers.JSON_ARRAY.deserialize(jsonElement).map(array -> {
            final List<T> getterList = this.listSupplier.get();
            array.forEach(elem -> {
                thisGetter.deserialize(elem).ifSuccess(getterList::add);
            });
            return getterList;
        });
    }

    public static <T> Class<List<T>> getListClass(Class<T> c) {
        return getListClass(c, LinkedList::new);
    }

    @SuppressWarnings({"MismatchedQueryAndUpdateOfCollection", "unchecked"})
    public static <T> Class<List<T>> getListClass(Class<T> c, Supplier<List<T>> listSupplier) {
        List<T> instance = listSupplier.get();
        return (Class<List<T>>) instance.getClass();
    }
}