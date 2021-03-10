package com.ferreusveritas.dynamictrees.util.json;

import com.ferreusveritas.dynamictrees.trees.Family;
import com.ferreusveritas.dynamictrees.trees.FamilyType;
import com.ferreusveritas.dynamictrees.trees.Species;
import com.ferreusveritas.dynamictrees.trees.SpeciesType;
import com.google.gson.JsonElement;
import net.minecraft.util.ResourceLocation;

import java.util.Optional;

/**
 * @author Harley O'Connor
 */
public final class FamilyTypeGetter implements IJsonObjectGetter<FamilyType<Family>> {

    @SuppressWarnings("unchecked")
    @Override
    public ObjectFetchResult<FamilyType<Family>> get(JsonElement jsonElement) {
        final ObjectFetchResult<ResourceLocation> resLocFetchResult = JsonObjectGetters.RESOURCE_LOCATION_GETTER.get(jsonElement);

        if (!resLocFetchResult.wasSuccessful())
            return ObjectFetchResult.failureFromOther(resLocFetchResult);

        final Optional<FamilyType<?>> familyType = FamilyType.get(resLocFetchResult.getValue());

        return familyType.map(type -> ObjectFetchResult.success(((FamilyType<Family>) type))).orElseGet(() -> ObjectFetchResult.failure("Could not find species type for registry name '" + resLocFetchResult.getValue() + "'."));
    }

}
