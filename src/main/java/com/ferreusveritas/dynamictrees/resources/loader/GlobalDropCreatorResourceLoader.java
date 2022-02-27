package com.ferreusveritas.dynamictrees.resources.loader;

import com.ferreusveritas.dynamictrees.api.resource.Resource;
import com.ferreusveritas.dynamictrees.api.resource.ResourceAccessor;
import com.ferreusveritas.dynamictrees.api.resource.loading.AbstractResourceLoader;
import com.ferreusveritas.dynamictrees.api.resource.loading.ApplicationException;
import com.ferreusveritas.dynamictrees.api.resource.loading.preparation.JsonResourcePreparer;
import com.ferreusveritas.dynamictrees.deserialisation.JsonDeserialisers;
import com.ferreusveritas.dynamictrees.systems.dropcreators.GlobalDropCreators;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.resources.IResourceManager;
import net.minecraft.util.ResourceLocation;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import static com.ferreusveritas.dynamictrees.deserialisation.JsonHelper.throwIfNotJsonObject;

/**
 * @author Harley O'Connor
 */
public final class GlobalDropCreatorResourceLoader extends AbstractResourceLoader<JsonElement> {

    private static final Logger LOGGER = LogManager.getLogger();

    public GlobalDropCreatorResourceLoader() {
        super(new JsonResourcePreparer("drop_creators/global"));
    }

    @Override
    public void applyOnReload(ResourceAccessor<JsonElement> resourceAccessor, IResourceManager resourceManager) {
        resourceAccessor.forEach(this::tryReadEntry);
    }

    private void tryReadEntry(Resource<JsonElement> resource) {
        try {
            this.readEntry(resource);
        } catch (ApplicationException e) {
            LOGGER.error("Error loading global drop creator \"{}\": {}",
                    resource.getLocation(), e.getMessage());
        }
    }

    private void readEntry(Resource<JsonElement> resource) throws ApplicationException {
        throwIfNotJsonObject(resource.getResource(), () -> new ApplicationException("Root element is not a Json object."));
        this.deserialiseAndPutEntry(resource.getLocation(), resource.getResource().getAsJsonObject());
    }

    private void deserialiseAndPutEntry(ResourceLocation name, JsonObject json) {
        JsonDeserialisers.CONFIGURED_DROP_CREATOR.deserialise(json)
                .ifSuccessOrElse(
                        result -> GlobalDropCreators.put(name, result),
                        error -> LOGGER.error("Error loading global drop creator \"{}\": {}", name, error),
                        warning -> LOGGER.warn("Warning whilst loading global drop creator \"{}\": {}", name, warning)
                );
    }
}
