package com.ferreusveritas.dynamictrees.util.json;

import com.google.gson.JsonElement;

import java.util.LinkedList;
import java.util.List;

public class ListGetter<T> implements IJsonObjectGetter<List<T>> {

    private final IJsonObjectGetter<T> thisGetter;

    public ListGetter (IJsonObjectGetter<T> thisGetter){
        this.thisGetter = thisGetter;
    }

    @Override
    public ObjectFetchResult<List<T>> get(JsonElement jsonElement) {
        return JsonObjectGetters.JSON_ARRAY.get(jsonElement).map(array-> {
            List<T> getterList = new LinkedList<>();
            array.forEach(elem-> thisGetter.get(elem).ifSuccessful(getterList::add));
            return getterList;
        });
    }

    @SuppressWarnings({"MismatchedQueryAndUpdateOfCollection", "unchecked"})
    public static <T> Class<List<T>> getListClass (Class<T> c){
        List<T> instance = new LinkedList<>();
        return (Class<List<T>>) instance.getClass();
    }
}