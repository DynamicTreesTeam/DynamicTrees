package com.ferreusveritas.dynamictrees.util.json;

import com.google.gson.JsonElement;

import java.util.LinkedList;
import java.util.List;
import java.util.function.Supplier;

public final class ListGetter<T> implements IJsonObjectGetter<List<T>> {

    private final IJsonObjectGetter<T> thisGetter;
    private final Supplier<List<T>> listSupplier;

    public ListGetter (IJsonObjectGetter<T> thisGetter){
        this(thisGetter, LinkedList::new);
    }

    public ListGetter(IJsonObjectGetter<T> thisGetter, Supplier<List<T>> listSupplier) {
        this.thisGetter = thisGetter;
        this.listSupplier = listSupplier;
    }

    @Override
    public ObjectFetchResult<List<T>> get(JsonElement jsonElement) {
        return JsonObjectGetters.JSON_ARRAY.get(jsonElement).map(array -> {
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