package com.ferreusveritas.dynamictrees.systems.genfeatures;

import com.ferreusveritas.dynamictrees.api.IFullGenFeature;
import com.ferreusveritas.dynamictrees.api.IPostGenFeature;
import com.ferreusveritas.dynamictrees.api.IPostGrowFeature;
import com.ferreusveritas.dynamictrees.api.IPreGenFeature;
import com.ferreusveritas.dynamictrees.systems.genfeatures.config.GenFeaturePropertyValue;
import com.ferreusveritas.dynamictrees.systems.genfeatures.config.ConfiguredGenFeature;
import com.ferreusveritas.dynamictrees.systems.genfeatures.config.GenFeatureProperty;
import com.ferreusveritas.dynamictrees.util.CanGrowPredicate;
import com.google.common.collect.Sets;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.registries.ForgeRegistryEntry;
import net.minecraftforge.registries.IForgeRegistry;

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
public abstract class GenFeature extends ForgeRegistryEntry<GenFeature> {

    // Common properties.
    public static final GenFeatureProperty<Float> VERTICAL_SPREAD = GenFeatureProperty.createFloatProperty("vertical_spread");
    public static final GenFeatureProperty<Integer> QUANTITY = GenFeatureProperty.createIntegerProperty("quantity");
    public static final GenFeatureProperty<Float> RAY_DISTANCE = GenFeatureProperty.createFloatProperty("ray_distance");
    public static final GenFeatureProperty<Integer> MAX_HEIGHT = GenFeatureProperty.createIntegerProperty("max_height");
    public static final GenFeatureProperty<CanGrowPredicate> CAN_GROW_PREDICATE = GenFeatureProperty.createProperty("can_grow_predicate", CanGrowPredicate.class);

    /** The registry. This is used for registering and querying {@link GenFeature} objects. */
    public static IForgeRegistry<GenFeature> REGISTRY;

    private final ConfiguredGenFeature<?> defaultConfiguration;

    /** A set of properties that can be used by this {@link GenFeature}. */
    private final Set<GenFeatureProperty<?>> registeredProperties = Sets.newHashSet();

    public GenFeature(ResourceLocation registryName, GenFeatureProperty<?>... properties) {
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
    protected ConfiguredGenFeature<?> createDefaultConfiguration () {
        return new ConfiguredGenFeature<>(this);
    }

    /**
     * Registers the given properties. This should only be called from this class's constructor
     * with the properties given as it is needed for creating the default configuration.
     *
     * @param properties The {@link GenFeatureProperty} to register.
     */
    private void register (GenFeatureProperty<?>... properties) {
        this.registeredProperties.addAll(Arrays.asList(properties));
    }

    /**
     * Checks if the given {@link GenFeatureProperty} is registered (and so can be set to)
     * this {@link GenFeature}.
     *
     * @param property The {@link GenFeatureProperty} to check for.
     * @return True if it is registered, false if not.
     */
    public boolean isPropertyRegistered(GenFeatureProperty<?> property) {
        return this.registeredProperties.contains(property);
    }

    /**
     * Gets the default configuration. For sub-classes, do not use this to add default
     * properties, use <tt>createDefaultConfiguration</tt>. This returns a copy of the
     * default configuration, so any default properties will not be set.
     *
     * @return A copy of the default configuration.
     */
    public ConfiguredGenFeature<?> getDefaultConfiguration() {
        return ConfiguredGenFeature.copyOf(this.defaultConfiguration);
    }

    /**
     * Utility method that gets the default configuration and adds the value, so that a chain
     * of configurations can be added without calling <tt>getDefaultConfiguration</tt>.
     *
     * @param featureProperty The {@link GenFeatureProperty} to add.
     * @param value The {@link GenFeaturePropertyValue} to set.
     * @param <V> The type of the value being added.
     * @return The {@link ConfiguredGenFeature} object created.
     */
    public <V> ConfiguredGenFeature<?> with (GenFeatureProperty<V> featureProperty, V value) {
        return this.getDefaultConfiguration().with(featureProperty, value);
    }

}
