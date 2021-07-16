package com.ferreusveritas.dynamictrees.util.json;

import com.ferreusveritas.dynamictrees.api.configurations.Configurable;
import com.ferreusveritas.dynamictrees.api.configurations.Configured;
import com.ferreusveritas.dynamictrees.api.registry.ConfigurableRegistryEntry;
import com.ferreusveritas.dynamictrees.init.DTTrees;
import com.ferreusveritas.dynamictrees.systems.genfeatures.config.ConfiguredGenFeature;
import com.ferreusveritas.dynamictrees.api.configurations.ConfigurationProperty;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

/**
 * @author Harley O'Connor
 */
public final class ConfiguredGetter<T extends Configured<T, C>, C extends Configurable> implements IJsonObjectGetter<T> {

    private final String configurableName;
    private final Class<C> configurableClass;

    public ConfiguredGetter(String configurableName, Class<C> configurableClass) {
        this.configurableName = configurableName;
        this.configurableClass = configurableClass;
    }

    @SuppressWarnings("unchecked")
    @Override
    public ObjectFetchResult<T> get(final JsonElement jsonElement) {
        final ObjectFetchResult<T> configured = new ObjectFetchResult<>();

        // TODO: Check error logging here.
        JsonHelper.JsonElementReader.of(jsonElement).ifOfType(configurableClass, configurable -> configured.setValue((T) configurable.getDefaultConfiguration()))
                .ifFailed(configured::setErrorMessage)
                .elseIfOfType(JsonObject.class, jsonObject ->
                    JsonHelper.JsonObjectReader.of(jsonObject).ifContains("name", configurableClass, configurable -> configured.setValue((T) configurable.getDefaultConfiguration()))
                            .ifContains("properties", JsonObject.class, propertiesObject -> {
                                setProperties(configured, propertiesObject);
                            })
                );

        final T value = configured.getValue();
        if (value == null || value.getConfigurable() == null || (value.getConfigurable() instanceof ConfigurableRegistryEntry &&
                ((ConfigurableRegistryEntry<?, ?>) value.getConfigurable()).getRegistryName().equals(DTTrees.NULL))) {
            return ObjectFetchResult.failure(configurableName + " couldn't be found from name or wasn't given a name.");
        }

        return configured;
    }

    public static <T extends Configured<T, ?>> void setProperties (ObjectFetchResult<T> fetchResult, JsonObject object){
        if (fetchResult.getValue() == ConfiguredGenFeature.NULL_CONFIGURED_FEATURE || fetchResult.getValue() == null || fetchResult.getValue().getConfigurable() == null) {
            return;
        }

        fetchResult.getValue().getConfigurable().getRegisteredProperties().forEach(property ->
                addProperty(fetchResult, object, property));
    }

    private static <T extends Configured<T, ?>, P> void addProperty(final ObjectFetchResult<T> fetchResult, final JsonObject propertiesObject, final ConfigurationProperty<P> configurationProperty) {
        final ObjectFetchResult<P> propertyValueFetchResult = configurationProperty.deserialise(propertiesObject);

        if (propertyValueFetchResult == null) {
            return;
        }

        propertyValueFetchResult.ifSuccessful(value -> fetchResult.getValue().with(configurationProperty, value))
                .otherwise(fetchResult::addWarning);
    }

}
