package com.ferreusveritas.dynamictrees.deserialisation;

import com.ferreusveritas.dynamictrees.api.configurations.Configurable;
import com.ferreusveritas.dynamictrees.api.configurations.ConfigurationProperty;
import com.ferreusveritas.dynamictrees.api.configurations.Configured;
import com.ferreusveritas.dynamictrees.api.registry.ConfigurableRegistryEntry;
import com.ferreusveritas.dynamictrees.init.DTTrees;
import com.ferreusveritas.dynamictrees.systems.genfeatures.config.ConfiguredGenFeature;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

/**
 * @author Harley O'Connor
 */
public final class ConfiguredDeserialiser<T extends Configured<T, C>, C extends Configurable> implements JsonDeserialiser<T> {

    private final String configurableName;
    private final Class<C> configurableClass;

    public ConfiguredDeserialiser(String configurableName, Class<C> configurableClass) {
        this.configurableName = configurableName;
        this.configurableClass = configurableClass;
    }

    @SuppressWarnings("unchecked")
    @Override
    public DeserialisationResult<T> deserialise(final JsonElement jsonElement) {
        final DeserialisationResult<T> configured = new DeserialisationResult<>();

        // TODO: Check error logging here.
        JsonHelper.JsonElementReader.of(jsonElement).ifOfType(configurableClass, configurable -> configured.setValue((T) configurable.getDefaultConfiguration()))
                .ifFailed(configured::setErrorMessage)
                .elseIfOfType(JsonObject.class, jsonObject ->
                        JsonHelper.JsonObjectReader.of(jsonObject).ifContains("name", configurableClass, configurable -> configured.setValue((T) configurable.getDefaultConfiguration()))
                                .ifContains("properties", JsonObject.class, propertiesObject -> setProperties(configured, propertiesObject))
                );

        final T value = configured.getValue();
        if (value == null || value.getConfigurable() == null || (value.getConfigurable() instanceof ConfigurableRegistryEntry &&
                ((ConfigurableRegistryEntry<?, ?>) value.getConfigurable()).getRegistryName().equals(DTTrees.NULL))) {
            return DeserialisationResult.failure(configurableName + " couldn't be found from name or wasn't given a name.");
        }

        return configured;
    }

    public static <T extends Configured<T, ?>> void setProperties(DeserialisationResult<T> result, JsonObject object) {
        if (result.getValue() == ConfiguredGenFeature.NULL_CONFIGURED_FEATURE || result.getValue() == null || result.getValue().getConfigurable() == null) {
            return;
        }

        result.getValue().getConfigurable().getRegisteredProperties().forEach(property ->
                addProperty(result, object, property));
    }

    private static <T extends Configured<T, ?>, P> void addProperty(final DeserialisationResult<T> result, final JsonObject propertiesObject, final ConfigurationProperty<P> configurationProperty) {
        final DeserialisationResult<P> propertyValueResult = configurationProperty.deserialise(propertiesObject);

        if (propertyValueResult == null) {
            return;
        }

        propertyValueResult.ifSuccessful(value -> result.getValue().with(configurationProperty, value))
                .elseIfError(result::addWarning);
    }

}
