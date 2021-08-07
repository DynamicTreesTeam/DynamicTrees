package com.ferreusveritas.dynamictrees.worldgen.deserialisation;

import com.ferreusveritas.dynamictrees.deserialisation.JsonDeserialiser;

/**
 * @author Harley O'Connor
 */
public interface JsonBiomeDatabaseDeserialiser<T> extends JsonDeserialiser<T> {

    String DEFAULT = "...";

    String STATIC = "static";
    String RANDOM = "random";
    String MATH = "math";
    String SCALE = "scale";

    default boolean isDefault(String candidate) {
        return DEFAULT.equals(candidate);
    }

}
