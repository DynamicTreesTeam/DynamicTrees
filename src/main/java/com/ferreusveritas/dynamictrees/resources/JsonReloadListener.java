package com.ferreusveritas.dynamictrees.resources;

import com.google.common.collect.Maps;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import net.minecraft.resources.IResourceManager;
import net.minecraft.util.JSONUtils;
import net.minecraft.util.ResourceLocation;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.Map;

/**
 * @author Harley O'Connor
 */
public abstract class JsonReloadListener<V> extends JsonApplierReloadListener<Map<ResourceLocation, JsonElement>, V> {

    private static final Logger LOGGER = LogManager.getLogger();

    public JsonReloadListener(final String folderName, final Class<V> objectType, final String applierRegistryName) {
        super(folderName, objectType, applierRegistryName);
    }

    @Override
    protected Map<ResourceLocation, JsonElement> prepare(final IResourceManager resourceManager) {
        final Map<ResourceLocation, JsonElement> map = Maps.newHashMap();
        int i = this.folderName.length() + 1;

        for (ResourceLocation resourceLocationIn : resourceManager.getAllResourceLocations(this.folderName, (fileName) -> fileName.endsWith(JSON_EXTENSION))) {
            final String resourcePath = resourceLocationIn.getPath();
            final ResourceLocation resourceLocation = new ResourceLocation(resourceLocationIn.getNamespace(),
                    resourcePath.substring(i, resourcePath.length() - JSON_EXTENSION_LENGTH));

            try {
                final Reader reader = new BufferedReader(new InputStreamReader(resourceManager.getResource(resourceLocationIn).getInputStream(), StandardCharsets.UTF_8));
                final JsonElement jsonElement = JSONUtils.fromJson(this.gson, reader, JsonElement.class);

                if (jsonElement == null) {
                    LOGGER.error("Couldn't load data file {} from {} as it's null or empty", resourceLocation, resourceLocationIn);
                    break;
                }

                map.put(resourceLocation, jsonElement);
            } catch (IllegalArgumentException | IOException | JsonParseException e) {
                LOGGER.error("Couldn't parse data file {} from {}", resourceLocation, resourceLocationIn, e);
            }
        }

        return map;
    }

}
