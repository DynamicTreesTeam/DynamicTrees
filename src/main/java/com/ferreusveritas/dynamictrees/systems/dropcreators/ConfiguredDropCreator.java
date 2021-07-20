package com.ferreusveritas.dynamictrees.systems.dropcreators;

import com.ferreusveritas.dynamictrees.api.configurations.Configured;

/**
 * @author Harley O'Connor
 */
public final class ConfiguredDropCreator<DC extends DropCreator> extends Configured<ConfiguredDropCreator<DC>, DC> {

    public static final ConfiguredDropCreator<DropCreator> NULL_CONFIGURED_DROP_CREATOR = new ConfiguredDropCreator<>(DropCreator.NULL_DROP_CREATOR);

    @SuppressWarnings("unchecked")
    public static final Class<ConfiguredDropCreator<DropCreator>> NULL_CONFIGURED_DROP_CREATOR_CLASS = (Class<ConfiguredDropCreator<DropCreator>>) NULL_CONFIGURED_DROP_CREATOR.getClass();

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
