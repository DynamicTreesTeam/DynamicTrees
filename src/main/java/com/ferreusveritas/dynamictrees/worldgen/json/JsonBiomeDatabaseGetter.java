package com.ferreusveritas.dynamictrees.worldgen.json;

import com.ferreusveritas.dynamictrees.util.json.JsonGetter;

/**
 * @author Harley O'Connor
 */
public interface JsonBiomeDatabaseGetter<T> extends JsonGetter<T> {

    String DEFAULT = "...";

    String STATIC = "static";
    String RANDOM = "random";
    String MATH = "math";
    String SCALE = "scale";

    default boolean isDefault(String candidate) {
        return DEFAULT.equals(candidate);
    }

}
