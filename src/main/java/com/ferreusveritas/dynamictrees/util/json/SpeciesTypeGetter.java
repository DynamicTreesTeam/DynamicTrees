package com.ferreusveritas.dynamictrees.util.json;

import com.ferreusveritas.dynamictrees.trees.Species;
import com.ferreusveritas.dynamictrees.trees.SpeciesType;
import com.ferreusveritas.dynamictrees.trees.TreeSpecies;
import com.google.gson.JsonElement;
import net.minecraft.util.ResourceLocation;

import java.util.Optional;

/**
 * @author Harley O'Connor
 */
public final class SpeciesTypeGetter implements IJsonObjectGetter<SpeciesType<Species>> {

    @SuppressWarnings("unchecked")
    @Override
    public ObjectFetchResult<SpeciesType<Species>> get(JsonElement jsonElement) {
        final ObjectFetchResult<ResourceLocation> resLocFetchResult = JsonObjectGetters.RESOURCE_LOCATION_GETTER.get(jsonElement);

        if (!resLocFetchResult.wasSuccessful())
            return ObjectFetchResult.failureFromOther(resLocFetchResult);

        final Optional<SpeciesType<?>> speciesType = SpeciesType.get(resLocFetchResult.getValue());

        return speciesType.map(type -> ObjectFetchResult.success(((SpeciesType<Species>) type))).orElseGet(() -> ObjectFetchResult.failure("Could not find species type for registry name '" + resLocFetchResult.getValue() + "'."));
    }

}
