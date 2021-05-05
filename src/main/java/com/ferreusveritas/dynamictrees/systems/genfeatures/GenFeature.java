package com.ferreusveritas.dynamictrees.systems.genfeatures;

import com.ferreusveritas.dynamictrees.api.IFullGenFeature;
import com.ferreusveritas.dynamictrees.api.IPostGenFeature;
import com.ferreusveritas.dynamictrees.api.IPostGrowFeature;
import com.ferreusveritas.dynamictrees.api.IPreGenFeature;
import com.ferreusveritas.dynamictrees.api.configurations.Configurable;
import com.ferreusveritas.dynamictrees.api.configurations.ConfigurationProperty;
import com.ferreusveritas.dynamictrees.api.configurations.ConfigurationPropertyValue;
import com.ferreusveritas.dynamictrees.api.registry.Registry;
import com.ferreusveritas.dynamictrees.api.registry.RegistryEntry;
import com.ferreusveritas.dynamictrees.init.DTTrees;
import com.ferreusveritas.dynamictrees.systems.genfeatures.config.ConfiguredGenFeature;
import com.ferreusveritas.dynamictrees.util.CanGrowPredicate;
import com.google.common.collect.Sets;
import net.minecraft.util.ResourceLocation;

import java.util.Arrays;
import java.util.Set;

/**
 * Base class for all gen features. These are features that grow on/in/around a tree on generation,
 * or whilst growing, depending on which interface is implemented.
 *
 * <p>Sub-classes should implement at least one of the following: {@link IFullGenFeature},
 * {@link IPostGenFeature}, {@link IPostGrowFeature}, or {@link IPreGenFeature} to do their generation.</p>
 *
 * @author Harley O'Connor
 */
public abstract class GenFeature extends RegistryEntry<GenFeature> implements Configurable {

    // Common properties.
    public static final ConfigurationProperty<Float> VERTICAL_SPREAD = ConfigurationProperty.floatProperty("vertical_spread");
    public static final ConfigurationProperty<Integer> QUANTITY = ConfigurationProperty.integer("quantity");
    public static final ConfigurationProperty<Float> RAY_DISTANCE = ConfigurationProperty.floatProperty("ray_distance");
    public static final ConfigurationProperty<Integer> MAX_HEIGHT = ConfigurationProperty.integer("max_height");
    public static final ConfigurationProperty<CanGrowPredicate> CAN_GROW_PREDICATE = ConfigurationProperty.property("can_grow_predicate", CanGrowPredicate.class);
    public static final ConfigurationProperty<Integer> MAX_COUNT = ConfigurationProperty.integer("max_count");

    public static final GenFeature NULL_GEN_FEATURE = new GenFeature(DTTrees.NULL) {};

    /**
     * Central registry for all {@link GenFeature} objects.
     */
    public static final Registry<GenFeature> REGISTRY = new Registry<>(GenFeature.class, NULL_GEN_FEATURE);

    private final ConfiguredGenFeature<GenFeature> defaultConfiguration;

    /** A set of properties that can be used by this {@link GenFeature}. */
    private final Set<ConfigurationProperty<?>> registeredProperties = Sets.newHashSet();

    public GenFeature(ResourceLocation registryName, ConfigurationProperty<?>... properties) {
        this.setRegistryName(registryName);
        this.register(properties);

        this.defaultConfiguration = this.createDefaultConfiguration();
    }

    /**
     * Creates the default configuration for this {@link GenFeature}. Sub-classes should override
     * this to add their default property values.
     *
     * @return The default {@link ConfiguredGenFeature}.
     */
    protected ConfiguredGenFeature<GenFeature> createDefaultConfiguration () {
        return new ConfiguredGenFeature<>(this);
    }

    /**
     * Registers the given properties. This should only be called from this class's constructor
     * with the properties given as it is needed for creating the default configuration.
     *
     * @param properties The {@link ConfigurationProperty} to register.
     */
    private void register (ConfigurationProperty<?>... properties) {
        this.registeredProperties.addAll(Arrays.asList(properties));
    }

    /**
     * {@inheritDoc}
     *
     * @param property The {@link ConfigurationProperty} to check for.
     * @return {@code true} if it is registered, {@code false} if not.
     */
    @Override
    public boolean isPropertyRegistered(ConfigurationProperty<?> property) {
        return this.registeredProperties.contains(property);
    }

    /**
     * Gets the {@link Set} of {@link ConfigurationProperty} objects registered to this
     * {@link GenFeature}.
     *
     * @return The {@link Set} of {@link ConfigurationProperty} for this {@link GenFeature}.
     */
    public Set<ConfigurationProperty<?>> getRegisteredProperties() {
        return registeredProperties;
    }

    /**
     * Gets the default configuration. For sub-classes, do not use this to add default
     * properties, use <tt>createDefaultConfiguration</tt>. This returns a copy of the
     * default configuration, so any default properties will not be set.
     *
     * @return A copy of the default configuration.
     */
    public ConfiguredGenFeature<GenFeature> getDefaultConfiguration() {
        return ConfiguredGenFeature.copyOf(this.defaultConfiguration);
    }

    /**
     * Utility method that gets the default configuration and adds the value, so that a chain
     * of configurations can be added without calling <tt>getDefaultConfiguration</tt>.
     *
     * @param featureProperty The {@link ConfigurationProperty} to add.
     * @param value The {@link ConfigurationPropertyValue} to set.
     * @param <V> The type of the value being added.
     * @return The {@link ConfiguredGenFeature} object created.
     */
    public <V> ConfiguredGenFeature<GenFeature> with (ConfigurationProperty<V> featureProperty, V value) {
        return this.getDefaultConfiguration().with(featureProperty, value);
    }

}
