package com.ferreusveritas.dynamictrees.api.configurations;

/**
 * Stores the value of a {@link ConfigurationProperty}.
 *
 * @author Harley O'Connor
 */
public class ConfigurationPropertyValue<V> {

    private final V value;

    public ConfigurationPropertyValue(V value) {
        this.value = value;
    }

    public V getValue() {
        return value;
    }

    @Override
    public String toString() {
        return "ConfigurationPropertyValue{" +
                "value=" + value +
                '}';
    }

}
