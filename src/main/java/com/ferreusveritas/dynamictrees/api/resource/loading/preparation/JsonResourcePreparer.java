package com.ferreusveritas.dynamictrees.api.resource.loading.preparation;

import com.ferreusveritas.dynamictrees.api.resource.DTResource;
import com.ferreusveritas.dynamictrees.api.resource.ResourceCollector;
import com.ferreusveritas.dynamictrees.deserialisation.JsonHelper;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.util.GsonHelper;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.IOException;
import java.io.Reader;

/**
 * @author Harley O'Connor
 */
public final class JsonResourcePreparer extends AbstractResourcePreparer<JsonElement> {

    private static final String JSON_EXTENSION = ".json";

    public JsonResourcePreparer(String folderName) {
        this(folderName, ResourceCollector.ordered());
    }

    public JsonResourcePreparer(String folderName, ResourceCollector<JsonElement> resourceCollector) {
        super(folderName, JSON_EXTENSION, resourceCollector);
    }

    @Override
    protected void readAndPutResource(Resource resource, ResourceLocation resourceName) throws PreparationException, IOException {
        final JsonElement jsonElement = readResource(resource);
        this.resourceCollector.put(new DTResource<>(resourceName, jsonElement));
    }

    @Nonnull
    static JsonElement readResource(Resource resource) throws PreparationException, IOException {
        final Reader reader = resource.openAsReader();
        final JsonElement json = tryParseJson(reader);

        if (json == null) {
            throw new PreparationException("Couldn't load file as it's null or empty");
        }
        return json;
    }

    @Nullable
    private static JsonElement tryParseJson(Reader reader) throws PreparationException {
        try {
            return GsonHelper.fromJson(JsonHelper.getGson(), reader, JsonElement.class);
        } catch (JsonParseException e) {
            throw new PreparationException(e);
        }
    }

}