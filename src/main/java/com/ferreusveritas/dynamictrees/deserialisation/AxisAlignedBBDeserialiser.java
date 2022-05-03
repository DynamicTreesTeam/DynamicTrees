package com.ferreusveritas.dynamictrees.deserialisation;

import com.ferreusveritas.dynamictrees.deserialisation.result.Result;
import com.google.gson.JsonElement;
import net.minecraft.world.phys.AABB;

/**
 * @author Harley O'Connor
 */
public final class AxisAlignedBBDeserialiser implements JsonDeserialiser<AABB> {

    @Override
    public Result<AABB, JsonElement> deserialise(JsonElement jsonElement) {
        return JsonDeserialisers.JSON_ARRAY.deserialise(jsonElement).map((jsonArray, warningConsumer) -> {
            if (jsonArray.size() != 6) {
                throw DeserialisationException.error("Array was not of correct size (6).");
            }

            final double[] params = new double[6];

            for (int i = 0; i < jsonArray.size(); i++) {
                params[i] = JsonDeserialisers.DOUBLE.deserialise(jsonArray.get(i)).orElseThrow();
            }

            return new AABB(params[0], params[1], params[2], params[3], params[4], params[5]);
        });
    }

}
