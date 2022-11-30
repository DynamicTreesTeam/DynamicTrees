package com.ferreusveritas.dynamictrees.api.configuration;

import java.util.Set;

/**
 * @author Harley O'Connor
 */
public interface Configurable {

    /**
     * Checks if the given {@link ConfigurationProperty} is registered (and so can be set to) this {@link
     * Configurable}.
     *
     * @param property The {@link ConfigurationProperty} to check for.
     * @return {@code true} if it is registered, {@code false} if not.
     */
    boolean isPropertyRegistered(ConfigurationProperty<?> property);

    Set<ConfigurationProperty<?>> getRegisteredProperties();

    Configuration<?, ?> getDefaultConfiguration();

}
