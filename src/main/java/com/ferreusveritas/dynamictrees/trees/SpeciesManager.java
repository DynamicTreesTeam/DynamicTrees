package com.ferreusveritas.dynamictrees.trees;

import com.google.common.collect.Maps;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import net.minecraft.client.resources.JsonReloadListener;
import net.minecraft.profiler.IProfiler;
import net.minecraft.resources.IResourceManager;
import net.minecraft.util.ResourceLocation;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Map;

/**
 * @author Harley O'Connor
 */
public final class SpeciesManager extends JsonReloadListener {

    private static final Gson GSON = (new GsonBuilder()).setPrettyPrinting().disableHtmlEscaping().create();
    private static final Logger LOGGER = LogManager.getLogger();

    public SpeciesManager() {
        super(GSON, "trees/species");
    }

    @Override
    protected void apply(Map<ResourceLocation, JsonElement> jsonFiles, IResourceManager resourceManager, IProfiler profiler) {

    }

}
