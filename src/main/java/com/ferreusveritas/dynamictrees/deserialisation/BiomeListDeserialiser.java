package com.ferreusveritas.dynamictrees.deserialisation;

import com.ferreusveritas.dynamictrees.api.treepacks.VoidApplier;
import com.ferreusveritas.dynamictrees.deserialisation.result.JsonResult;
import com.ferreusveritas.dynamictrees.deserialisation.result.Result;
import com.ferreusveritas.dynamictrees.util.BiomeList;
import com.ferreusveritas.dynamictrees.util.JsonMapWrapper;
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
public final class BiomeListDeserialiser implements JsonDeserialiser<BiomeList> {

    private static final VoidApplier<BiomeList, String> TYPE_APPLIER = (biomeList, typeString) ->
            biomeList.removeIf(biome -> {
                final Set<BiomeDictionary.Type> biomeTypes = BiomeDictionary.getTypes(RegistryKey.create(ForgeRegistries.Keys.BIOMES, biome.getRegistryName()));
                if (typeString.toCharArray()[0] == '!') {
                    return biomeTypes.stream().anyMatch(type -> type.toString().toLowerCase().matches(typeString.substring(1).toLowerCase()));
                } else {
                    return biomeTypes.stream().noneMatch(type -> type.toString().toLowerCase().matches(typeString.toLowerCase()));
                }
            });

    private static final VoidApplier<BiomeList, String> CATEGORY_APPLIER = (biomeList, categoryString) ->
            biomeList.removeIf(biome -> {
                final String biomeName = String.valueOf(biome.getRegistryName());
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

    private final JsonPropertyAppliers<BiomeList> appliers = new JsonPropertyAppliers<>(BiomeList.class);

    public BiomeListDeserialiser() {
        this.appliers.register("type", String.class, TYPE_APPLIER)
                .registerArrayApplier("types", String.class, TYPE_APPLIER)
                .register("category", String.class, CATEGORY_APPLIER)
                .register("name", String.class, NAME_APPLIER)
                .registerArrayApplier("names", String.class, NAME_APPLIER);
    }

    @Override
    public Result<BiomeList, JsonElement> deserialise(final JsonElement input) {
        return JsonResult.forInput(input)
                .mapIfType(Biome.class, biome -> new BiomeList(Collections.singletonList(biome)))
                .elseMapIfType(JsonObject.class, selectorObject -> {
                    // Start with a list of all biomes.
                    final BiomeList biomes = BiomeList.getAll();
                    // Apply from all appliers, filtering the list.
                    this.appliers.applyAll(new JsonMapWrapper(selectorObject), biomes);

                    return biomes;
                }).elseTypeError();
    }

}
