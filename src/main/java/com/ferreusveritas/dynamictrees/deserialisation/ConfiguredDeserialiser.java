package com.ferreusveritas.dynamictrees.deserialisation;

import com.ferreusveritas.dynamictrees.api.configurations.Configurable;
import com.ferreusveritas.dynamictrees.api.configurations.ConfigurationProperty;
import com.ferreusveritas.dynamictrees.api.configurations.Configured;
import com.ferreusveritas.dynamictrees.api.registry.ConfigurableRegistryEntry;
import com.ferreusveritas.dynamictrees.deserialisation.result.JsonResult;
import com.ferreusveritas.dynamictrees.deserialisation.result.Result;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.util.function.Consumer;

/**
 * @author Harley O'Connor
 */
public final class ConfiguredDeserialiser<T extends Configured<T, C>, C extends Configurable> implements JsonDeserialiser<T> {

    private final String configurableName;
    private final Class<C> configurableClass;
    private final C nullValue;

    public ConfiguredDeserialiser(String configurableName, Class<C> configurableClass, C nullValue) {
        this.configurableName = configurableName;
        this.configurableClass = configurableClass;
        this.nullValue = nullValue;
    }

    @SuppressWarnings("unchecked")
    @Override
    public Result<T, JsonElement> deserialise(final JsonElement jsonElement) {
        return JsonResult.forInput(jsonElement)
                .mapIfType(this.configurableClass, configurable -> (T) configurable.getDefaultConfiguration())
                .elseMapIfType(JsonObject.class, (object, warningConsumer) -> {
                    final C configurable = JsonHelper.getOrDefault(object, "name", this.configurableClass,
                            this.nullValue);
                    final JsonObject properties = JsonHelper.getOrDefault(object, "properties",
                            JsonObject.class, new JsonObject());
                    final T configured = (T) configurable.getDefaultConfiguration();

                    configurable.getRegisteredProperties().forEach(property ->
                            this.addProperty(configured, property, properties, warningConsumer));

                    return configured;
                }).elseError(
                        config -> config != null && (config.getConfigurable() instanceof ConfigurableRegistryEntry &&
                                ((ConfigurableRegistryEntry<?, ?>) config.getConfigurable()).isValid()),
                        this.configurableName + " couldn't be found from input \"{}\"."
                );
    }

    private <V> void addProperty(T configured, ConfigurationProperty<V> property, JsonObject properties,
                                 Consumer<String> warningConsumer) {
        property.deserialise(properties).map(result ->
                result.ifSuccessOrElse(
                        value -> configured.with(property, value),
                        warningConsumer,
                        warningConsumer
                )
        );
    }

}
