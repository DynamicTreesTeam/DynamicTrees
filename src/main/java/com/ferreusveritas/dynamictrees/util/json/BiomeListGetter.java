package com.ferreusveritas.dynamictrees.util.json;

import com.ferreusveritas.dynamictrees.api.treepacks.VoidApplier;
import com.ferreusveritas.dynamictrees.deserialisation.JsonDeserialiser;
import com.ferreusveritas.dynamictrees.deserialisation.JsonDeserialisers;
import com.ferreusveritas.dynamictrees.deserialisation.JsonPropertyAppliers;
import com.ferreusveritas.dynamictrees.deserialisation.result.JsonResult;
import com.ferreusveritas.dynamictrees.deserialisation.result.Result;
import com.ferreusveritas.dynamictrees.util.BiomeList;
import com.ferreusveritas.dynamictrees.util.JsonMapWrapper;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.util.RegistryKey;
import net.minecraft.world.biome.Biome;
import net.minecraftforge.common.BiomeDictionary;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.Collections;
import java.util.Set;

/**
 * @author Harley O'Connor
 */
public final class BiomeListGetter implements JsonDeserialiser<BiomeList> {

    private static final VoidApplier<BiomeList, String> TYPE_APPLIER = (biomeList, typeString) ->
            biomeList.removeIf(biome -> {
                        final Set<BiomeDictionary.Type> biomeTypes = BiomeDictionary.getTypes(RegistryKey.create(ForgeRegistries.Keys.BIOMES, biome.getRegistryName()));
                if (typeString.toCharArray()[0] == '!') {
                    return biomeTypes.stream().anyMatch(type -> type.toString().toLowerCase().matches(typeString.substring(1).toLowerCase()));
                } else {
                    return biomeTypes.stream().noneMatch(type -> type.toString().toLowerCase().matches(typeString.toLowerCase()));
                }
                    }
            );

    private static final VoidApplier<BiomeList, String> CATEGORY_APPLIER = (biomeList, categoryString) ->
            biomeList.removeIf(biome -> {
                final String biomeName = biome.getRegistryName().toString();
                if (categoryString.toCharArray()[0] == '!') {
                    return biomeName.toLowerCase().matches(categoryString.substring(1).toLowerCase());
                } else {
                    return !biomeName.toLowerCase().matches(categoryString.toLowerCase());
                }
            });

    private static final VoidApplier<BiomeList, String> NAME_APPLIER = (biomeList, nameString) ->
            biomeList.removeIf(biome -> {
                final String biomeName = String.valueOf(biome.getRegistryName());
                if (nameString.toCharArray()[0] == '!') {
                    return biomeName.matches(nameString.substring(1).toLowerCase());
                } else {
                    return !biomeName.matches(nameString.toLowerCase());
                }
            });
    private static final VoidApplier<BiomeList, JsonArray> NAMES_OR_APPLIER = (biomeList, arrayList) ->{
        BiomeList whiteList = new BiomeList();
        BiomeList blackList = new BiomeList();
        BiomeList.getAll().forEach((biome -> {
            final String biomeName = String.valueOf(biome.getRegistryName());
            arrayList.forEach((jsonElement)->{
                String nameString = jsonElement.getAsString();
                if (nameString.toCharArray()[0] == '!') {
                    if (biomeName.matches(nameString.substring(1).toLowerCase())) blackList.add(biome);
                } else {
                    if (biomeName.matches(nameString.toLowerCase())) whiteList.add(biome);
                }
            });
        }));
        whiteList.removeIf(blackList::contains);
        biomeList.removeIf((biome)-> !whiteList.contains(biome));
    };

    private final VoidApplier<BiomeList, JsonArray> OR_OPERATOR = (biomes, jsonArray) -> {
        BiomeList biomesList = new BiomeList();
        for (JsonElement elem : jsonArray){
            if (elem instanceof JsonObject){
                BiomeList thisBiomesList = BiomeList.getAll();
                applyAllAppliers(elem.getAsJsonObject(), thisBiomesList);
                biomesList.addAll(thisBiomesList);
            }
        }
        biomes.removeIf(biome -> !biomesList.contains(biome));
    };

    private final JsonPropertyAppliers<BiomeList> appliers = new JsonPropertyAppliers<>(BiomeList.class);

    public BiomeListGetter() {
        this.appliers.register("type", String.class, TYPE_APPLIER)
                .registerArrayApplier("types", String.class, TYPE_APPLIER)
                .register("category", String.class, CATEGORY_APPLIER)
                .register("name", String.class, NAME_APPLIER)
                .registerArrayApplier("names", String.class, NAME_APPLIER)
                .register("names_or", JsonArray.class, NAMES_OR_APPLIER)
                .registerArrayApplier("AND", JsonObject.class, (biomes, jsonObject) -> applyAllAppliers(jsonObject, biomes))
                .register("OR", JsonArray.class, OR_OPERATOR);
    }

    private void applyAllAppliers(JsonObject json, BiomeList biomes){
        appliers.applyAll(new JsonMapWrapper(json), biomes);
    }

    @Override
    public Result<BiomeList, JsonElement> deserialise(final JsonElement json) {
        final BiomeList biomes;

        final Result<Biome, JsonElement> result = JsonDeserialisers.BIOME.deserialise(json);

        if (result.success()) {
            biomes = new BiomeList(Collections.singletonList(result.get()));
        } else {
            if (!json.isJsonObject()) {
                return result.map(biome -> new BiomeList(Collections.singletonList(biome)));
            }
            JsonObject jsonObject = json.getAsJsonObject();

            // Start with a list of all biomes.
            biomes = BiomeList.getAll();

            // Apply from all appliers, filtering the list.
            applyAllAppliers(jsonObject, biomes);
        }

        return JsonResult.success(json, biomes);
    }

}
