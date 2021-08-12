package com.ferreusveritas.dynamictrees.systems.genfeatures;

import com.ferreusveritas.dynamictrees.api.configurations.ConfigurationProperty;
import com.ferreusveritas.dynamictrees.api.configurations.Configured;
import com.ferreusveritas.dynamictrees.deserialisation.JsonDeserialisers;
import com.ferreusveritas.dynamictrees.trees.Species;

/**
 * A configured version of a {@link GenFeature}. This is used for holding {@link ConfigurationProperty} objects so that
 * {@link GenFeature} objects can be customised by different {@link Species}.
 *
 * @author Harley O'Connor
 */
public class ConfiguredGenFeature extends Configured<ConfiguredGenFeature, GenFeature> {

    /**
     * A null configured gen feature. Mainly used for getting the class with the {@link GenFeature} parameter for {@link
     * JsonDeserialisers#CONFIGURED_GEN_FEATURE}.
     */
    public static final ConfiguredGenFeature NULL = new ConfiguredGenFeature(GenFeature.NULL_GEN_FEATURE);

    public ConfiguredGenFeature(GenFeature genFeature) {
        super(genFeature);
    }

    public GenFeature getGenFeature() {
        return this.configurable;
    }

    /**
     * {@inheritDoc}
     *
     * @return The copy of this {@link ConfiguredGenFeature}.
     */
    @Override
    public ConfiguredGenFeature copy() {
        final ConfiguredGenFeature duplicateGenFeature = new ConfiguredGenFeature(this.configurable);
        duplicateGenFeature.properties.putAll(this.properties);
        return duplicateGenFeature;
    }

}
