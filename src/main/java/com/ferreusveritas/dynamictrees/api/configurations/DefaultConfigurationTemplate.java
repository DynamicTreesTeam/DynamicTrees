package com.ferreusveritas.dynamictrees.api.configurations;

import com.ferreusveritas.dynamictrees.deserialisation.result.JsonResult;
import com.ferreusveritas.dynamictrees.deserialisation.result.Result;
import com.google.gson.JsonNull;

import java.util.Collections;

/**
 * @author Harley O'Connor
 */
public final class DefaultConfigurationTemplate<C extends Configuration<C, ?>> implements ConfigurationTemplate<C> {

    private final C defaultConfiguration;
    private final Iterable<ConfigurationProperty<?>> registeredProperties;

    public DefaultConfigurationTemplate(C defaultConfiguration, Configurable configurable) {
        this.defaultConfiguration = defaultConfiguration;
        this.registeredProperties = Collections.unmodifiableSet(configurable.getRegisteredProperties());
    }

    @Override
    public Result<C, ?> apply(PropertiesAccessor properties) {
        return JsonResult.success(JsonNull.INSTANCE, this.defaultConfiguration.copy().withAll(properties));
    }

    @Override
    public Iterable<ConfigurationProperty<?>> getRegisteredProperties() {
        return this.registeredProperties;
    }

}
