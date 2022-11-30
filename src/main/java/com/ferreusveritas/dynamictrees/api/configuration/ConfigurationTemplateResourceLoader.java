package com.ferreusveritas.dynamictrees.api.configuration;

import com.ferreusveritas.dynamictrees.api.resource.DTResource;
import com.ferreusveritas.dynamictrees.api.resource.ResourceAccessor;
import com.ferreusveritas.dynamictrees.api.resource.loading.AbstractResourceLoader;
import com.ferreusveritas.dynamictrees.api.resource.loading.preparation.JsonResourcePreparer;
import com.ferreusveritas.dynamictrees.deserialisation.DeserialisationException;
import com.ferreusveritas.dynamictrees.deserialisation.result.JsonResult;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.server.packs.resources.ResourceManager;
import org.apache.logging.log4j.LogManager;

import java.util.Collections;
import java.util.List;

/**
 * @author Harley O'Connor
 */
public class ConfigurationTemplateResourceLoader<C extends Configuration<C, E>, E extends ConfigurableRegistryEntry<E, C>>
        extends AbstractResourceLoader<JsonElement> {

    private final ConfigurableRegistry<E, C> configurableRegistry;
    private final TemplateRegistry<C> templateRegistry;

    public ConfigurationTemplateResourceLoader(String folderName, ConfigurableRegistry<E, C> configurableRegistry,
                                               TemplateRegistry<C> templateRegistry) {
        super(new JsonResourcePreparer(folderName));
        this.configurableRegistry = configurableRegistry;
        this.templateRegistry = templateRegistry;
    }

    @Override
    public void applyOnReload(ResourceAccessor<JsonElement> resourceAccessor, ResourceManager resourceManager) {
        resourceAccessor.forEach(resource -> {
            try {
                this.register(resource.map(JsonElement::getAsJsonObject));
            } catch (DeserialisationException | IllegalStateException e) {
                LogManager.getLogger()
                        .error("Error deserialising " + configurableRegistry.getType().getSimpleName() + " \"{}\": {}",
                                resource.getLocation(), e.getMessage());
            }
        });
    }

    protected void register(DTResource<JsonObject> resource) throws DeserialisationException {
        this.assertNameNotReserved(resource);

        final List<PropertyDefinition<?>> propertyDefinitions =
                this.deserialisePropertyDefinitions(resource.getResource());
        final E configurable = this.getConfigurable(resource);
        final CustomConfigurationTemplate<C> template =
                this.createTemplate(resource, propertyDefinitions, configurable);
        this.templateRegistry.register(resource.getLocation(), template);
    }

    @SuppressWarnings("unchecked")
    private CustomConfigurationTemplate<C> createTemplate(DTResource<JsonObject> resource,
                                                          List<PropertyDefinition<?>> propertyDefinitions,
                                                          E configurable) {
        return new CustomConfigurationTemplate<>(
                propertyDefinitions,
                resource.getResource().toString(),
                (Class<C>) configurable.getDefaultConfiguration().getClass(),
                configurable
        );
    }

    private E getConfigurable(DTResource<JsonObject> resource) throws DeserialisationException {
        return this.configurableRegistry.get(
                this.getConfigurableName(resource.getResource())
        );
    }

    private String getConfigurableName(JsonObject json) throws DeserialisationException {
        return JsonResult.success(json, json)
                .mapIfContains("name", String.class, (name) -> name)
                .orElseThrow();
    }

    private List<PropertyDefinition<?>> deserialisePropertyDefinitions(JsonObject json) {
        return JsonResult.success(json, json)
                .mapIfContains("property_definitions", JsonArray.class, (array, warningAppender) ->
                        JsonResult.forInput(array)
                                .mapToListOfType(PropertyDefinition.captureClass())
                                .forEachWarning(warningAppender)
                                .orElseThrow())
                .orElse(Collections.emptyList());
    }

    private void assertNameNotReserved(DTResource<?> resource) throws DeserialisationException {
        if (this.configurableRegistry.has(resource.getLocation())) {
            throw new DeserialisationException("Cannot override default configuration of " +
                    this.configurableRegistry.getType().getSimpleName() + " with key \"" + resource.getLocation() +
                    "\".");
        }
    }

}
