package com.dtteam.dynamictrees.api.resource.loading.preparation;

import com.dtteam.dynamictrees.api.resource.DTResource;
import com.dtteam.dynamictrees.api.resource.ResourceCollector;
import com.dtteam.dynamictrees.deserialization.JsonHelper;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.util.GsonHelper;
import org.jetbrains.annotations.NotNull;

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

    protected void readAndPutResource(Resource resource, Identifier resourceName) throws PreparationException, IOException {
        final JsonElement jsonElement = readResource(resource);
        this.resourceCollector.put(new DTResource<>(resourceName, jsonElement));
    }

    @NotNull
    static JsonElement readResource(Resource resource) throws PreparationException, IOException {
        final Reader reader = resource.openAsReader();
        return parseJson(reader);
    }

    private static JsonElement parseJson(Reader reader) throws PreparationException {
        try {
            return GsonHelper.fromJson(JsonHelper.getGson(), reader, JsonElement.class);
        } catch (JsonParseException e) {
            throw new PreparationException(e);
        }
    }

}