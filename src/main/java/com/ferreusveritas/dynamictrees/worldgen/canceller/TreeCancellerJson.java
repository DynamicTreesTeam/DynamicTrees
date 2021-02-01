package com.ferreusveritas.dynamictrees.worldgen.canceller;

import com.ferreusveritas.dynamictrees.api.WorldGenRegistry;
import com.ferreusveritas.dynamictrees.util.JsonHelper;
import com.ferreusveritas.dynamictrees.api.events.TreeCancelRegistryEvent;
import com.ferreusveritas.dynamictrees.worldgen.BiomeSelectorJson;
import com.google.common.collect.Lists;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.biome.Biome;
import net.minecraftforge.registries.ForgeRegistries;

import java.io.File;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Default implementation of {@link ITreeCanceller}, this both takes cancellations from the Json file and the
 * {@link TreeCancelRegistryEvent} - or just the Json if <tt>autoCancel</tt> is disabled.
 *
 * @author Harley O'Connor
 */
public class TreeCancellerJson extends BiomeSelectorJson implements ITreeCanceller {

    public static TreeCancellerJson INSTANCE = null;

    public static final String AUTO_CANCEL = "auto_cancel";
    public static final String CANCEL_NAMESPACES = "cancel_namespaces";

    private final File file;
    private final JsonElement jsonElement;

    private boolean autoCancel = true;

    private final Map<ResourceLocation, Set<String>> cancellationEntries = new HashMap<>();

    public TreeCancellerJson (File file, WorldGenRegistry.TreeCancellerJsonCapabilityRegistryEvent event) {
        this.file = file;
        this.jsonElement = JsonHelper.load(file);

        this.registerJsonCapabilities(event);
        this.loadFileContents();

        INSTANCE = this;
    }

    public void registerJsonCapabilities(WorldGenRegistry.TreeCancellerJsonCapabilityRegistryEvent event) {
        registerJsonBiomeCapabilities(event);
    }

    public void loadFileContents() {
        if(jsonElement != null && jsonElement.isJsonArray()) {
            for (JsonElement sectionElement : jsonElement.getAsJsonArray()) {
                if (sectionElement.isJsonObject()) {
                    JsonObject section = sectionElement.getAsJsonObject();
                    this.readSection(section);
                }
            }
        }
    }

    private void readSection (JsonObject section) {
        List<JsonBiomeSelectorData> selectors = new LinkedList<>();
        Set<String> namespaces = new HashSet<>();

        for (Map.Entry<String, JsonElement> entry : section.entrySet()) {
            String key = entry.getKey();
            JsonElement element = entry.getValue();

            if (isComment(key))
                continue;

            List<JsonBiomeSelectorData> elementSelectors = this.readSelection(key, element, this.file.getName());

            if (elementSelectors.size() > 1) {
                selectors.addAll(elementSelectors);
            } else if (key.equals(AUTO_CANCEL)) {
                if (element.isJsonPrimitive()) {
                    this.autoCancel = element.getAsJsonPrimitive().getAsBoolean();
                }
                break; // Custom canceller is a special section, so there's no need to read anything else for this section.
            } else if (key.equals(CANCEL_NAMESPACES)) {
                if (element.isJsonArray()) {
                    for (JsonElement namespace : element.getAsJsonArray()) {
                        if (namespace.isJsonPrimitive()) {
                            namespaces.add(namespace.getAsString());
                        }
                    }
                }
            }
        }

        // Filter biomes by selector predicates
        Stream<Biome> stream = Lists.newArrayList(ForgeRegistries.BIOMES).stream();
        for(JsonBiomeSelectorData s : selectors) {
            stream = stream.filter(s.getFilter());
        }

        stream.forEach(biome -> this.registerCancellations(biome.getRegistryName(), namespaces.stream().collect(Collectors.toList())));
    }

    @Override
    public boolean shouldCancelFeatures(ResourceLocation biomeResLoc) {
        return this.cancellationEntries.containsKey(biomeResLoc);
    }

    @Override
    public boolean shouldCancelFeature(ResourceLocation biomeResLoc, ResourceLocation featureResLoc) {
        return this.cancellationEntries.get(biomeResLoc).contains(featureResLoc.getNamespace());
    }

    @Override
    public void registerCancellations(String modIdForBiomes, List<String> namespaces) {
        ForgeRegistries.BIOMES.forEach(biome -> {
            ResourceLocation biomeResLoc = biome.getRegistryName();

            if (biomeResLoc != null && biomeResLoc.getNamespace().equals(modIdForBiomes))
                this.registerCancellations(biomeResLoc, namespaces);
        });
    }

    @Override
    public void registerCancellations(ResourceLocation biomeResLoc, List<String> namespaces) {
        if (namespaces.size() < 1)
            return;

        if (!this.cancellationEntries.containsKey(biomeResLoc))
            this.cancellationEntries.put(biomeResLoc, new HashSet<>());

        this.cancellationEntries.get(biomeResLoc).addAll(namespaces);
    }

    public boolean isAutoCancel() {
        return autoCancel;
    }

}
