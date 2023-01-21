package com.ferreusveritas.dynamictrees.deserialisation;

import com.ferreusveritas.dynamictrees.api.applier.VoidApplier;
import com.ferreusveritas.dynamictrees.deserialisation.result.JsonResult;
import com.ferreusveritas.dynamictrees.deserialisation.result.Result;
import com.ferreusveritas.dynamictrees.util.BiomeList;
import com.ferreusveritas.dynamictrees.util.JsonMapWrapper;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.Tag;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.biome.Biome;
import net.minecraftforge.common.BiomeDictionary;
import net.minecraftforge.registries.ForgeRegistries;
import org.apache.logging.log4j.LogManager;

import java.util.*;

/**
 * @author Harley O'Connor
 */
public final class BiomeListDeserialiser implements JsonDeserialiser<BiomeList> {

    private static final Map<ResourceLocation, List<ResourceLocation>> TAGS = Maps.newHashMap();

    /**
     * Required for filtering by biome tags.
     */
    public static void cacheNewTags(Map<ResourceLocation, Tag<Holder<Biome>>> biomeTags) {
        TAGS.clear();
        biomeTags.forEach((key, tag) ->
                tag.getValues().forEach(biome ->
                        TAGS.computeIfAbsent(biome.unwrapKey().orElseThrow().location(), k -> Lists.newLinkedList()).add(key)
                )
        );
    }

    private static final VoidApplier<BiomeList, String> TYPE_APPLIER = (biomeList, typeRegex) -> {
        final boolean notOperator = usingNotOperator(typeRegex);
        typeRegex = typeRegex.toLowerCase();
        if (notOperator) {
            typeRegex = typeRegex.substring(1);
            removeBiomesWithMatchingType(biomeList, typeRegex);
        } else {
            removeBiomesWithoutMatchingType(biomeList, typeRegex);
        }
    };

    private static void removeBiomesWithMatchingType(BiomeList biomeList, String typeRegex) {
        biomeList.removeIf(biome -> getBiomeTypes(biome).stream().anyMatch(type -> typeMatches(typeRegex, type)));
    }

    private static void removeBiomesWithoutMatchingType(BiomeList biomeList, String typeRegex) {
        biomeList.removeIf(biome -> getBiomeTypes(biome).stream().noneMatch(type -> typeMatches(typeRegex, type)));
    }

    private static boolean typeMatches(String typeRegex, BiomeDictionary.Type type) {
        return type.toString().toLowerCase().matches(typeRegex);
    }

    private static Set<BiomeDictionary.Type> getBiomeTypes(Biome biome) {
        return BiomeDictionary.getTypes(
                ResourceKey.create(ForgeRegistries.Keys.BIOMES, Objects.requireNonNull(biome.getRegistryName()))
        );
    }

    private static final VoidApplier<BiomeList, String> TAG_APPLIER = (biomeList, tagRegex) -> {
        final boolean notOperator = usingNotOperator(tagRegex);
        tagRegex = tagRegex.toLowerCase();
        if (notOperator) {
            tagRegex = tagRegex.substring(1);
            removeBiomesWithMatchingTag(biomeList, tagRegex);
        } else {
            removeBiomesWithoutMatchingTag(biomeList, tagRegex);
        }
    };


    private static void removeBiomesWithMatchingTag(BiomeList biomeList, String tagRegex) {
        biomeList.removeIf(biome -> TAGS.get(biome.getRegistryName()).stream().anyMatch(tag -> tag.toString().matches(tagRegex)));
    }

    private static void removeBiomesWithoutMatchingTag(BiomeList biomeList, String tagRegex) {
        biomeList.removeIf(biome -> TAGS.get(biome.getRegistryName()).stream().noneMatch(tag -> tag.toString().matches(tagRegex)));
    }

    private static final VoidApplier<BiomeList, String> CATEGORY_APPLIER = (biomeList, categoryRegex) -> {
        final boolean notOperator = usingNotOperator(categoryRegex);
        if (notOperator) {
            categoryRegex = categoryRegex.substring(1);
            removeBiomesWithMatchingCategory(biomeList, categoryRegex);
        } else {
            removeBiomesWithoutMatchingCategory(biomeList, categoryRegex);
        }
    };

    private static void removeBiomesWithMatchingCategory(BiomeList biomeList, String categoryRegex) {
        biomeList.removeIf(biome -> biomeCategoryMatches(categoryRegex, biome));
    }

    private static void removeBiomesWithoutMatchingCategory(BiomeList biomeList, String categoryRegex) {
        biomeList.removeIf(biome -> !biomeCategoryMatches(categoryRegex, biome));
    }

    private static boolean biomeCategoryMatches(String categoryRegex, Biome biome) {
        return biome.getBiomeCategory().toString().toLowerCase().matches(categoryRegex);
    }


    private static final VoidApplier<BiomeList, String> NAME_APPLIER = (biomeList, nameRegex) -> {
        final boolean notOperator = usingNotOperator(nameRegex);
        nameRegex = nameRegex.toLowerCase();
        if (notOperator) {
            nameRegex = nameRegex.substring(1);
            removeBiomesWithMatchingName(biomeList, nameRegex);
        } else {
            removeBiomesWithoutMatchingName(biomeList, nameRegex);
        }
    };

    private static boolean usingNotOperator(String categoryString) {
        return categoryString.toCharArray()[0] == '!';
    }

    private static void removeBiomesWithMatchingName(BiomeList biomeList, String nameRegex) {
        biomeList.removeIf(biome -> biomeNameMatches(nameRegex, biome));
    }

    private static void removeBiomesWithoutMatchingName(BiomeList biomeList, String nameRegex) {
        biomeList.removeIf(biome -> !biomeNameMatches(nameRegex, biome));
    }

    private static boolean biomeNameMatches(String nameRegex, Biome biome) {
        return String.valueOf(biome.getRegistryName()).matches(nameRegex);
    }


    private static final VoidApplier<BiomeList, JsonArray> NAMES_OR_APPLIER = (biomeList, json) -> {
        final List<String> nameRegexes = JsonResult.forInput(json)
                .mapEachIfArray(String.class, (Result.SimpleMapper<String, String>) String::toLowerCase)
                .orElse(Collections.emptyList(), LogManager.getLogger()::error, LogManager.getLogger()::warn);

        final BiomeList validBiomes = new BiomeList();
        nameRegexes.forEach(nameRegex -> {
            if (usingNotOperator(nameRegex)) {
                nameRegex = nameRegex.substring(1);
                populateBlacklistForName(validBiomes, nameRegex);
            } else {
                populateWhitelistForName(validBiomes, nameRegex);
            }
        });

        biomeList.removeIf(biome -> !validBiomes.contains(biome));
    };

    private static void populateListsForName(BiomeList whitelist, BiomeList blacklist, String nameRegex) {
        if (usingNotOperator(nameRegex)) {
            nameRegex = nameRegex.substring(1);
            populateBlacklistForName(whitelist, nameRegex);
        } else {
            populateWhitelistForName(blacklist, nameRegex);
        }
    }

    private static void populateWhitelistForName(BiomeList whitelist, String nameRegex) {
        ForgeRegistries.BIOMES.getValues().stream()
                .filter(biome -> biomeNameMatches(nameRegex, biome))
                .forEach(whitelist::add);
    }

    private static void populateBlacklistForName(BiomeList blacklist, String nameRegex) {
        ForgeRegistries.BIOMES.getValues().stream()
                .filter(biome -> !biomeNameMatches(nameRegex, biome))
                .forEach(blacklist::add);
    }

    private final VoidApplier<BiomeList, JsonObject> andOperator =
            (biomes, jsonObject) -> applyAllAppliers(jsonObject, biomes);

    private final VoidApplier<BiomeList, JsonArray> orOperator = (biomeList, json) -> {
        BiomeList biomesList = new BiomeList();
        JsonResult.forInput(json)
                .mapEachIfArray(JsonObject.class, object -> {
                    BiomeList subList = BiomeList.getAll();
                    applyAllAppliers(object, subList);
                    biomesList.addAll(subList);
                    return object;
                })
                .orElse(null, LogManager.getLogger()::error, LogManager.getLogger()::warn);
        biomeList.removeIf(biome -> !biomesList.contains(biome));
    };

    private final VoidApplier<BiomeList, JsonObject> notOperator = (biomeList, jsonObject) -> {
        final BiomeList notBiomeList = BiomeList.getAll();
        applyAllAppliers(jsonObject, notBiomeList);
        notBiomeList.forEach(biomeList::remove);
    };

    private final JsonPropertyAppliers<BiomeList> appliers = new JsonPropertyAppliers<>(BiomeList.class);

    public BiomeListDeserialiser() {
        registerAppliers();
    }

    private void registerAppliers() {
        this.appliers
                .register("type", String.class, TYPE_APPLIER)
                .registerArrayApplier("types", String.class, TYPE_APPLIER)
                .register("tag", String.class, TAG_APPLIER)
                .registerArrayApplier("tags", String.class, TAG_APPLIER)
                .register("category", String.class, CATEGORY_APPLIER)
                .register("name", String.class, NAME_APPLIER)
                .registerArrayApplier("names", String.class, NAME_APPLIER)
                .register("names_or", JsonArray.class, NAMES_OR_APPLIER)
                .registerArrayApplier("AND", JsonObject.class, andOperator)
                .register("OR", JsonArray.class, orOperator)
                .register("NOT", JsonObject.class, notOperator);
    }

    private void applyAllAppliers(JsonObject json, BiomeList biomes) {
        appliers.applyAll(new JsonMapWrapper(json), biomes);
    }

    @Override
    public Result<BiomeList, JsonElement> deserialise(final JsonElement input) {
        return JsonResult.forInput(input)
                .mapIfType(Biome.class, biome -> new BiomeList(Collections.singletonList(biome)))
                .elseMapIfType(JsonObject.class, selectorObject -> {
                    // Start with a list of all biomes.
                    final BiomeList biomes = BiomeList.getAll();
                    // Apply from all appliers, filtering the list.
                    applyAllAppliers(selectorObject, biomes);
                    return biomes;
                }).elseTypeError();
    }

}
