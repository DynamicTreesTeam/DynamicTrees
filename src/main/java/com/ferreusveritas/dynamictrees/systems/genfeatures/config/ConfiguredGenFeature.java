package com.ferreusveritas.dynamictrees.systems.genfeatures.config;

import com.ferreusveritas.dynamictrees.systems.genfeatures.GenFeature;
import com.ferreusveritas.dynamictrees.systems.genfeatures.GenFeatures;
import com.ferreusveritas.dynamictrees.trees.Species;
import com.ferreusveritas.dynamictrees.util.json.JsonObjectGetters;
import com.google.common.collect.Maps;
import net.minecraft.crash.CrashReport;
import net.minecraft.crash.ReportedException;

import java.util.Map;

/**
 * A configured version of a {@link GenFeature}. This is used for holding {@link GenFeatureProperty}
 * objects so that {@link GenFeature} objects can be customised by different {@link Species}.
 *
 * @author Harley O'Connor
 */
public class ConfiguredGenFeature<T extends GenFeature> {

    /** A null configured gen feature. Mainly used for getting the class with the
     * {@link GenFeature} parameter for {@link JsonObjectGetters#CONFIGURED_GEN_FEATURE_GETTER}. */
    public static final ConfiguredGenFeature<GenFeature> NULL_CONFIGURED_FEATURE = new ConfiguredGenFeature<>(GenFeatures.NULL);

    private final T genFeature;
    private final Map<GenFeatureProperty<?>, GenFeaturePropertyValue<?>> properties = Maps.newHashMap();

    public ConfiguredGenFeature(T genFeature) {
        this.genFeature = genFeature;
    }

    /**
     * Adds the given {@link GenFeatureProperty} to this {@link ConfiguredGenFeature} object's
     * properties.
     *
     * @param genFeatureProperty The {@link GenFeatureProperty} to set.
     * @param value The value to register.
     * @param <V> The type of value to register.
     * @return This {@link ConfiguredGenFeature} after adding the property.
     * @throws ReportedException if the property given is not registered to the {@link GenFeature}.
     */
    public <V> ConfiguredGenFeature<T> with (GenFeatureProperty<V> genFeatureProperty, V value) {
        if (!this.genFeature.isPropertyRegistered(genFeatureProperty)) {
            CrashReport crashReport = CrashReport.makeCrashReport(new IllegalArgumentException(), "Tried to add unregistered property with identifier '" + genFeatureProperty.getIdentifier() + "' and type '" + genFeatureProperty.getType() + "' to gen feature '" + this.genFeature.getRegistryName() + "'.");
            crashReport.makeCategory("Adding property to a gen feature.");
            throw new ReportedException(crashReport);
        }

        this.properties.put(genFeatureProperty, new GenFeaturePropertyValue<>(value));
        return this;
    }

    /**
     * Checks if the properties contains the given {@link GenFeatureProperty}.
     *
     * @param genFeatureProperty The {@link GenFeatureProperty} to check for.
     * @return True if it does, false if not.
     */
    public boolean has (GenFeatureProperty<?> genFeatureProperty) {
        return this.properties.containsKey(genFeatureProperty);
    }

    /**
     * Gets the {@link GenFeaturePropertyValue} object's value for the given {@link GenFeatureProperty}.
     * This method expects that the feature will be set, so call <tt>has</tt> first if it is optional.
     *
     * @param genFeatureProperty The {@link GenFeatureProperty} to get.
     * @param <V> The type of the property's value.
     * @return The property's value.
     * @throws ReportedException if the property did not exist. If a property is optional, <tt>has</tt>
     * must be called before this method.
     */
    public <V> V get (GenFeatureProperty<V> genFeatureProperty) {
        if (!this.has(genFeatureProperty)) {
            CrashReport crashReport = CrashReport.makeCrashReport(new IllegalStateException(), "Tried to obtain gen feature property '" + genFeatureProperty.getIdentifier() + "' from '" + genFeature.getRegistryName() + "' that did not exist.");
            crashReport.makeCategory("Getting property from a configured gen feature.");
            throw new ReportedException(crashReport);
        }

        return genFeatureProperty.getType().cast(this.properties.get(genFeatureProperty).getValue());
    }

    public T getGenFeature() {
        return genFeature;
    }

    /**
     * Copies all properties from one {@link ConfiguredGenFeature} to another. This is mainly
     * used for making sure the default configuration isn't changed.
     *
     * @param configuredGenFeature The {@link ConfiguredGenFeature} to copy.
     * @param <V> The {@link GenFeature} type.
     * @return The duplicate {@link ConfiguredGenFeature}.
     */
    public static <V extends GenFeature> ConfiguredGenFeature<V> copyOf (ConfiguredGenFeature<V> configuredGenFeature) {
        ConfiguredGenFeature<V> duplicateGenFeature = new ConfiguredGenFeature<>(configuredGenFeature.genFeature);
        duplicateGenFeature.properties.putAll(configuredGenFeature.properties);
        return duplicateGenFeature;
    }

    @Override
    public String toString() {
        return "ConfiguredGenFeature{" +
                "genFeature=" + genFeature +
                ", properties=" + properties +
                '}';
    }

}
