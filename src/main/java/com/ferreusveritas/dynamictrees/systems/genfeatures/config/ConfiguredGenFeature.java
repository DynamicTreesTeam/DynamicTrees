package com.ferreusveritas.dynamictrees.systems.genfeatures.config;

import com.ferreusveritas.dynamictrees.api.configurations.ConfigurationProperty;
import com.ferreusveritas.dynamictrees.api.configurations.Configured;
import com.ferreusveritas.dynamictrees.systems.genfeatures.GenFeature;
import com.ferreusveritas.dynamictrees.trees.Species;
import com.ferreusveritas.dynamictrees.util.json.JsonObjectGetters;

/**
 * A configured version of a {@link GenFeature}. This is used for holding {@link ConfigurationProperty}
 * objects so that {@link GenFeature} objects can be customised by different {@link Species}.
 *
 * @author Harley O'Connor
 */
public class ConfiguredGenFeature<GF extends GenFeature> extends Configured<ConfiguredGenFeature<GF>, GF> {

    /** A null configured gen feature. Mainly used for getting the class with the
     * {@link GenFeature} parameter for {@link JsonObjectGetters#CONFIGURED_GEN_FEATURE}. */
    public static final ConfiguredGenFeature<GenFeature> NULL_CONFIGURED_FEATURE = new ConfiguredGenFeature<>(GenFeature.NULL_GEN_FEATURE);

    @SuppressWarnings("unchecked")
    public static final Class<ConfiguredGenFeature<GenFeature>> NULL_CONFIGURED_FEATURE_CLASS = (Class<ConfiguredGenFeature<GenFeature>>) NULL_CONFIGURED_FEATURE.getClass();

    public ConfiguredGenFeature(GF genFeature) {
        super(genFeature);
    }

    public GF getGenFeature() {
        return this.configurable;
    }

    /**
     * Copies all properties from one {@link ConfiguredGenFeature} to another. This is mainly
     * used for making sure the default configuration isn't changed.
     *
     * @param configuredGenFeature The {@link ConfiguredGenFeature} to copy.
     * @param <GF> The {@link GenFeature} type.
     * @return The duplicate {@link ConfiguredGenFeature}.
     */
    public static <GF extends GenFeature> ConfiguredGenFeature<GF> copyOf (ConfiguredGenFeature<GF> configuredGenFeature) {
        ConfiguredGenFeature<GF> duplicateGenFeature = new ConfiguredGenFeature<>(configuredGenFeature.configurable);
        duplicateGenFeature.properties.putAll(configuredGenFeature.properties);
        return duplicateGenFeature;
    }

}
