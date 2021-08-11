package com.ferreusveritas.dynamictrees.systems.dropcreators;

import com.ferreusveritas.dynamictrees.api.configurations.Configured;

/**
 * @author Harley O'Connor
 */
public final class ConfiguredDropCreator extends Configured<ConfiguredDropCreator, DropCreator> {

    public static final ConfiguredDropCreator NULL_CONFIGURED_DROP_CREATOR = new ConfiguredDropCreator(DropCreator.NULL_DROP_CREATOR);

    @SuppressWarnings("unchecked")
    public static final Class<ConfiguredDropCreator> NULL_CONFIGURED_DROP_CREATOR_CLASS =
            (Class<ConfiguredDropCreator>) NULL_CONFIGURED_DROP_CREATOR.getClass();

    public ConfiguredDropCreator(DropCreator dropCreator) {
        super(dropCreator);
    }

    /**
     * {@inheritDoc}
     *
     * @return The copy of this {@link ConfiguredDropCreator}.
     */
    @Override
    public ConfiguredDropCreator copy() {
        final ConfiguredDropCreator duplicateGenFeature = new ConfiguredDropCreator(this.configurable);
        duplicateGenFeature.properties.putAll(this.properties);
        return duplicateGenFeature;
    }

}
