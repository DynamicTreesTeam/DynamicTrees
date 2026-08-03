package com.dtteam.dynamictrees.deserialization.deserializer.worldgen;


import com.dtteam.dynamictrees.deserialization.deserializer.JsonDeserializer;

/**
 * @author Harley O'Connor
 */
public interface JsonBiomeDatabaseDeserializer<T> extends JsonDeserializer<T> {

    String DEFAULT = "...";

    String STATIC = "static";
    String RANDOM = "random";
    @Deprecated String MATH = "math";
    @Deprecated String SCALE = "scale";

    default boolean isDefault(String candidate) {
        return DEFAULT.equals(candidate);
    }

}
