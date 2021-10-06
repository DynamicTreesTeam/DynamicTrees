package com.ferreusveritas.dynamictrees.api.resource.loading.preparation;

import com.ferreusveritas.dynamictrees.api.resource.ResourceCollector;
import com.ferreusveritas.dynamictrees.api.resource.Resource;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import net.minecraft.resources.IResource;
import net.minecraft.util.JSONUtils;
import net.minecraft.util.ResourceLocation;

import javax.annotation.Nonnull;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;

/**
 * @author Harley O'Connor
 */
public final class JsonResourcePreparer extends AbstractResourcePreparer<JsonElement> {

    private static final String JSON_EXTENSION = ".json";

    private static final Gson GSON = (new GsonBuilder())
            .setPrettyPrinting()
            .disableHtmlEscaping()
            .create();

    public JsonResourcePreparer(String folderName) {
        this(folderName, ResourceCollector.ordered());
    }

    public JsonResourcePreparer(String folderName, ResourceCollector<JsonElement> resourceCollector) {
        super(folderName, JSON_EXTENSION, resourceCollector);
    }

    @Override
    protected void readAndPutResource(IResource resource, ResourceLocation resourceName) throws PreparationException {
        final JsonElement jsonElement = readResource(resource);
        this.resourceCollector.put(new Resource<>(resourceName, jsonElement));
    }

    @Nonnull
    static JsonElement readResource(IResource resource) throws PreparationException {
        final Reader reader =
                new BufferedReader(new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8));
        final JsonElement json = JSONUtils.fromJson(GSON, reader, JsonElement.class);

        if (json == null) {
            throw new PreparationException("Couldn't load file as it's null or empty");
        }
        return json;
    }

}
