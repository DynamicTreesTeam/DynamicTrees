package com.ferreusveritas.dynamictrees.api.configurations;

import com.ferreusveritas.dynamictrees.deserialisation.result.Result;

/**
 * @author Harley O'Connor
 */
public interface ConfigurationTemplate<C extends Configuration<C, ?>> {

    Result<C, ?> apply(PropertiesAccessor properties);

    Iterable<ConfigurationProperty<?>> getRegisteredProperties();

}
