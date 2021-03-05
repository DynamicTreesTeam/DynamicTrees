package com.ferreusveritas.dynamictrees.resources;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import net.minecraft.client.resources.ReloadListener;
import net.minecraft.profiler.IProfiler;
import net.minecraft.resources.IResourceManager;
import net.minecraft.util.JSONUtils;
import net.minecraft.util.ResourceLocation;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

/**
 * An extension of {@link ReloadListener} in which each map entry contains every file loaded
 * for the given resource location. Useful for reload listeners that need to load multiple files
 * from the same resource location.
 *
 * @author Harley O'Connor
 */
public abstract class MultiJsonReloadListener extends ReloadListener<Map<ResourceLocation, List<Pair<String, JsonElement>>>> {

    private static final Logger LOGGER = LogManager.getLogger();

    private static final String JSON_EXTENSION = ".json";
    private static final int JSON_EXTENSION_LENGTH = JSON_EXTENSION.length();

    private final Gson gson = (new GsonBuilder()).setPrettyPrinting().disableHtmlEscaping().create();
    private final String folderName;

    public MultiJsonReloadListener(final String folderName) {
        this.folderName = folderName;
    }

    @Override
    protected Map<ResourceLocation, List<Pair<String, JsonElement>>> prepare(IResourceManager resourceManager, IProfiler profiler) {
        final Map<ResourceLocation, List<Pair<String, JsonElement>>> map = Maps.newHashMap();
        int i = folderName.length() + 1;

        for(ResourceLocation resourceLocationIn : resourceManager.getAllResourceLocations(this.folderName, (fileName) -> fileName.endsWith(JSON_EXTENSION))) {
            final String resourcePath = resourceLocationIn.getPath();
            final ResourceLocation resourceLocation = new ResourceLocation(resourceLocationIn.getNamespace(),
                    resourcePath.substring(i, resourcePath.length() - JSON_EXTENSION_LENGTH));

            try {
                resourceManager.getAllResources(resourceLocationIn).forEach(resource -> {
                    final Reader reader = new BufferedReader(new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8));
                    final JsonElement jsonElement = JSONUtils.fromJson(this.gson, reader, JsonElement.class);

                    if (jsonElement == null) {
                        LOGGER.error("Couldn't load data file {} from {} as it's null or empty", resourceLocation, resourceLocationIn);
                        return;
                    }

                    map.computeIfAbsent(resourceLocation, l -> Lists.newArrayList()).add(Pair.of(resourceLocationIn.getPath(), jsonElement));
                });
            } catch (IllegalArgumentException | IOException | JsonParseException e) {
                LOGGER.error("Couldn't parse data file {} from {}", resourceLocation, resourceLocationIn, e);
            }
        }

        return map;
    }

}
