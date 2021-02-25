package com.ferreusveritas.dynamictrees.systems.genfeatures.config;

/**
 * Stores the value of a {@link GenFeatureProperty}.
 *
 * @author Harley O'Connor
 */
public class GenFeaturePropertyValue<V> {

    private final V value;

    public GenFeaturePropertyValue(V value) {
        this.value = value;
    }

    public V getValue() {
        return value;
    }

    @Override
    public String toString() {
        return "GenFeaturePropertyValue{" +
                "value=" + value +
                '}';
    }

}
