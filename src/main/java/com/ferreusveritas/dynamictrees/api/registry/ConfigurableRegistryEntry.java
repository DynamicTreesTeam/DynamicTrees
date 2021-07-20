package com.ferreusveritas.dynamictrees.api.registry;

import com.ferreusveritas.dynamictrees.api.configurations.Configurable;
import com.ferreusveritas.dynamictrees.api.configurations.ConfigurationProperty;
import com.ferreusveritas.dynamictrees.api.configurations.ConfigurationPropertyValue;
import com.ferreusveritas.dynamictrees.api.configurations.Configured;
import com.ferreusveritas.dynamictrees.systems.genfeatures.GenFeature;
import com.google.common.collect.Sets;
import net.minecraft.util.ResourceLocation;

import java.util.Arrays;
import java.util.Collections;
import java.util.Set;

/**
 * @author Harley O'Connor
 */
public abstract class ConfigurableRegistryEntry<T extends ConfigurableRegistryEntry<T, C>, C extends Configured<C, T>> extends RegistryEntry<T> implements Configurable {

    protected final C defaultConfiguration;

    /** A set of properties that can be used by this {@link GenFeature}. */
    private final Set<ConfigurationProperty<?>> properties = Sets.newHashSet();

    protected ConfigurableRegistryEntry() {
        this.defaultConfiguration = this.createDefaultConfiguration();
    }

    protected ConfigurableRegistryEntry(ResourceLocation registryName) {
        super(registryName);
        this.registerProperties();

        this.defaultConfiguration = this.createDefaultConfiguration();
    }

    /**
     * Creates the default configuration for this {@link ConfigurableRegistryEntry}. Sub-classes
     * should override this to add their default property values.
     *
     * @return The default configured {@link ConfigurableRegistryEntry}.
     */
    protected abstract C createDefaultConfiguration ();

    /**
     * Sub-classes should override this to register their properties.
     */
    protected abstract void registerProperties();

    /**
     * Registers the given properties. For the majority of use cases, this should be used by
     * {@link #registerProperties()}. Registering properties after this is not recommended.
     *
     * @param properties The {@link ConfigurationProperty} to register.
     */
    @SuppressWarnings("unchecked")
    protected final T register (final ConfigurationProperty<?>... properties) {
        this.properties.addAll(Arrays.asList(properties));
        return (T) this;
    }

    /**
     * {@inheritDoc}
     *
     * @param property The {@link ConfigurationProperty} to check for.
     * @return {@code true} if it is registered, {@code false} if not.
     */
    @Override
    public boolean isPropertyRegistered(ConfigurationProperty<?> property) {
        return this.properties.contains(property);
    }

    /**
     * Gets an unmodifiable view of the {@link Set} of {@link ConfigurationProperty} objects
     * registered to this {@link ConfigurableRegistryEntry}.
     *
     * @return The {@link Set} of {@link ConfigurationProperty} for this
     *         {@link ConfigurableRegistryEntry}.
     */
    @Override
    public Set<ConfigurationProperty<?>> getRegisteredProperties() {
        return Collections.unmodifiableSet(this.properties);
    }

    /**
     * Returns the {@link #defaultConfiguration}. Generally, this will return a <b>copy</b>
     * of the default config so that it can be configured without impacting the default
     * configuration.
     *
     * @return The {@link #defaultConfiguration}.
     */
    @Override
    public C getDefaultConfiguration() {
        return this.defaultConfiguration.copy();
    }

    /**
     * Gets the default configuration, adding the specified {@code property} with the specified
     * {@code value}.
     *
     * @param property The {@link ConfigurationProperty} to add.
     * @param value The {@link ConfigurationPropertyValue} to set.
     * @param <V> The type of the value being added.
     * @return The {@link Configured} object created.
     */
    public <V> C with (final ConfigurationProperty<V> property, final V value) {
        return this.getDefaultConfiguration().with(property, value);
    }

}
