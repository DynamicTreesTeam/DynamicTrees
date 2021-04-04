package com.ferreusveritas.dynamictrees.util.json;

import com.ferreusveritas.dynamictrees.api.treepacks.IVoidPropertyApplier;
import com.ferreusveritas.dynamictrees.util.BiomeList;
import com.google.gson.JsonElement;
import net.minecraft.util.RegistryKey;
import net.minecraft.world.biome.Biome;
import net.minecraftforge.common.BiomeDictionary;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.Collections;

/**
 * @author Harley O'Connor
 */
public final class BiomeListGetter implements IJsonObjectGetter<BiomeList> {

    private static final IVoidPropertyApplier<BiomeList, String> TYPE_APPLIER = (biomeList, typeString) ->
            biomeList.removeIf(biome -> BiomeDictionary.getTypes(RegistryKey.create(ForgeRegistries.Keys.BIOMES, biome.getRegistryName()))
                    .stream().noneMatch(type -> type.toString().toLowerCase().matches(typeString.toLowerCase())));

    private static final IVoidPropertyApplier<BiomeList, String> NAME_APPLIER = (biomeList, nameString) ->
            biomeList.removeIf(biome -> !biome.getRegistryName().toString().matches(nameString.toLowerCase()));

    private final JsonPropertyApplierList<BiomeList> appliers = new JsonPropertyApplierList<>(BiomeList.class);

    public BiomeListGetter() {
        this.appliers.register("type", String.class, TYPE_APPLIER)
                .registerArrayApplier("types", String.class, TYPE_APPLIER)
                .register("name", String.class, NAME_APPLIER)
                .registerArrayApplier("names", String.class, NAME_APPLIER);
    }

    @Override
    public ObjectFetchResult<BiomeList> get (final JsonElement jsonElement) {
        final BiomeList biomes;

        final ObjectFetchResult<Biome> biomeFetchResult = JsonObjectGetters.BIOME.get(jsonElement);

        if (biomeFetchResult.wasSuccessful()) {
            biomes = new BiomeList(Collections.singletonList(biomeFetchResult.getValue()));
        } else {
            if (!jsonElement.isJsonObject())
                return ObjectFetchResult.failureFromOther(biomeFetchResult);

            // Start with a list of all biomes.
            biomes = BiomeList.getAll();

            // Apply from all appliers, filtering the list.
            this.appliers.applyAll(jsonElement.getAsJsonObject(), biomes);
        }

        return ObjectFetchResult.success(biomes);
    }

}
