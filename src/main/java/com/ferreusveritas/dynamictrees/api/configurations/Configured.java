package com.ferreusveritas.dynamictrees.api.configurations;

import com.google.common.collect.Maps;
import net.minecraft.crash.CrashReport;
import net.minecraft.crash.ReportedException;

import java.util.Map;
import java.util.function.Predicate;

/**
 * A configured version of a {@link Configurable}. This is used for holding
 * {@link ConfigurationProperty} objects so that registered {@link Configurable}s
 * can have custom configurations.
 *
 * @author Harley O'Connor
 */
public abstract class Configured<T extends Configured<T, C>, C extends Configurable> {

    protected final C configurable;
    protected final Map<ConfigurationProperty<?>, ConfigurationPropertyValue<?>> properties = Maps.newHashMap();

    public Configured(C configurable) {
        this.configurable = configurable;
    }

    /**
     * Adds the given {@link ConfigurationProperty} to this {@link Configured} object's
     * properties.
     *
     * @param property The {@link ConfigurationProperty} to set.
     * @param value The value to register.
     * @param <V> The type of value to register.
     * @return This {@link Configured} after adding the property.
     * @throws ReportedException If the property given is not registered to the
     *                           {@link Configurable}.
     */
    @SuppressWarnings("unchecked")
    public <V> T with (ConfigurationProperty<V> property, V value) {
        if (!this.configurable.isPropertyRegistered(property)) {
            final CrashReport crashReport = CrashReport.forThrowable(new IllegalArgumentException(), "Tried to add " +
                    "unregistered property with identifier '" + property.getIdentifier() + "' and type '" +
                    property.getType() + "' configurable '" + this.configurable + "'.");
            crashReport.addCategory("Adding property to a gen feature.");
            throw new ReportedException(crashReport);
        }

        this.properties.put(property, new ConfigurationPropertyValue<>(value));
        return (T) this;
    }

    /**
     * Checks if the properties contains the given {@link ConfigurationProperty}.
     *
     * @param property The {@link ConfigurationProperty} to check for.
     * @return {@code true} if it does, {@code false} if not.
     */
    public boolean has (ConfigurationProperty<?> property) {
        return this.properties.containsKey(property);
    }

    /**
     * Gets the {@link ConfigurationPropertyValue} object's value for the given
     * {@link ConfigurationProperty}. This method expects that the feature will be set, so
     * call {@link #has(ConfigurationProperty)} first if it is optional.
     *
     * @param property The {@link ConfigurationProperty} to get.
     * @param <V> The type of the property's value.
     * @return The property's value.
     * @throws ReportedException If the property did not exist. If a property is optional.
     *                           {@link #has(ConfigurationProperty)} should be checked before
     *                           calling this.
     */
    public <V> V get (ConfigurationProperty<V> property) {
        if (!this.has(property)) {
            final CrashReport crashReport = CrashReport.forThrowable(new IllegalStateException(), "Tried to obtain " +
                    "gen feature property '" + property.getIdentifier() + "' from '" + this.configurable + "' " +
                    "that did not exist.");
            crashReport.addCategory("Getting property from a configured gen feature.");
            throw new ReportedException(crashReport);
        }

        return property.getType().cast(this.properties.get(property).getValue());
    }

    public <V> V getOrInvalidDefault(ConfigurationProperty<V> property, Predicate<V> validator, V invalidDefault) {
        final V setProperty = this.get(property);
        return validator.test(setProperty) ? setProperty : invalidDefault;
    }

    /**
     * Makes a copy of this {@link Configurable}, copying the {@link #configurable} reference
     * and all {@link #properties} from this {@link Configurable}.
     *
     * @return The copy of this {@link Configurable}.
     */
    public abstract T copy();

    public C getConfigurable() {
        return this.configurable;
    }

    @Override
    public String toString() {
        return this.getClass().getSimpleName() + "{" +
                "configurable=" + configurable +
                ", properties=" + properties +
                '}';
    }

}
