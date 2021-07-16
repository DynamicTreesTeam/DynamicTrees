package com.ferreusveritas.dynamictrees.blocks.rootyblocks;

import com.ferreusveritas.dynamictrees.api.configurations.ConfigurationProperty;
import com.ferreusveritas.dynamictrees.api.configurations.ConfigurationPropertyValue;
import com.ferreusveritas.dynamictrees.api.configurations.Configured;

import java.util.Map;

public final class ConfiguredSoilProperties<SP extends SoilProperties> extends Configured<ConfiguredSoilProperties<SP>, SP> {

    public static final ConfiguredSoilProperties<SoilProperties> NULL_CONFIGURED_SOIL_PROPERTIES = new ConfiguredSoilProperties<>(SoilProperties.NULL_SOIL_PROPERTIES);

    @SuppressWarnings("unchecked")
    public static final Class<ConfiguredSoilProperties<SoilProperties>> NULL_CONFIGURED_SOIL_PROPERTIES_CLASS = (Class<ConfiguredSoilProperties<SoilProperties>>) NULL_CONFIGURED_SOIL_PROPERTIES.getClass();

    public ConfiguredSoilProperties(SP soilProperties) {
        super(soilProperties);
    }

    /**
     * {@inheritDoc}
     *
     * @return The copy of this {@link ConfiguredSoilProperties}.
     */
    @Override
    public ConfiguredSoilProperties<SP> copy() {
        final ConfiguredSoilProperties<SP> duplicateProperty = new ConfiguredSoilProperties<>(this.configurable);
        duplicateProperty.properties.putAll(this.properties);
        return duplicateProperty;
    }

    @SuppressWarnings("unchecked")
    public <V> ConfiguredSoilProperties<SP> withAll (ConfiguredSoilProperties<SP> from){
        for (Map.Entry<ConfigurationProperty<?>, ConfigurationPropertyValue<?>> entry : from.properties.entrySet())
            this.with((ConfigurationProperty<V>) entry.getKey(), ((ConfigurationPropertyValue<V>)entry.getValue()).getValue());
        return this;
    }

    public ConfiguredSoilProperties<SP> setSoilFlags(Integer adjFlag){
        configurable.setSoilFlags(adjFlag);
        return this;
    }

    public ConfiguredSoilProperties<SP> addSoilFlags(Integer adjFlag){
        configurable.addSoilFlags(adjFlag);
        return this;
    }

}