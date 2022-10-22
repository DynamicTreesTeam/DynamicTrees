package com.ferreusveritas.dynamictrees.deserialisation;

import com.ferreusveritas.dynamictrees.DynamicTrees;
import com.ferreusveritas.dynamictrees.api.configuration.*;
import com.ferreusveritas.dynamictrees.deserialisation.result.JsonResult;
import com.ferreusveritas.dynamictrees.deserialisation.result.Result;
import com.ferreusveritas.dynamictrees.util.ResourceLocationUtils;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.resources.ResourceLocation;

import javax.annotation.Nullable;
import java.util.function.Consumer;
import java.util.stream.StreamSupport;

/**
 * @author Harley O'Connor
 */
public final class ConfiguredDeserialiser<T extends Configuration<T, C>, C extends Configurable> implements JsonDeserialiser<T> {

    private final String configurableName;
    private final Class<C> configurableClass;
    private final TemplateRegistry<T> templates;

    public ConfiguredDeserialiser(String configurableName, Class<C> configurableClass,
                                  TemplateRegistry<T> templates) {
        this.configurableName = configurableName;
        this.configurableClass = configurableClass;
        this.templates = templates;
    }

    @SuppressWarnings("unchecked")
    @Override
    public Result<T, JsonElement> deserialise(final JsonElement jsonElement) {
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
        return config != null && (config.getConfigurable() instanceof ConfigurableRegistryEntry &&
                ((ConfigurableRegistryEntry<?, ?>) config.getConfigurable()).isValid());
    }

    private ConfigurationTemplate<T> getTemplate(ResourceLocation templateName) throws DeserialisationException {
        return this.templates.get(templateName)
                .orElseThrow(() -> new DeserialisationException("No such template \"" + templateName + "\" for \"" + configurableName + "\"."));
    }

    private ResourceLocation getTemplateName(JsonObject json) throws DeserialisationException {
        return JsonHelper.getAsOptional(json, "name", JsonDeserialisers.DT_RESOURCE_LOCATION)
                .orElseThrow(() -> new DeserialisationException("Configurable must state name of template to use."));
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
