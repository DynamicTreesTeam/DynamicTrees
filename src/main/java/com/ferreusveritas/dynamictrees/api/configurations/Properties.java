package com.ferreusveritas.dynamictrees.api.configurations;

import com.google.common.collect.Maps;

import javax.annotation.Nullable;
import java.util.Map;

/**
 * @author Harley O'Connor
 */
public final class Properties implements PropertiesAccessor {

    public static final Properties NONE = new Properties();

    private final Map<ConfigurationProperty<?>, Object> map = Maps.newHashMap();

    public <V> void put(ConfigurationProperty<V> property, V value) {
        this.map.put(property, value);
    }

    public void putAll(PropertiesAccessor properties) {
        properties.forEach(this::put);
    }

    @SuppressWarnings("unchecked")
    @Nullable
    public <V> V get(ConfigurationProperty<V> property) {
        return (V) this.map.get(property);
    }

    @Override
    public boolean has(ConfigurationProperty<?> property) {
        return this.map.containsKey(property);
    }

    @Override
    public void forEach(IterationAction<?> action) {
        this.map.forEach((property, value) -> this.applyIteration(action, property, value));
    }

    @SuppressWarnings("unchecked")
    private <V> void applyIteration(IterationAction<V> action, ConfigurationProperty<?> property, Object value) {
        action.apply(((ConfigurationProperty<V>) property), (V) value);
    }

    @Override
    public String toString() {
        return map.toString();
    }

}
