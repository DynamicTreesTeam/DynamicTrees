package com.ferreusveritas.dynamictrees.api.configurations;

/**
 * @author Harley O'Connor
 */
public final class PropertyDefinition<T> {

    private static final String PREFIX = "#";

    public static final PropertyDefinition<Object> NULL =
            new PropertyDefinition<>("null", Object.class, new Object(), ConfigurationProperty.NULL);

    private final String key;
    private final Class<T> type;
    private final T defaultValue;
    private final ConfigurationProperty<T> property;

    public PropertyDefinition(String key, Class<T> type, T defaultValue) {
        this(key, type, defaultValue, new CustomConfigurationProperty<>(key, type));
    }

    private PropertyDefinition(String key, Class<T> type, T defaultValue,
                               ConfigurationProperty<T> property) {
        this.key = key;
        this.type = type;
        this.defaultValue = defaultValue;
        this.property = property;
    }

    public ConfigurationProperty<T> getProperty() {
        return property;
    }

    public String process(String input, PropertiesAccessor properties) {
        return input.replaceAll(PREFIX + this.key, String.valueOf(
                this.getOrDefault(properties)
        ));
    }

    private Object getOrDefault(PropertiesAccessor properties) {
        return properties.has(property) ? properties.get(this.property) : defaultValue;
    }

    @SuppressWarnings("unchecked")
    public static Class<PropertyDefinition<?>> captureClass() {
        return (Class<PropertyDefinition<?>>) NULL.getClass();
    }

}
