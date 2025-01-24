package com.dtteam.dynamictrees.deserialization.deserializer;

import com.dtteam.dynamictrees.DynamicTrees;
import com.dtteam.dynamictrees.api.configuration.*;
import com.dtteam.dynamictrees.deserialization.DeserializationException;
import com.dtteam.dynamictrees.deserialization.JsonDeserializers;
import com.dtteam.dynamictrees.deserialization.JsonHelper;
import com.dtteam.dynamictrees.deserialization.result.JsonResult;
import com.dtteam.dynamictrees.deserialization.result.Result;
import com.dtteam.dynamictrees.utility.helper.ResourceLocationUtils;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.resources.ResourceLocation;

import org.jetbrains.annotations.Nullable;
import java.util.function.Consumer;
import java.util.stream.StreamSupport;

/**
 * @author Harley O'Connor
 */
public final class ConfiguredDeserializer<T extends Configuration<T, C>, C extends Configurable> implements JsonDeserializer<T> {

    private final String configurableName;
    private final Class<C> configurableClass;
    private final TemplateRegistry<T> templates;

    public ConfiguredDeserializer(String configurableName, Class<C> configurableClass,
                                  TemplateRegistry<T> templates) {
        this.configurableName = configurableName;
        this.configurableClass = configurableClass;
        this.templates = templates;
    }

    @SuppressWarnings("unchecked")
    @Override
    public Result<T, JsonElement> deserialize(final JsonElement jsonElement) {
        return JsonResult.forInput(jsonElement)
                .mapIfType(String.class, (name, warningConsumer) -> {
                    final ConfigurationTemplate<T> template = getTemplate(
                            ResourceLocationUtils.parse(name, DynamicTrees.MOD_ID)
                    );
                    return template.apply(Properties.NONE).orElseThrow();
                })
                .elseMapIfType(this.configurableClass, configurable -> (T) configurable.getDefaultConfiguration())
                .elseMapIfType(JsonObject.class, (object, warningConsumer) -> {
                    final ConfigurationTemplate<T> template = getTemplate(this.getTemplateName(object));
                    final JsonObject propertiesJson = JsonHelper.getOrDefault(object, "properties",
                            JsonObject.class, new JsonObject());
                    final Properties properties = new Properties();

                    StreamSupport.stream(template.getRegisteredProperties().spliterator(), false)
                            .forEach(property ->
                                    this.addProperty(properties, property, propertiesJson, warningConsumer)
                            );

                    return template.apply(properties).orElseThrow();
                }).elseError(
                        this::isConfigurationValid,
                        this.configurableName + " couldn't be found from input \"{}\"."
                );
    }

    private boolean isConfigurationValid(@Nullable T config) {
        return config != null && (config.getConfigurable() instanceof ConfigurableRegistryEntry<?,?> &&
                ((ConfigurableRegistryEntry<?, ?>) config.getConfigurable()).isValid());
    }

    private ConfigurationTemplate<T> getTemplate(ResourceLocation templateName) throws DeserializationException {
        return this.templates.get(templateName)
                .orElseThrow(() -> new DeserializationException("No such template \"" + templateName + "\" for \"" + configurableName + "\"."));
    }

    private ResourceLocation getTemplateName(JsonObject json) throws DeserializationException {
        return JsonHelper.getAsOptional(json, "name", JsonDeserializers.DT_RESOURCE_LOCATION)
                .orElseThrow(() -> new DeserializationException("Configurable must state name of template to use."));
    }

    private <V> void addProperty(Properties properties, ConfigurationProperty<V> property, JsonObject propertiesJson,
                                 Consumer<String> warningConsumer) {
        property.deserialise(propertiesJson).map(result ->
                result.ifSuccessOrElse(
                        value -> properties.put(property, value),
                        warningConsumer,
                        warningConsumer
                )
        );
    }

}
