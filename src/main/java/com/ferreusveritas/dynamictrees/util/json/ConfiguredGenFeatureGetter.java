package com.ferreusveritas.dynamictrees.util.json;

import com.ferreusveritas.dynamictrees.init.DTTrees;
import com.ferreusveritas.dynamictrees.systems.genfeatures.GenFeature;
import com.ferreusveritas.dynamictrees.systems.genfeatures.config.ConfiguredGenFeature;
import com.ferreusveritas.dynamictrees.systems.genfeatures.config.GenFeatureProperty;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.apache.logging.log4j.LogManager;

/**
 * @author Harley O'Connor
 */
public final class ConfiguredGenFeatureGetter implements IJsonObjectGetter<ConfiguredGenFeature<GenFeature>> {

    private static final class ConfiguredGenFeatureHolder {
        private ConfiguredGenFeature<GenFeature> configuredGenFeature;

        private ConfiguredGenFeatureHolder(ConfiguredGenFeature<GenFeature> configuredGenFeature) {
            this.configuredGenFeature = configuredGenFeature;
        }
    }

    @SuppressWarnings("unchecked")
    private static final JsonPropertyApplierList<ConfiguredGenFeatureHolder> APPLIERS = new JsonPropertyApplierList<>(ConfiguredGenFeatureHolder.class)
            .register("name", GenFeature.class,
                    (configuredGenFeatureHolder, genFeature) -> configuredGenFeatureHolder.configuredGenFeature = (ConfiguredGenFeature<GenFeature>) genFeature.getDefaultConfiguration())
            .register("properties", JsonObject.class,
                    (configuredGenFeatureHolder, jsonObject) -> {
                        final ConfiguredGenFeature<GenFeature> configuredGenFeature = configuredGenFeatureHolder.configuredGenFeature;
                        configuredGenFeature.getGenFeature().getRegisteredProperties().forEach(genFeatureProperty ->
                                addProperty(configuredGenFeature, jsonObject, genFeatureProperty));
                    });

    @SuppressWarnings("unchecked")
    @Override
    public ObjectFetchResult<ConfiguredGenFeature<GenFeature>> get(final JsonElement jsonElement) {
        final ObjectFetchResult<JsonObject> jsonObjectFetchResult = JsonObjectGetters.JSON_OBJECT_GETTER.get(jsonElement);

        final ConfiguredGenFeatureHolder configuredGenFeatureHolder = new ConfiguredGenFeatureHolder(ConfiguredGenFeature.NULL_CONFIGURED_FEATURE);

        if (!jsonObjectFetchResult.wasSuccessful()) {
            final ObjectFetchResult<GenFeature> fetchResult = JsonObjectGetters.GEN_FEATURE_GETTER.get(jsonElement);

            if (!fetchResult.wasSuccessful())
                return ObjectFetchResult.failureFromOther(fetchResult);

            configuredGenFeatureHolder.configuredGenFeature = (ConfiguredGenFeature<GenFeature>) fetchResult.getValue().getDefaultConfiguration();
        } else {
            APPLIERS.applyAll(jsonObjectFetchResult.getValue(), configuredGenFeatureHolder);
        }

        if (configuredGenFeatureHolder.configuredGenFeature.getGenFeature().getRegistryName().equals(DTTrees.NULL))
            return ObjectFetchResult.failure("Configured feature couldn't be found from name or wasn't given a name.");

        return ObjectFetchResult.success(configuredGenFeatureHolder.configuredGenFeature);
    }

    private static <T> void addProperty(final ConfiguredGenFeature<?> configuredGenFeature, final JsonObject propertiesObject, final GenFeatureProperty<T> genFeatureProperty) {
        final ObjectFetchResult<T> propertyValueFetchResult = genFeatureProperty.getValueFromJsonObject(propertiesObject);

        if (propertyValueFetchResult == null)
            return;

        if (!propertyValueFetchResult.wasSuccessful()) {
            LogManager.getLogger().warn("Error whilst getting gen feature property: {}", propertyValueFetchResult.getErrorMessage());
            return;
        }

        configuredGenFeature.with(genFeatureProperty, propertyValueFetchResult.getValue());
    }

}
