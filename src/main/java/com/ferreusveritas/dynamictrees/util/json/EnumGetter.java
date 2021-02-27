package com.ferreusveritas.dynamictrees.util.json;

import com.electronwill.nightconfig.core.EnumGetMethod;
import com.google.gson.JsonElement;

/**
 * @author Harley O'Connor
 */
public final class EnumGetter<T extends Enum<T>> implements IJsonObjectGetter<T> {

    private final Class<T> enumType;

    public EnumGetter(Class<T> enumType) {
        this.enumType = enumType;
    }

    @Override
    public ObjectFetchResult<T> get(JsonElement jsonElement) {
        final ObjectFetchResult<String> stringFetchResult = JsonObjectGetters.STRING_GETTER.get(jsonElement);

        if (!stringFetchResult.wasSuccessful())
            return ObjectFetchResult.failureFromOther(stringFetchResult);

        T fetchedEnum = EnumGetMethod.NAME_IGNORECASE.get(stringFetchResult.getValue(), this.enumType);

        if (fetchedEnum == null)
            return ObjectFetchResult.failure("Couldn't get enum for '" + this.enumType + "' from value " + stringFetchResult.getValue());

        return ObjectFetchResult.success(fetchedEnum);
    }


}
