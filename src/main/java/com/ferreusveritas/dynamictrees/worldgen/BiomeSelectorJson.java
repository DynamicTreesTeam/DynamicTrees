package com.ferreusveritas.dynamictrees.worldgen;

import com.ferreusveritas.dynamictrees.DynamicTrees;
import com.ferreusveritas.dynamictrees.api.events.JsonCapabilityRegistryEvent;
import com.ferreusveritas.dynamictrees.trees.Species;
import com.ferreusveritas.dynamictrees.worldgen.json.IJsonBiomeSelector;
import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import net.minecraft.world.biome.Biome;
import net.minecraftforge.common.BiomeDictionary;

import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * Super-class for Json files handlers which apply data to biomes.
 *
 * @author Harley O'Connor
 */
// TODO: AhHhhhHHh. This needs changing.
public abstract class BiomeSelectorJson {

    public static final String SELECT = "select";
    public static final String NAME = "name";
    public static final String TYPE = "type";

    protected static Map<String, IJsonBiomeSelector> jsonBiomeSelectorMap = new HashMap<>();

    public static void addJsonBiomeSelector(String name, IJsonBiomeSelector selector) {
        jsonBiomeSelectorMap.put(name, selector);
    }

    public static void cleanupBiomeSelectors () {
        jsonBiomeSelectorMap.clear();
    }

    protected static void registerJsonBiomeCapabilities (JsonCapabilityRegistryEvent event) {
        event.register(NAME, jsonElement -> {
            if(jsonElement != null && jsonElement.isJsonPrimitive()) {
                JsonPrimitive primitive = jsonElement.getAsJsonPrimitive();
                if(primitive.isString()) {
                    String biomeMatch = primitive.getAsString();
                    return b-> b.getRegistryName().toString().matches(biomeMatch);
                }
            }

            return b -> false;
        });

        event.register(TYPE, jsonElement -> {
            if(jsonElement != null) {
                if (jsonElement.isJsonPrimitive()) {
                    String typeMatch = jsonElement.getAsString();
                    List<BiomeDictionary.Type> types = Arrays.asList(typeMatch.split(",")).stream().map(BiomeDictionary.Type::getType).collect(Collectors.toList());
                    return b -> biomeHasTypes(b, types);
                } else
                if (jsonElement.isJsonArray()) {
                    List<BiomeDictionary.Type> types = new ArrayList<>();
                    for(JsonElement element : jsonElement.getAsJsonArray()) {
                        if(element.isJsonPrimitive() && element.getAsJsonPrimitive().isString()) {
                            types.add(BiomeDictionary.Type.getType(element.getAsString()));
                        }
                    }
                    return b -> biomeHasTypes(b, types);
                }
            }

            return b -> false;
        });
    }

    protected static boolean biomeHasTypes(Biome biome, List<BiomeDictionary.Type> types) {
        return types.stream().allMatch(t -> BiomeDictionary.hasType(Species.getBiomeKey(biome), t));
    }

    protected static class JsonBiomeSelectorData {
        final IJsonBiomeSelector selector;
        final JsonElement elementData;

        JsonBiomeSelectorData(IJsonBiomeSelector selector, JsonElement elementData) {
            this.selector = selector;
            this.elementData = elementData;
        }

        public Predicate<Biome> getFilter() {
            return this.selector.getFilter(elementData);
        }
    }

    protected static boolean isComment(String s) {
        return s.startsWith("__"); // Allow for comments. Comments are any keys starting with "__".
    }

    protected List<JsonBiomeDatabasePopulator.JsonBiomeSelectorData> readSelection(String currentKey, JsonElement currentElement, String fileName) {
        List<JsonBiomeSelectorData> selectors = new ArrayList<>();

        if (!currentKey.equals(SELECT) || !currentElement.isJsonObject())
            return selectors;

        for(Map.Entry<String, JsonElement> selectElement : currentElement.getAsJsonObject().entrySet()) {
            String selectorName = selectElement.getKey();

            if(isComment(selectorName))
                continue;

            IJsonBiomeSelector selector = jsonBiomeSelectorMap.get(selectorName);
            if(selector != null) {
                selectors.add(new JsonBiomeDatabasePopulator.JsonBiomeSelectorData(selector, selectElement.getValue()));
            } else {
                DynamicTrees.getLogger().error("Json Error: Undefined selector property \"" + selectorName + "\" in " + fileName + ".");
            }
        }

        return selectors;
    }

}
