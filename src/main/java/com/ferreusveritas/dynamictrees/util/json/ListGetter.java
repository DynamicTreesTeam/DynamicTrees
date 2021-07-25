package com.ferreusveritas.dynamictrees.util.json;

import com.google.gson.JsonElement;

import java.util.LinkedList;
import java.util.List;
import java.util.function.Supplier;

public final class ListGetter<T> implements JsonGetter<List<T>> {

    private final JsonGetter<T> thisGetter;
    private final Supplier<List<T>> listSupplier;

    public ListGetter (JsonGetter<T> thisGetter){
        this(thisGetter, LinkedList::new);
    }

    public ListGetter(JsonGetter<T> thisGetter, Supplier<List<T>> listSupplier) {
        this.thisGetter = thisGetter;
        this.listSupplier = listSupplier;
    }

    @Override
    public FetchResult<List<T>> get(JsonElement jsonElement) {
        return JsonGetters.JSON_ARRAY.get(jsonElement).map(array -> {
            final List<T> getterList = this.listSupplier.get();
            array.forEach(elem -> thisGetter.get(elem).ifSuccessful(getterList::add));
            return getterList;
        });
    }

    public static <T> Class<List<T>> getListClass (Class<T> c) {
        return getListClass(c, LinkedList::new);
    }

    @SuppressWarnings({"MismatchedQueryAndUpdateOfCollection", "unchecked"})
    public static <T> Class<List<T>> getListClass (Class<T> c, Supplier<List<T>> listSupplier) {
        List<T> instance = listSupplier.get();
        return (Class<List<T>>) instance.getClass();
    }
}