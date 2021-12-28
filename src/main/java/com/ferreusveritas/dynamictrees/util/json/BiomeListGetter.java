package com.ferreusveritas.dynamictrees.util.json;

import com.ferreusveritas.dynamictrees.api.treepacks.IVoidPropertyApplier;
import com.ferreusveritas.dynamictrees.util.BiomeList;
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
public final class BiomeListGetter implements IJsonObjectGetter<BiomeList> {

    private static final IVoidPropertyApplier<BiomeList, String> TYPE_APPLIER = (biomeList, typeString) ->
            biomeList.removeIf(biome -> {
                        final Set<BiomeDictionary.Type> biomeTypes = BiomeDictionary.getTypes(RegistryKey.create(ForgeRegistries.Keys.BIOMES, biome.getRegistryName()));
                if (typeString.toCharArray()[0] == '!') {
                    return biomeTypes.stream().anyMatch(type -> type.toString().toLowerCase().matches(typeString.substring(1).toLowerCase()));
                } else {
                    return biomeTypes.stream().noneMatch(type -> type.toString().toLowerCase().matches(typeString.toLowerCase()));
                }
                    }
            );

    private static final IVoidPropertyApplier<BiomeList, String> CATEGORY_APPLIER = (biomeList, categoryString) ->
            biomeList.removeIf(biome -> {
                final String biomeName = biome.getRegistryName().toString();
                if (categoryString.toCharArray()[0] == '!') {
                    return biomeName.toLowerCase().matches(categoryString.substring(1).toLowerCase());
                } else {
                    return !biomeName.toLowerCase().matches(categoryString.toLowerCase());
                }
            });

    private static final IVoidPropertyApplier<BiomeList, String> NAME_APPLIER = (biomeList, nameString) ->
            biomeList.removeIf(biome -> {
                final String biomeName = biome.getRegistryName().toString();
                if (nameString.toCharArray()[0] == '!') {
                    return biomeName.matches(nameString.substring(1).toLowerCase());
                } else {
                    return !biomeName.matches(nameString.toLowerCase());
                }
            });
    private static final IVoidPropertyApplier<BiomeList, JsonArray> NAMES_OR_APPLIER = (biomeList, arrayList) ->{
        BiomeList whiteList = new BiomeList();
        BiomeList blackList = new BiomeList();
        BiomeList.getAll().forEach((biome -> {
            final String biomeName = biome.getRegistryName().toString();
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

    private final IVoidPropertyApplier<BiomeList, JsonArray> OR_OPERATOR = (biomes, jsonArray) -> {
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

    private final JsonPropertyApplierList<BiomeList> appliers = new JsonPropertyApplierList<>(BiomeList.class);

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

    private void applyAllAppliers(JsonObject obj, BiomeList biomes){
        appliers.applyAll(obj, biomes);
    }

    @Override
    public ObjectFetchResult<BiomeList> get(final JsonElement jsonElement) {
        final BiomeList biomes;

        final ObjectFetchResult<Biome> biomeFetchResult = JsonObjectGetters.BIOME.get(jsonElement);

        if (biomeFetchResult.wasSuccessful()) {
            biomes = new BiomeList(Collections.singletonList(biomeFetchResult.getValue()));
        } else {
            if (!jsonElement.isJsonObject()) {
                return ObjectFetchResult.failureFromOther(biomeFetchResult);
            }
            JsonObject jsonObject = jsonElement.getAsJsonObject();

            // Start with a list of all biomes.
            biomes = BiomeList.getAll();

            // Apply from all appliers, filtering the list.
            applyAllAppliers(jsonObject, biomes);
        }

        return ObjectFetchResult.success(biomes);
    }

}
