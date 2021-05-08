package com.ferreusveritas.dynamictrees.systems.dropcreators;

import com.ferreusveritas.dynamictrees.api.configurations.Configured;

/**
 * @author Harley O'Connor
 */
public final class ConfiguredDropCreator<DC extends DropCreator> extends Configured<ConfiguredDropCreator<DC>, DC> {

    public ConfiguredDropCreator(DC dropCreator) {
        super(dropCreator);
    }

    /**
     * {@inheritDoc}
     *
     * @return The copy of this {@link ConfiguredDropCreator}.
     */
    @Override
    public ConfiguredDropCreator<DC> copy() {
        final ConfiguredDropCreator<DC> duplicateGenFeature = new ConfiguredDropCreator<>(this.configurable);
        duplicateGenFeature.properties.putAll(this.properties);
        return duplicateGenFeature;
    }

}
