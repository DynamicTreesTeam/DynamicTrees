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
                                if (configured.getValue() == ConfiguredGenFeature.NULL_CONFIGURED_FEATURE || configured.getValue().getConfigurable() == null) {
                                    return;
                                }

                                configured.getValue().getConfigurable().getRegisteredProperties().forEach(property ->
                                        addProperty(configured, propertiesObject, property));
                            })
                );

        final C configurable = configured.getValue().getConfigurable();
        if (configurable == null || (configurable instanceof ConfigurableRegistryEntry && ((ConfigurableRegistryEntry<?, ?>) configurable).getRegistryName().equals(DTTrees.NULL))) {
            return ObjectFetchResult.failure(configurableName + " couldn't be found from name or wasn't given a name.");
        }

        return configured;
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
