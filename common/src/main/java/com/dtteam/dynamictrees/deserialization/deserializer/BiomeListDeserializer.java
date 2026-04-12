package com.dtteam.dynamictrees.deserialization.deserializer;

import com.dtteam.dynamictrees.deserialization.JsonMapWrapper;
import com.dtteam.dynamictrees.deserialization.JsonPropertyAppliers;
import com.dtteam.dynamictrees.deserialization.applier.Applier;
import com.dtteam.dynamictrees.deserialization.applier.PropertyApplierResult;
import com.dtteam.dynamictrees.deserialization.applier.VoidApplier;
import com.dtteam.dynamictrees.deserialization.result.JsonResult;
import com.dtteam.dynamictrees.deserialization.result.Result;
import com.dtteam.dynamictrees.platform.Services;
import com.dtteam.dynamictrees.worldgen.IDTBiomeHolderSet;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.IdentifierException;
import net.minecraft.core.HolderSet;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.biome.Biome;
import org.apache.logging.log4j.LogManager;

import java.util.*;
import java.util.function.Supplier;

/**
 * @author Harley O'Connor
 */
public final class BiomeListDeserializer implements JsonDeserializer<IDTBiomeHolderSet> {

    public static final Supplier<Registry<Biome>> DELAYED_BIOME_REGISTRY = () -> {
        MinecraftServer currentServer = Services.MISC.getCurrentServer();
        if (currentServer == null)
            throw new IllegalStateException("Queried biome registry too early; server does not exist yet!");
        return currentServer.registryAccess().lookupOrThrow(Registries.BIOME);
    };

    private static final Applier<IDTBiomeHolderSet, String> TAG_APPLIER = (biomeList, tagRegex) -> {
        tagRegex = tagRegex.toLowerCase(Locale.ENGLISH);
        final boolean notOperator = usingNotOperator(tagRegex);
        if (notOperator)
            tagRegex = tagRegex.substring(1);
        if (tagRegex.charAt(0) == '#')
            tagRegex = tagRegex.substring(1);

        try {
            Identifier tagLocation = Identifier.parse(tagRegex);
            TagKey<Biome> tagKey = TagKey.create(Registries.BIOME, tagLocation);

            // TODO UPDATE: This is used as a regex in 1.19.2. Double check!!!
            biomeList.addDelayedHolderSet(
                    (notOperator ? biomeList.getExcludeComponents() : biomeList.getIncludeComponents()),
                    () -> DELAYED_BIOME_REGISTRY.get().getOrThrow(tagKey));
        } catch (IdentifierException e) {
            return PropertyApplierResult.failure(e.getMessage());
        }

        // TODO UPDATE
        // (notOperator ? biomeList.getExcludeComponents() : biomeList.getIncludeComponents()).add(new TagsRegexMatchHolderSet<>(DELAYED_BIOME_REGISTRY, tagRegex));
        return PropertyApplierResult.success();
    };

    private static final VoidApplier<IDTBiomeHolderSet, String> NAME_APPLIER = (biomeList, nameRegex) -> {
        nameRegex = nameRegex.toLowerCase(Locale.ENGLISH);
        final boolean notOperator = usingNotOperator(nameRegex);
        if (notOperator)
            nameRegex = nameRegex.substring(1);

        String finalNameRegex = nameRegex;
        biomeList.addNameRegexMatch(
                (notOperator ? biomeList.getExcludeComponents() : biomeList.getIncludeComponents()),
                DELAYED_BIOME_REGISTRY::get, finalNameRegex);
    };

    private static boolean usingNotOperator(String categoryString) {
        return categoryString.charAt(0) == '!';
    }

    private static final VoidApplier<IDTBiomeHolderSet, JsonArray> NAMES_OR_APPLIER = (biomeList, json) -> {
        final List<String> nameRegexes = JsonResult.forInput(json)
                .mapEachIfArray(String.class, (Result.SimpleMapper<String, String>) String::toLowerCase)
                .orElse(Collections.emptyList(), LogManager.getLogger()::error, LogManager.getLogger()::warn);

        List<HolderSet<Biome>> orIncludes = new ArrayList<>();
        List<HolderSet<Biome>> orExcludes = new ArrayList<>();
        nameRegexes.forEach(nameRegex -> {
            nameRegex = nameRegex.toLowerCase(Locale.ENGLISH);
            final boolean notOperator = usingNotOperator(nameRegex);
            if (notOperator)
                nameRegex = nameRegex.substring(1);

            String finalNameRegex = nameRegex;
            biomeList.addNameRegexMatch(
                    (notOperator ? orExcludes : orIncludes),
                    DELAYED_BIOME_REGISTRY::get, finalNameRegex);
        });

        if (!orIncludes.isEmpty())
            biomeList.addOr(biomeList.getIncludeComponents(), orIncludes);
        if (!orExcludes.isEmpty())
            biomeList.addOr(biomeList.getExcludeComponents(), orExcludes);
    };

    private static final VoidApplier<IDTBiomeHolderSet, JsonArray> TAGS_OR_APPLIER = (biomeList, json) -> {
        final List<String> nameRegexes = JsonResult.forInput(json)
                .mapEachIfArray(String.class, (Result.SimpleMapper<String, String>) String::toLowerCase)
                .orElse(Collections.emptyList(), LogManager.getLogger()::error, LogManager.getLogger()::warn);

        List<HolderSet<Biome>> orIncludes = new ArrayList<>();
        List<HolderSet<Biome>> orExcludes = new ArrayList<>();
        nameRegexes.forEach(tagRegex -> {
            tagRegex = tagRegex.toLowerCase(Locale.ENGLISH);
            final boolean notOperator = usingNotOperator(tagRegex);
            if (notOperator)
                tagRegex = tagRegex.substring(1);
            if (tagRegex.charAt(0) == '#')
                tagRegex = tagRegex.substring(1);

            biomeList.addTagsRegexMatch(
                    (notOperator ? orExcludes : orIncludes),
                    DELAYED_BIOME_REGISTRY::get, tagRegex);
        });

        if (!orIncludes.isEmpty())
            biomeList.addOr(biomeList.getIncludeComponents(), orIncludes);
        if (!orExcludes.isEmpty())
            biomeList.addOr(biomeList.getExcludeComponents(), orExcludes);
    };

    private final VoidApplier<IDTBiomeHolderSet, JsonObject> andOperator =
            (biomes, jsonObject) -> applyAllAppliers(jsonObject, biomes);

    private final VoidApplier<IDTBiomeHolderSet, JsonArray> orOperator = (biomeList, json) -> {
        List<HolderSet<Biome>> appliedList = new LinkedList<>();

        JsonResult.forInput(json)
                .mapEachIfArray(JsonObject.class, object -> {
                    IDTBiomeHolderSet subList = Services.MISC.newDTBiomeHolderSet();
                    applyAllAppliers(object, subList);
                    appliedList.add(subList);
                    return object;
                })
                .orElse(null, LogManager.getLogger()::error, LogManager.getLogger()::warn);

        if (!appliedList.isEmpty())
            biomeList.addOr(biomeList.getIncludeComponents(), appliedList);
    };

    private final VoidApplier<IDTBiomeHolderSet, JsonObject> notOperator = (biomeList, jsonObject) -> {
        final IDTBiomeHolderSet notBiomeList = Services.MISC.newDTBiomeHolderSet();
        applyAllAppliers(jsonObject, notBiomeList);
        biomeList.getExcludeComponents().add(notBiomeList);
    };

    private final JsonPropertyAppliers<IDTBiomeHolderSet> appliers = new JsonPropertyAppliers<>(IDTBiomeHolderSet.class);

    public BiomeListDeserializer() {
        registerAppliers();
    }

    private void registerAppliers() {
        this.appliers
                .register("tag", String.class, TAG_APPLIER)
                .registerArrayApplier("tags", String.class, TAG_APPLIER)
                .register("tags_or", JsonArray.class, TAGS_OR_APPLIER)
                .register("name", String.class, NAME_APPLIER)
                .registerArrayApplier("names", String.class, NAME_APPLIER)
                .register("names_or", JsonArray.class, NAMES_OR_APPLIER)
                .registerArrayApplier("AND", JsonObject.class, andOperator)
                .register("OR", JsonArray.class, orOperator)
                .register("NOT", JsonObject.class, notOperator);
    }

    private void applyAllAppliers(JsonObject json, IDTBiomeHolderSet biomes) {
        appliers.applyAll(new JsonMapWrapper(json), biomes);
    }

    @Override
    public Result<IDTBiomeHolderSet, JsonElement> deserialize(final JsonElement input) {
        return JsonResult.forInput(input)
                .mapIfType(String.class, biomeName -> {
                    IDTBiomeHolderSet biomes = Services.MISC.newDTBiomeHolderSet();
                    biomes.addNameRegexMatch(biomes.getIncludeComponents(), DELAYED_BIOME_REGISTRY::get, biomeName.toLowerCase(Locale.ENGLISH));
                    return biomes;
                })
                .elseMapIfType(JsonObject.class, selectorObject -> {
                    final IDTBiomeHolderSet biomes = Services.MISC.newDTBiomeHolderSet();
                    // Apply from all appliers
                    applyAllAppliers(selectorObject, biomes);
                    return biomes;
                }).elseTypeError();
    }

}