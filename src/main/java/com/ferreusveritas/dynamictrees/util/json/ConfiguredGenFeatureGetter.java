package com.ferreusveritas.dynamictrees.util.json;

import com.ferreusveritas.dynamictrees.init.DTTrees;
import com.ferreusveritas.dynamictrees.systems.genfeatures.GenFeature;
import com.ferreusveritas.dynamictrees.systems.genfeatures.config.ConfiguredGenFeature;
import com.ferreusveritas.dynamictrees.systems.genfeatures.config.GenFeatureProperty;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

/**
 * @author Harley O'Connor
 */
public final class ConfiguredGenFeatureGetter implements IJsonObjectGetter<ConfiguredGenFeature<GenFeature>> {

    @Override
    public ObjectFetchResult<ConfiguredGenFeature<GenFeature>> get(final JsonElement jsonElement) {
        final ObjectFetchResult<ConfiguredGenFeature<GenFeature>> configuredGenFeature = new ObjectFetchResult<>();

        // TODO: Check error logging here.
        JsonHelper.JsonElementReader.of(jsonElement).ifOfType(GenFeature.class, genFeature -> configuredGenFeature.setValue(genFeature.getDefaultConfiguration()))
                .ifFailed(configuredGenFeature::setErrorMessage)
                .elseIfOfType(JsonObject.class, jsonObject ->
                    JsonHelper.JsonObjectReader.of(jsonObject).ifContains("name", GenFeature.class, genFeature -> configuredGenFeature.setValue(genFeature.getDefaultConfiguration()))
                            .ifContains("properties", JsonObject.class, propertiesObject -> {
                                if (configuredGenFeature.getValue() == ConfiguredGenFeature.NULL_CONFIGURED_FEATURE || configuredGenFeature.getValue().getGenFeature() == null)
                                    return;

                                configuredGenFeature.getValue().getGenFeature().getRegisteredProperties().forEach(genFeatureProperty ->
                                        addProperty(configuredGenFeature, propertiesObject, genFeatureProperty));
                            })
                );

        if (configuredGenFeature.getValue().getGenFeature() == null || configuredGenFeature.getValue().getGenFeature().getRegistryName().equals(DTTrees.NULL))
            return ObjectFetchResult.failure("Configured feature couldn't be found from name or wasn't given a name.");

        return configuredGenFeature;
    }

    private static <T> void addProperty(final ObjectFetchResult<ConfiguredGenFeature<GenFeature>> fetchResult, final JsonObject propertiesObject, final GenFeatureProperty<T> genFeatureProperty) {
        final ObjectFetchResult<T> propertyValueFetchResult = genFeatureProperty.getValueFromJsonObject(propertiesObject);

        if (propertyValueFetchResult == null)
            return;

        propertyValueFetchResult.ifSuccessful(value -> fetchResult.getValue().with(genFeatureProperty, value)).otherwise(fetchResult::addWarning);
    }

}
