package com.ferreusveritas.dynamictrees.api.configurations;

import com.ferreusveritas.dynamictrees.api.registry.RegistryEntry;
import com.ferreusveritas.dynamictrees.util.Null;
import net.minecraft.crash.CrashReport;
import net.minecraft.crash.ReportedException;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Optional;
import java.util.function.Predicate;

/**
 * A configured version of a {@link Configurable}. This is used for holding {@link ConfigurationProperty} objects so
 * that registered {@link Configurable}s can have custom configurations.
 *
 * @author Harley O'Connor
 */
public abstract class Configuration<T extends Configuration<T, C>, C extends Configurable>
        extends RegistryEntry<T> {

    protected final C configurable;
    protected final Properties properties = new Properties();

    public Configuration(C configurable) {
        this.configurable = configurable;
    }

    /**
     * Adds the given {@link ConfigurationProperty} to this {@link Configuration} object's properties.
     *
     * @param property The {@link ConfigurationProperty} to set.
     * @param value    The value to register.
     * @param <V>      The type of value to register.
     * @return This {@link Configuration} after adding the property.
     * @throws ReportedException If the property given is not registered to the {@link Configurable}.
     */
    @SuppressWarnings("unchecked")
    public <V> T with(ConfigurationProperty<V> property, V value) {
        if (!this.configurable.isPropertyRegistered(property)) {
            final CrashReport crashReport = CrashReport.forThrowable(new IllegalArgumentException(), "Tried to add " +
                    "unregistered property with identifier '" + property.getKey() + "' and type '" +
                    property.getType() + "' configurable '" + this.configurable + "'.");
            crashReport.addCategory("Adding property to a gen feature.");
            throw new ReportedException(crashReport);
        }

        this.properties.put(property, value);
        return (T) this;
    }

    @SuppressWarnings("unchecked")
    public T withAll(PropertiesAccessor properties) {
        properties.forEach(this::with);
        return (T) this;
    }

    /**
     * Checks if the properties contains the given {@link ConfigurationProperty}.
     *
     * @param property The {@link ConfigurationProperty} to check for.
     * @return {@code true} if it does, {@code false} if not.
     */
    public boolean has(ConfigurationProperty<?> property) {
        return this.properties.has(property);
    }

    /**
     * Gets the value for the given {@link ConfigurationProperty}. This
     * method expects that the feature will be set, so call {@link #getAsOptional(ConfigurationProperty)} instead if it is
     * optional.
     *
     * @param property The {@link ConfigurationProperty} to get.
     * @param <V>      The type of the property's value.
     * @return The property's value.
     * @throws ReportedException If the property is null. If a property is optional. {@link
     *                           #getAsOptional(ConfigurationProperty)} should be called instead.
     */
    @Nonnull
    public <V> V get(ConfigurationProperty<V> property) {
        Optional<V> optionalProperty = getAsOptional(property);

        if (optionalProperty.isPresent())
            return optionalProperty.get();
         else {
            final CrashReport crashReport = CrashReport.forThrowable(new IllegalStateException(),
                    "Property '" + property.getKey() + "' from '" + this.configurable + "' is Null.");
            crashReport.addCategory("Getting property from a configuration");
            throw new ReportedException(crashReport);
        }

    }

    /**
     *
     * @param property The {@link ConfigurationProperty} to get.
     * @param <V>      The type of the property's value.
     * @return An Optional with the property's value.
     * @throws ReportedException If the property did not exist.
     */
    public <V> Optional<V> getAsOptional(ConfigurationProperty<V> property) {
        if (!this.has(property)) {
            final CrashReport crashReport = CrashReport.forThrowable(new IllegalStateException(), "Tried to obtain " +
                    "property '" + property.getKey() + "' from '" + this.configurable + "' that did not exist.");
            crashReport.addCategory("Getting property from a configuration");
            throw new ReportedException(crashReport);
        }

        return Optional.ofNullable(this.properties.get(property));
    }

    /**
     * If property is null, invalidResult will be returned instead
     *
     * @param property The {@link ConfigurationProperty} to get.
     * @param validator A predicate that can be used to validate the value of the Property. If the
     *                  predicate fails, the invalidDefault will be returned instead
     * @param invalidDefault The value that will be returned if the validator fails
     * @param <V>      The type of the property's value.
     * @return  The property's value or the invalidDefault if the validator failed.
     */
    public <V> V getOrInvalidDefault(ConfigurationProperty<V> property, Predicate<V> validator, V invalidDefault) {
        return this.getAsOptional(property).filter(validator).orElse(invalidDefault);
    }

    /**
     * Makes a copy of this {@link Configurable}, copying the {@link #configurable} reference and all {@link
     * #properties} from this {@link Configurable}.
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
