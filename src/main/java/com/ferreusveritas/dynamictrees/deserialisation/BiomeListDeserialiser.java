package com.ferreusveritas.dynamictrees.deserialisation;

import com.ferreusveritas.dynamictrees.api.treepacks.Applier;
import com.ferreusveritas.dynamictrees.api.treepacks.PropertyApplierResult;
import com.ferreusveritas.dynamictrees.api.treepacks.VoidApplier;
import com.ferreusveritas.dynamictrees.deserialisation.result.JsonResult;
import com.ferreusveritas.dynamictrees.deserialisation.result.Result;
import com.ferreusveritas.dynamictrees.util.JsonMapWrapper;
import com.ferreusveritas.dynamictrees.util.holderset.DelayedTagEntriesHolderSet;
import com.ferreusveritas.dynamictrees.util.holderset.IncludesExcludesHolderSet;
import com.ferreusveritas.dynamictrees.util.holderset.NameRegexMatchHolderSet;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.ResourceLocationException;
import net.minecraft.core.HolderSet;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.biome.Biome;
import net.minecraftforge.registries.holdersets.OrHolderSet;
import net.minecraftforge.server.ServerLifecycleHooks;
import org.apache.logging.log4j.LogManager;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.function.Supplier;

/**
 * @author Harley O'Connor
 */
public final class BiomeListDeserialiser implements JsonDeserialiser<IncludesExcludesHolderSet<Biome>> {

    public static final Supplier<Registry<Biome>> DELAYED_BIOME_REGISTRY = () -> ServerLifecycleHooks.getCurrentServer().registryAccess().registryOrThrow(Registry.BIOME_REGISTRY);

    private static final Applier<IncludesExcludesHolderSet<Biome>, String> TAG_APPLIER = (biomeList, tagString) -> {
        tagString = tagString.toLowerCase();
        final boolean notOperator = usingNotOperator(tagString);
        if (notOperator)
            tagString = tagString.substring(1);
        if (tagString.charAt(0) == '#')
            tagString = tagString.substring(1);

        try {
            ResourceLocation tagLocation = new ResourceLocation(tagString);
            TagKey<Biome> tagKey = TagKey.create(Registry.BIOME_REGISTRY, tagLocation);

            (notOperator ? biomeList.getExcludeComponents() : biomeList.getIncludeComponents()).add(new DelayedTagEntriesHolderSet<>(DELAYED_BIOME_REGISTRY, tagKey));
        } catch (ResourceLocationException e) {
            return PropertyApplierResult.failure(e.getMessage());
        }

        return PropertyApplierResult.success();
    };

    private static final VoidApplier<IncludesExcludesHolderSet<Biome>, String> NAME_APPLIER = (biomeList, nameRegex) -> {
        nameRegex = nameRegex.toLowerCase();
        final boolean notOperator = usingNotOperator(nameRegex);
        if (notOperator)
            nameRegex = nameRegex.substring(1);

        (notOperator ? biomeList.getExcludeComponents() : biomeList.getIncludeComponents()).add(new NameRegexMatchHolderSet<>(DELAYED_BIOME_REGISTRY, nameRegex));
    };

    private static boolean usingNotOperator(String categoryString) {
        return categoryString.charAt(0) == '!';
    }

    private static final VoidApplier<IncludesExcludesHolderSet<Biome>, JsonArray> NAMES_OR_APPLIER = (biomeList, json) -> {
        final List<String> nameRegexes = JsonResult.forInput(json)
                .mapEachIfArray(String.class, (Result.SimpleMapper<String, String>) String::toLowerCase)
                .orElse(Collections.emptyList(), LogManager.getLogger()::error, LogManager.getLogger()::warn);

        List<HolderSet<Biome>> orIncludes = new ArrayList<>();
        List<HolderSet<Biome>> orExcludes = new ArrayList<>();
        nameRegexes.forEach(nameRegex -> {
            nameRegex = nameRegex.toLowerCase();
            final boolean notOperator = usingNotOperator(nameRegex);
            if (notOperator)
                nameRegex = nameRegex.substring(1);

            (notOperator ? orExcludes : orIncludes).add(new NameRegexMatchHolderSet<>(DELAYED_BIOME_REGISTRY, nameRegex));
        });

        if (!orIncludes.isEmpty())
            biomeList.getIncludeComponents().add(new OrHolderSet<>(orIncludes));
        if (!orExcludes.isEmpty())
            biomeList.getExcludeComponents().add(new OrHolderSet<>(orExcludes));
    };

    private final VoidApplier<IncludesExcludesHolderSet<Biome>, JsonObject> andOperator =
            (biomes, jsonObject) -> applyAllAppliers(jsonObject, biomes);

    private final VoidApplier<IncludesExcludesHolderSet<Biome>, JsonArray> orOperator = (biomeList, json) -> {
        JsonResult.forInput(json)
                .mapEachIfArray(JsonObject.class, object -> {
                    IncludesExcludesHolderSet<Biome> subList = IncludesExcludesHolderSet.emptyAnds();
                    applyAllAppliers(object, subList);
                    biomeList.getIncludeComponents().add(subList);
                    return object;
                })
                .orElse(null, LogManager.getLogger()::error, LogManager.getLogger()::warn);
    };

    private final VoidApplier<IncludesExcludesHolderSet<Biome>, JsonObject> notOperator = (biomeList, jsonObject) -> {
        final IncludesExcludesHolderSet<Biome> notBiomeList = IncludesExcludesHolderSet.emptyAnds();
        applyAllAppliers(jsonObject, notBiomeList);
        biomeList.getExcludeComponents().add(notBiomeList);
    };

    private final JsonPropertyAppliers<IncludesExcludesHolderSet<Biome>> appliers = new JsonPropertyAppliers<>(IncludesExcludesHolderSet.getCastedClass());

    public BiomeListDeserialiser() {
        registerAppliers();
    }

    private void registerAppliers() {
        this.appliers
                .register("tag", String.class, TAG_APPLIER)
                .registerArrayApplier("tags", String.class, TAG_APPLIER)
                .register("name", String.class, NAME_APPLIER)
                .registerArrayApplier("names", String.class, NAME_APPLIER)
                .register("names_or", JsonArray.class, NAMES_OR_APPLIER)
                .registerArrayApplier("AND", JsonObject.class, andOperator)
                .register("OR", JsonArray.class, orOperator)
                .register("NOT", JsonObject.class, notOperator);
    }

    private void applyAllAppliers(JsonObject json, IncludesExcludesHolderSet<Biome> biomes) {
        appliers.applyAll(new JsonMapWrapper(json), biomes);
    }

    @Override
    public Result<IncludesExcludesHolderSet<Biome>, JsonElement> deserialise(final JsonElement input) {
        return JsonResult.forInput(input)
                .mapIfType(String.class, biomeName -> {
                    IncludesExcludesHolderSet<Biome> biomes = IncludesExcludesHolderSet.emptyAnds();
                    biomes.getIncludeComponents().add(new NameRegexMatchHolderSet<>(DELAYED_BIOME_REGISTRY, biomeName.toLowerCase(Locale.ROOT)));
                    return biomes;
                })
                .elseMapIfType(JsonObject.class, selectorObject -> {
                    final IncludesExcludesHolderSet<Biome> biomes = IncludesExcludesHolderSet.emptyAnds();
                    // Apply from all appliers
                    applyAllAppliers(selectorObject, biomes);
                    return biomes;
                }).elseTypeError();
    }

}
