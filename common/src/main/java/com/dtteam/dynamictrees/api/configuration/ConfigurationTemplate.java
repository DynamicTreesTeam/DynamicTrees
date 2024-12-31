package com.dtteam.dynamictrees.api.configuration;

import com.dtteam.dynamictrees.deserialization.result.Result;

/**
 * @author Harley O'Connor
 */
public interface ConfigurationTemplate<C extends Configuration<C, ?>> {

    Result<C, ?> apply(PropertiesAccessor properties);

    Iterable<ConfigurationProperty<?>> getRegisteredProperties();

}
