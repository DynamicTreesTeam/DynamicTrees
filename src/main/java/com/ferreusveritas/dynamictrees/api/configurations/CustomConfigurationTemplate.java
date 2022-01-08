package com.ferreusveritas.dynamictrees.api.configurations;

import com.ferreusveritas.dynamictrees.deserialisation.JsonDeserialisers;
import com.ferreusveritas.dynamictrees.deserialisation.JsonHelper;
import com.ferreusveritas.dynamictrees.deserialisation.result.Result;
import com.ferreusveritas.dynamictrees.util.CommonCollectors;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;

import java.util.List;
import java.util.stream.Stream;

/**
 * @author Harley O'Connor
 */
public final class CustomConfigurationTemplate<C extends Configuration<C, ?>> implements ConfigurationTemplate<C> {

    private final List<PropertyDefinition<?>> propertyDefinitions;
    private final String json;
    private final Class<C> configurationClass;
    private final Iterable<ConfigurationProperty<?>> registeredProperties;

    public CustomConfigurationTemplate(List<PropertyDefinition<?>> propertyDefinitions,
                                       String json, Class<C> configurationClass, Configurable configurable) {
        this.propertyDefinitions = propertyDefinitions;
        this.json = json;
        this.configurationClass = configurationClass;
        this.registeredProperties = collectAllProperties(propertyDefinitions, configurable);
    }

    private static Iterable<ConfigurationProperty<?>> collectAllProperties(
            List<PropertyDefinition<?>> propertyDefinitions, Configurable configurable
    ) {
        return Stream.concat(
            configurable.getRegisteredProperties().stream(),
            propertyDefinitions.stream().map(PropertyDefinition::getProperty)
        ).collect(CommonCollectors.toUnmodifiableSet());
    }

    @Override
    public Result<C, JsonElement> apply(PropertiesAccessor properties) {
        String json = this.processJson(properties);
        return this.deserialiseJson(json);
    }

    private Result<C, JsonElement> deserialiseJson(String json) {
        try {
            return JsonDeserialisers.getOrThrow(this.configurationClass).deserialise(
                    JsonHelper.getGson().fromJson(json, JsonObject.class)
            );
        } catch (JsonSyntaxException e) {
            throw new RuntimeException("Failed to deserialise processed Json:\n " + json, e);
        }
    }

    private String processJson(PropertiesAccessor properties) {
        String json = this.json;
        for (PropertyDefinition<?> definition : this.propertyDefinitions) {
            json = definition.process(json, properties);
        }
        return json;
    }

    @Override
    public Iterable<ConfigurationProperty<?>> getRegisteredProperties() {
        return this.registeredProperties;
    }

}
