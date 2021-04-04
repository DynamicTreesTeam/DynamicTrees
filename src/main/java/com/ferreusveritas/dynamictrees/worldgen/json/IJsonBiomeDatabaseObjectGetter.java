package com.ferreusveritas.dynamictrees.worldgen.json;

import com.ferreusveritas.dynamictrees.util.json.IJsonObjectGetter;

/**
 * @author Harley O'Connor
 */
public interface IJsonBiomeDatabaseObjectGetter<T> extends IJsonObjectGetter<T> {

    String DEFAULT = "...";

    String STATIC = "static";
    String RANDOM = "random";
    String MATH = "math";
    String SCALE = "scale";

    default boolean isDefault(String candidate) {
        return DEFAULT.equals(candidate);
    }

}
