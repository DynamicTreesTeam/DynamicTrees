package com.ferreusveritas.dynamictrees.util.json;

import com.ferreusveritas.dynamictrees.api.treepacks.IVoidPropertyApplier;
import com.ferreusveritas.dynamictrees.util.BiomeList;
import com.google.gson.JsonElement;
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

    private static final IVoidPropertyApplier<BiomeList, String> TYPE_APPLIER = (biomeList, typeString) ->
            biomeList.removeIf(biome -> {
                        final Set<BiomeDictionary.Type> biomeTypes = BiomeDictionary.getTypes(RegistryKey.create(ForgeRegistries.Keys.BIOMES, biome.getRegistryName()));
                        if (typeString.toCharArray()[0] == '!')
                            return biomeTypes.stream().anyMatch(type -> type.toString().toLowerCase().matches(typeString.substring(1).toLowerCase()));
                         else
                            return biomeTypes.stream().noneMatch(type -> type.toString().toLowerCase().matches(typeString.toLowerCase()));
                    }
            );

    private static final IVoidPropertyApplier<BiomeList, String> CATEGORY_APPLIER = (biomeList, categoryString) ->
            biomeList.removeIf(biome -> {
                final String biomeName = biome.getRegistryName().toString();
                if (categoryString.toCharArray()[0] == '!')
                    return biomeName.toLowerCase().matches(categoryString.substring(1).toLowerCase());
                else
                    return !biomeName.toLowerCase().matches(categoryString.toLowerCase());
            });

    private static final IVoidPropertyApplier<BiomeList, String> NAME_APPLIER = (biomeList, nameString) ->
            biomeList.removeIf(biome -> {
                final String biomeName = biome.getRegistryName().toString();
                if (nameString.toCharArray()[0] == '!')
                    return biomeName.matches(nameString.substring(1).toLowerCase());
                else
                    return !biomeName.matches(nameString.toLowerCase());
            });

    private final JsonPropertyApplierList<BiomeList> appliers = new JsonPropertyApplierList<>(BiomeList.class);

    public BiomeListDeserialiser() {
        this.appliers.register("type", String.class, TYPE_APPLIER)
                .registerArrayApplier("types", String.class, TYPE_APPLIER)
                .register("category", String.class, CATEGORY_APPLIER)
                .register("name", String.class, NAME_APPLIER)
                .registerArrayApplier("names", String.class, NAME_APPLIER);
    }

    @Override
    public DeserialisationResult<BiomeList> deserialise(final JsonElement jsonElement) {
        final BiomeList biomes;

        final DeserialisationResult<Biome> biomeResult = JsonDeserialisers.BIOME.deserialise(jsonElement);

        if (biomeResult.wasSuccessful()) {
            biomes = new BiomeList(Collections.singletonList(biomeResult.getValue()));
        } else {
            if (!jsonElement.isJsonObject())
                return DeserialisationResult.failureFromOther(biomeResult);

            // Start with a list of all biomes.
            biomes = BiomeList.getAll();

            // Apply from all appliers, filtering the list.
            this.appliers.applyAll(jsonElement.getAsJsonObject(), biomes);
        }

        return DeserialisationResult.success(biomes);
    }

}
