package com.ferreusveritas.dynamictrees.util.json;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import net.minecraft.util.math.AxisAlignedBB;

/**
 * @author Harley O'Connor
 */
public final class AxisAlignedBBDeserialiser implements JsonDeserialiser<AxisAlignedBB> {

    @Override
    public DeserialisationResult<AxisAlignedBB> deserialise(JsonElement jsonElement) {
        return JsonDeserialisers.JSON_ARRAY.deserialise(jsonElement).map(jsonArray -> {
            if (jsonArray.size() != 6 || !this.allElementsNumber(jsonArray))
                return null;

            final double[] params = new double[6];

            for (int i = 0; i < jsonArray.size(); i++) {
                params[i] = JsonDeserialisers.DOUBLE.deserialise(jsonArray.get(i)).getValue();
            }

            return new AxisAlignedBB(params[0], params[1], params[2], params[3], params[4], params[5]);
        }, "Array was not of correct size 6 or all elements were not numbers.");
    }

    public boolean allElementsNumber(final JsonArray jsonArray) {
        for (JsonElement jsonElement : jsonArray) {
            if (!jsonElement.isJsonPrimitive() || !jsonElement.getAsJsonPrimitive().isNumber())
                return false;
        }
        return true;
    }

}
